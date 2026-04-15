package com.splito.observability;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@ConfigurationProperties(prefix = "app.logging.http")
public record HttpLoggingProperties(
        boolean enabled,
        boolean includeHeaders,
        boolean includeBody,
        int maxBodyBytes,
        String correlationHeader,
        List<String> excludePaths,
        Mask mask
) {
    public record Mask(List<String> headers, List<String> jsonFields) {}

    public Set<String> maskHeadersLower() {
        if (mask == null || mask.headers == null) return Set.of();
        return mask.headers.stream()
                .map(s -> s.toLowerCase(Locale.ROOT))
                .collect(HashSet::new, Set::add, Set::addAll);
    }

    public Set<String> maskJsonFieldsLower() {
        if (mask == null || mask.jsonFields == null) return Set.of();
        return mask.jsonFields.stream()
                .map(s -> s.toLowerCase(Locale.ROOT))
                .collect(HashSet::new, Set::add, Set::addAll);
    }
}
