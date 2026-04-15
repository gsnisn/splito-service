package com.splito.service;

import com.splito.dto.request.*;
import com.splito.dto.response.AuthResponse;
import com.splito.exception.ResourceNotFoundException;
import com.splito.model.SplitoUser;
import com.splito.repository.UserRepository;
import com.splito.security.JwtService;
import com.splito.utility.LogMasking;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.splito.utility.LogMasking.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    @Transactional
    public AuthResponse signup(SignupRequest req, String device) {

        log.info("AUTH_SIGNUP_START email={} phone={} device={}",
                maskEmail(req.getEmail()), maskPhone(req.getPhone()), safe(device));

        if (userRepository.existsByEmail(req.getEmail())) {
            log.warn("AUTH_SIGNUP_EMAIL_EXISTS email={} device={}",
                    maskEmail(req.getEmail()), safe(device));
            throw new IllegalArgumentException("Email already registered");
        }

        SplitoUser u = new SplitoUser();
        u.setName(req.getName());
        u.setEmail(req.getEmail());
        u.setPhone(req.getPhone());

        // never log password (raw or encoded)
        u.setPassword(passwordEncoder.encode(req.getPassword()));

        SplitoUser saved = userRepository.save(u);
        log.info("AUTH_SIGNUP_USER_CREATED userId={} email={} device={}",
                saved.getId(), maskEmail(saved.getEmail()), safe(device));

        String access = jwtService.generateAccessToken(saved.getId(), saved.getEmail());
        log.debug("AUTH_SIGNUP_ACCESS_TOKEN_ISSUED userId={}", saved.getId()); // never log token

        var refreshPair = refreshTokenService.issue(saved, device);
        log.info("AUTH_SIGNUP_REFRESH_TOKEN_ISSUED userId={} device={} refreshTokenId={}",
                saved.getId(), safe(device), refreshPair.entity().getId()); // log DB id, not token

        log.info("AUTH_SIGNUP_SUCCESS userId={}", saved.getId());
        return new AuthResponse(access, refreshPair.rawToken());
    }

    @Transactional
    public AuthResponse login(LoginRequest req, String device) {

        log.info("AUTH_LOGIN_START email={} device={}", maskEmail(req.getEmail()), safe(device));

        SplitoUser u = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> {
                    log.warn("AUTH_LOGIN_FAIL_USER_NOT_FOUND email={} device={}",
                            maskEmail(req.getEmail()), safe(device));
                    return new ResourceNotFoundException("Invalid credentials");
                });

        if (u.getPassword() == null || u.getPassword().isBlank()) {
            log.error("AUTH_LOGIN_FAIL_PASSWORD_NOT_SET userId={} email={}",
                    u.getId(), maskEmail(u.getEmail()));
            throw new IllegalArgumentException("Password not set for this user");
        }

        if (!passwordEncoder.matches(req.getPassword(), u.getPassword())) {
            log.warn("AUTH_LOGIN_FAIL_BAD_PASSWORD userId={} email={} device={}",
                    u.getId(), maskEmail(u.getEmail()), safe(device));
            throw new ResourceNotFoundException("Invalid credentials");
        }

        log.info("AUTH_LOGIN_PASSWORD_OK userId={} email={}", u.getId(), maskEmail(u.getEmail()));

        String access = jwtService.generateAccessToken(u.getId(), u.getEmail());
        log.debug("AUTH_LOGIN_ACCESS_TOKEN_ISSUED userId={}", u.getId());

        var refreshPair = refreshTokenService.issue(u, device);
        log.info("AUTH_LOGIN_REFRESH_TOKEN_ISSUED userId={} device={} refreshTokenId={}",
                u.getId(), safe(device), refreshPair.entity().getId());

        log.info("AUTH_LOGIN_SUCCESS userId={}", u.getId());
        return new AuthResponse(access, refreshPair.rawToken());
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest req, String device) {

        log.info("AUTH_REFRESH_START device={} tokenPresent={}", safe(device), req.getRefreshToken() != null);

        var rotated = refreshTokenService.rotate(req.getRefreshToken(), device);

        SplitoUser user = rotated.entity().getUser();
        log.info("AUTH_REFRESH_ROTATED userId={} device={} newTokenId={}",
                user.getId(), safe(device), rotated.entity().getId());

        String access = jwtService.generateAccessToken(user.getId(), user.getEmail());
        log.debug("AUTH_REFRESH_ACCESS_TOKEN_ISSUED userId={}", user.getId());

        log.info("AUTH_REFRESH_SUCCESS userId={}", user.getId());
        return new AuthResponse(access, rotated.rawToken());
    }

    @Transactional
    public void logout(LogoutRequest req) {
        log.info("AUTH_LOGOUT_START tokenPresent={}", req.getRefreshToken() != null);
        refreshTokenService.revoke(req.getRefreshToken());
        log.info("AUTH_LOGOUT_SUCCESS");
    }

}
