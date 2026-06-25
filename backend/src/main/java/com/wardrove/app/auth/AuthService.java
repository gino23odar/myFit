package com.wardrove.app.auth;

import com.wardrove.app.users.User;
import com.wardrove.app.users.UserRepository;
import com.wardrove.app.users.UserStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Clock clock = Clock.systemUTC();

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            JwtProperties jwtProperties
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
    }

    @Transactional
    public AuthResponse signup(AuthRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new AuthException("AUTH_EMAIL_ALREADY_EXISTS", "An account with this email already exists.");
        }

        Instant now = clock.instant();
        User user = new User(
                UUID.randomUUID(),
                email,
                passwordEncoder.encode(request.password()),
                UserStatus.ACTIVE,
                now,
                now
        );

        userRepository.save(user);
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse signin(AuthRequest request) {
        User user = userRepository.findByEmailIgnoreCase(normalizeEmail(request.email()))
                .filter(candidate -> candidate.getStatus() == UserStatus.ACTIVE)
                .filter(candidate -> passwordEncoder.matches(request.password(), candidate.getPasswordHash()))
                .orElseThrow(() -> new AuthException("AUTH_INVALID_CREDENTIALS", "Invalid email or password."));

        return issueTokens(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        String tokenHash = hashToken(request.refreshToken());
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .filter(token -> token.getRevokedAt() == null)
                .filter(token -> token.getExpiresAt().isAfter(clock.instant()))
                .orElseThrow(() -> new AuthException("AUTH_TOKEN_EXPIRED", "Refresh token is invalid or expired."));

        refreshToken.revoke(clock.instant());
        return issueTokens(refreshToken.getUser());
    }

    @Transactional
    public void signout(RefreshTokenRequest request) {
        refreshTokenRepository.findByTokenHash(hashToken(request.refreshToken()))
                .filter(token -> token.getRevokedAt() == null)
                .ifPresent(token -> token.revoke(clock.instant()));
    }

    private AuthResponse issueTokens(User user) {
        String accessToken = jwtService.createAccessToken(user);
        String refreshToken = createOpaqueToken();
        Instant now = clock.instant();

        refreshTokenRepository.save(new RefreshToken(
                UUID.randomUUID(),
                user,
                hashToken(refreshToken),
                now.plus(jwtProperties.refreshTokenTtl()),
                now
        ));

        return new AuthResponse(accessToken, refreshToken, "Bearer", jwtService.accessTokenTtlSeconds());
    }

    private String createOpaqueToken() {
        byte[] bytes = new byte[64];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
