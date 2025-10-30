package com.lynkai.controller;

import com.lynkai.dto.*;
import com.lynkai.model.ActionType;
import com.lynkai.model.ActivityLog;
import com.lynkai.model.User;
import com.lynkai.service.ActivityLogService;
import com.lynkai.service.AuthService;
import com.lynkai.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final AuthService authService;
    private final ActivityLogService activityLogService;
    private final UserService userService;

    public AuthController(AuthService authService,
                          ActivityLogService activityLogService,
                          UserService userService) {
        this.authService = authService;
        this.activityLogService = activityLogService;
        this.userService = userService;
    }

    /**
     * Register a new user.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (userService.existsByUsername(request.getUsername()) || userService.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Username or email already in use."));
        }

        User user = authService.registerUser(request.getUsername(), request.getEmail(), request.getPassword());
        activityLogService.saveLog(new ActivityLog(user, ActionType.REGISTER));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Registration successful! Please verify your email."));
    }




    /**
     * Login using username or email.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            // Find user by username or email
            User user = userService.getByUsernameOrEmail(request.getUsernameOrEmail());

            // Check if user is verified
            if (!user.isVerified()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of(
                                "message", "Email not verified. Please verify your account before logging in.",
                                "code", "EMAIL_NOT_VERIFIED",
                                "email", user.getEmail()
                        ));
            }

            // Normal authentication
            AuthService.TokenPair tokens = authService.loginUser(user.getUsername(), request.getPassword());

            activityLogService.saveLog(new ActivityLog(user, ActionType.LOGIN));
            return ResponseEntity.ok(tokens);

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid credentials"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Login failed"));
        }
    }


    /**
     * Refresh JWT tokens.
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshRequest request) {
        try {
            AuthService.TokenPair tokens = authService.refresh(request.getRefreshToken());
            return ResponseEntity.ok(tokens);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired refresh token.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Token refresh failed.");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody LogoutRequest request) {
        authService.invalidateRefreshToken(request.getRefreshToken());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(
            @Valid @RequestBody ResendVerificationRequest request) {

        try {
            User user = userService.getUserByEmail(request.getEmail());

            if (user.isVerified()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Account is already verified."));
            }

            String code = authService.generateVerificationCode();
            authService.sendVerificationEmail(user.getEmail(), code);
            authService.saveVerificationCode(user.getId(), code);

            return ResponseEntity.ok(Map.of("message", "Verification code resent successfully."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Failed to resend verification code."));
        }
    }

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(
            @Valid @RequestBody VerifyEmailRequest request) {

        try {
            User user = userService.getUserByEmail(request.getEmail());

            authService.verifyCode(user.getId(), request.getCode());

            AuthService.TokenPair tokens = authService.generateTokensForUser(user);

            activityLogService.saveLog(new ActivityLog(user, ActionType.LOGIN));

            return ResponseEntity.ok(tokens);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Email verification failed. Please try again."));
        }
    }


    @GetMapping("/check-verification")
    public ResponseEntity<?> checkVerification(@RequestParam String email) {
        try {
            User user = userService.getUserByEmail(email);
            return ResponseEntity.ok(Map.of("verified", user.isVerified()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("verified", false));
        }
    }



}
