package com.splito.observability;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class HttpLoggingFilter extends OncePerRequestFilter {

    private final HttpLoggingProperties props;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher matcher = new AntPathMatcher();

    private static final String MDC_CORRELATION_ID = "correlationId";

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!props.enabled()) return true;

        String path = request.getRequestURI();
        for (String prefix : props.excludePaths()) {
            if (path.startsWith(prefix)) return true;
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        Instant start = Instant.now();

        String correlationId = resolveCorrelationId(request);
        MDC.put(MDC_CORRELATION_ID, correlationId);
        response.setHeader(props.correlationHeader(), correlationId);

        ContentCachingRequestWrapper reqWrap = new ContentCachingRequestWrapper(request, props.maxBodyBytes());
        ContentCachingResponseWrapper resWrap = new ContentCachingResponseWrapper(response);

        try {
            logRequest(reqWrap, correlationId);
            filterChain.doFilter(reqWrap, resWrap);
        } finally {
            logResponse(reqWrap, resWrap, correlationId, Duration.between(start, Instant.now()));
            resWrap.copyBodyToResponse();
            MDC.remove(MDC_CORRELATION_ID);
        }
    }

    private String resolveCorrelationId(HttpServletRequest request) {
        String header = request.getHeader(props.correlationHeader());
        if (StringUtils.hasText(header)) return header.trim();
        return UUID.randomUUID().toString();
    }

    private void logRequest(ContentCachingRequestWrapper req, String correlationId) {

        String method = req.getMethod();
        String uri = req.getRequestURI();
        String qs = req.getQueryString();

        String path = (qs == null) ? uri : (uri + "?" + qs);

        String user = (req.getUserPrincipal() != null) ? req.getUserPrincipal().getName() : "anonymous";

        String headersPart = props.includeHeaders() ? (" headers=" + safeHeaders(req)) : "";
        String bodyPart = props.includeBody() ? (" body=" + safeRequestBody(req)) : "";

        log.info("HTTP IN  {} {} user={} cid={}{}{}",
                method, path, user, correlationId, headersPart, bodyPart);
    }

    private void logResponse(ContentCachingRequestWrapper req,
                             ContentCachingResponseWrapper res,
                             String correlationId,
                             Duration duration) {

        String method = req.getMethod();
        String uri = req.getRequestURI();
        int status = res.getStatus();

        String bodyPart = props.includeBody() ? (" body=" + safeResponseBody(res)) : "";

        log.info("HTTP OUT {} {} status={} durMs={} cid={}{}",
                method, uri, status, duration.toMillis(), correlationId, bodyPart);
    }

    private String safeHeaders(HttpServletRequest req) {
        Enumeration<String> names = req.getHeaderNames();
        if (names == null) return "{}";

        Set<String> maskSet = props.maskHeadersLower();

        Map<String, String> map = new LinkedHashMap<>();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            String value = req.getHeader(name);

            if (value == null) continue;

            if (maskSet.contains(name.toLowerCase(Locale.ROOT))) {
                map.put(name, "***");
            } else {
                map.put(name, truncate(value, 256));
            }
        }
        return map.toString();
    }

    private String safeRequestBody(ContentCachingRequestWrapper req) {
        if (!isBodyLoggable(req.getContentType())) return "[non-loggable-content-type]";
        byte[] buf = req.getContentAsByteArray(); // populated after read; may be empty before controller reads
        if (buf.length == 0) return "";
        return sanitizeBody(buf, req.getContentType());
    }

    private String safeResponseBody(ContentCachingResponseWrapper res) {
        if (!isBodyLoggable(res.getContentType())) return "[non-loggable-content-type]";
        byte[] buf = res.getContentAsByteArray();
        if (buf.length == 0) return "";
        return sanitizeBody(buf, res.getContentType());
    }

    private String sanitizeBody(byte[] buf, String contentType) {
        int max = Math.max(0, props.maxBodyBytes());
        int len = Math.min(buf.length, max);
        String raw = new String(buf, 0, len, StandardCharsets.UTF_8);

        if (contentType != null && contentType.toLowerCase(Locale.ROOT).contains(MediaType.APPLICATION_JSON_VALUE)) {
            return sanitizeJson(raw);
        }
        return truncate(raw.replaceAll("\\s+", " "), max);
    }

    private boolean isBodyLoggable(String contentType) {
        if (!StringUtils.hasText(contentType)) return false;
        String ct = contentType.toLowerCase(Locale.ROOT);

        // avoid multipart, binaries etc.
        if (ct.contains("multipart/form-data")) return false;
        if (ct.contains("octet-stream")) return false;

        // log JSON + text-like
        return ct.contains("application/json") || ct.startsWith("text/");
    }

    private String sanitizeJson(String raw) {
        try {
            JsonNode root = objectMapper.readTree(raw);
            maskJsonFields(root, props.maskJsonFieldsLower());
            String out = objectMapper.writeValueAsString(root);
            return truncate(out, props.maxBodyBytes());
        } catch (Exception e) {
            // if invalid json, just truncate
            return truncate(raw, props.maxBodyBytes());
        }
    }

    private void maskJsonFields(JsonNode node, Set<String> fieldsToMaskLower) {
        if (node == null) return;

        if (node.isObject()) {
            ObjectNode obj = (ObjectNode) node;
            Iterator<String> it = obj.fieldNames();
            List<String> names = new ArrayList<>();
            it.forEachRemaining(names::add);

            for (String name : names) {
                JsonNode child = obj.get(name);
                if (fieldsToMaskLower.contains(name.toLowerCase(Locale.ROOT))) {
                    obj.put(name, "***");
                } else {
                    maskJsonFields(child, fieldsToMaskLower);
                }
            }
        } else if (node.isArray()) {
            for (JsonNode child : node) {
                maskJsonFields(child, fieldsToMaskLower);
            }
        }
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        if (max <= 0) return "";
        if (s.length() <= max) return s;
        return s.substring(0, max) + "...(truncated)";
    }
}
