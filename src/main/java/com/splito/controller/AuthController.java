package com.splito.controller;

import com.splito.dto.request.*;
import com.splito.dto.response.ApiMessageResponse;
import com.splito.dto.response.AuthResponse;
import com.splito.service.AuthService;
import com.splito.service.PasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    private final Environment env;

    @PostMapping("/signup")
    public AuthResponse signup(@Valid @RequestBody SignupRequest req, HttpServletRequest http) {
        return authService.signup(req, device(http));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req, HttpServletRequest http) {
        return authService.login(req, device(http));
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest req, HttpServletRequest http) {
        return authService.refresh(req, device(http));
    }

    @PostMapping("/logout")
    public ApiMessageResponse logout(@Valid @RequestBody LogoutRequest req) {
        authService.logout(req);
        return new ApiMessageResponse("Logged out");
    }

    @PostMapping("/forgot-password")
    public ApiMessageResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {

        var pair = passwordResetService.createResetTokenIfUserExists(req.email());

        // ✅ Log raw token ONLY in dev
        if (pair != null && isDev()) {
            log.info("RESET TOKEN for {}: {}", req.email(), pair.rawToken());
        }

        return new ApiMessageResponse("If the account exists, a reset link has been sent.");
    }

    @PostMapping("/reset-password")
    public ApiMessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        passwordResetService.resetPassword(req.token(), req.newPassword());
        return new ApiMessageResponse("Password updated successfully");
    }

    private boolean isDev() {
        return env.acceptsProfiles("dev");
    }

    private String device(HttpServletRequest req) {
        String ua = req.getHeader("User-Agent");
        if (ua == null) ua = "unknown";
        ua = ua.length() > 120 ? ua.substring(0, 120) : ua;

        String ip = req.getRemoteAddr();
        return ua + " | ip=" + ip;
    }

}
