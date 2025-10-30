package com.lynkai.service;

import com.lynkai.model.EmailVerification;
import com.lynkai.model.RefreshToken;
import com.lynkai.model.User;
import com.lynkai.repository.EmailVerificationRepository;
import com.lynkai.repository.RefreshTokenRepository;
import com.lynkai.repository.UserRepository;
import com.lynkai.security.HashEncoder;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
public class AuthService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final HashEncoder hashEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailVerificationRepository verificationRepository;
    private final EmailService emailService;
    private final SecureRandom random = new SecureRandom();

    public AuthService(JwtService jwtService,
                       UserRepository userRepository,
                       HashEncoder hashEncoder,
                       RefreshTokenRepository refreshTokenRepository,
                       EmailVerificationRepository verificationRepository,
                       EmailService emailService) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.hashEncoder = hashEncoder;
        this.refreshTokenRepository = refreshTokenRepository;
        this.verificationRepository = verificationRepository;
        this.emailService = emailService;
    }

    // ✅ FIXED: Convert user.getId() to String, not the entire user object
    public TokenPair generateTokensForUser(User user) {
        String accessToken = jwtService.generateAccessToken(String.valueOf(user.getId()));
        String refreshToken = jwtService.generateRefreshToken(String.valueOf(user.getId()));
        storeRefreshToken(user.getId(), refreshToken);
        return new TokenPair(accessToken, refreshToken);
    }

    // Token pair class
    public record TokenPair(String accessToken, String refreshToken) {}

    // Register a new user (without marking verified)
    public User registerUser(String username, String email, String password) {
        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(hashEncoder.encode(password))
                .verified(false)
                .build();

        User savedUser = userRepository.save(user);

        // Send email in a safe way
        try {
            String code = generateVerificationCode();
            sendVerificationEmail(user.getEmail(), code);
            saveVerificationCode(user.getId(), code);
        } catch (Exception emailEx) {
            System.err.println("Failed to send verification email: " + emailEx.getMessage());
        }

        return savedUser;
    }

    // Login user and return access + refresh token
    @Transactional
    public TokenPair loginUser(String username, String password) {
        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new BadCredentialsException("Invalid credentials."));

        if (!hashEncoder.matches(password, user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials.");
        }

        if (!user.isVerified()) {
            throw new BadCredentialsException("Account not verified. Please check your email.");
        }

        // Remove old refresh tokens
        refreshTokenRepository.deleteAllByUserId(user.getId());

        // ✅ CORRECT: Already using user.getId()
        String newAccessToken = jwtService.generateAccessToken(String.valueOf(user.getId()));
        String newRefreshToken = jwtService.generateRefreshToken(String.valueOf(user.getId()));

        storeRefreshToken(user.getId(), newRefreshToken);

        return new TokenPair(newAccessToken, newRefreshToken);
    }

    // Refresh tokens
    @Transactional
    public TokenPair refresh(String refreshToken) {
        if (!jwtService.validateRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token.");
        }

        Long userId = jwtService.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token."));

        String hashed = hashToken(refreshToken);

        RefreshToken storedToken = refreshTokenRepository
                .findByUserIdAndHashedToken(user.getId(), hashed)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token not recognized."));

        if (storedToken.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Refresh token expired.");
        }

        refreshTokenRepository.deleteByUserIdAndHashedToken(user.getId(), hashed);


        String newAccessToken = jwtService.generateAccessToken(String.valueOf(user.getId()));
        String newRefreshToken = jwtService.generateRefreshToken(String.valueOf(user.getId()));

        storeRefreshToken(user.getId(), newRefreshToken);

        return new TokenPair(newAccessToken, newRefreshToken);
    }

    // Invalidate refresh token
    @Transactional
    public void invalidateRefreshToken(String refreshToken) {
        String hashed = hashToken(refreshToken);
        refreshTokenRepository.deleteByHashedToken(hashed);
    }

    // Store hashed refresh token in DB
    private void storeRefreshToken(Long userId, String rawRefreshToken) {
        String hashedRefreshToken = hashToken(rawRefreshToken);
        Instant expiresAt = Instant.now().plusMillis(jwtService.getRefreshTokenValidityMs());

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(userId)
                .hashedToken(hashedRefreshToken)
                .expiresAt(expiresAt)
                .build();

        refreshTokenRepository.save(refreshToken);
    }

    // SHA-256 hash + Base64 encoding
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(token.getBytes());
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing token", e);
        }
    }

    // ------------------ Email Verification ------------------

    public String generateVerificationCode() {
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    public void sendVerificationEmail(String toEmail, String code) {
        emailService.sendVerificationEmail(toEmail, code);
    }

    public void saveVerificationCode(Long userId, String code) {
        EmailVerification emailVerification = new EmailVerification();
        emailVerification.setUserId(userId);
        emailVerification.setCode(code);
        emailVerification.setExpiresAt(Instant.now().plusSeconds(10 * 60)); // 10 min expiry
        verificationRepository.save(emailVerification);
    }

    public void verifyCode(Long userId, String code) {
        EmailVerification ev = verificationRepository.findByUserIdAndCode(userId, code)
                .orElseThrow(() -> new IllegalArgumentException("Invalid verification code."));

        if (ev.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Verification code expired.");
        }

        // Mark user as verified
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        user.setVerified(true);
        userRepository.save(user);

        verificationRepository.delete(ev);
    }
}