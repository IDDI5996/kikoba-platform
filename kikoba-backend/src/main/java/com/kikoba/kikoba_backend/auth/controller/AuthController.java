package com.kikoba.kikoba_backend.auth.controller;

import com.kikoba.kikoba_backend.auth.dto.InitiateRegistrationRequest;
import com.kikoba.kikoba_backend.auth.dto.LoginInitiateResponse;
import com.kikoba.kikoba_backend.auth.dto.MfaVerificationRequest;
import com.kikoba.kikoba_backend.auth.dto.RegistrationProgressResponse;
import com.kikoba.kikoba_backend.auth.dto.VerifyOtpRequest;
import com.kikoba.kikoba_backend.auth.service.AuthService;
import com.kikoba.kikoba_backend.user.dto.AuthResponse;
import com.kikoba.kikoba_backend.user.dto.LoginRequest;
import com.kikoba.kikoba_backend.user.dto.RegisterRequest;
import com.kikoba.kikoba_backend.user.dto.ForgotPasswordRequest;
import com.kikoba.kikoba_backend.user.dto.ResetPasswordRequest;
import com.kikoba.kikoba_backend.user.service.PasswordResetService;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    // ---- Legacy endpoints ----
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // ---- New secure registration flow ----
    @PostMapping("/register/initiate")
    public ResponseEntity<RegistrationProgressResponse> initiateRegistration(
            @Valid @RequestBody InitiateRegistrationRequest request,
            HttpServletRequest http) {
        return ResponseEntity.ok(authService.initiateRegistration(request, clientIp(http)));
    }

    @PostMapping("/register/verify-phone")
    public ResponseEntity<RegistrationProgressResponse> verifyPhone(
            @Valid @RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(authService.verifyRegistrationPhone(request));
    }

    @PostMapping("/register/verify-email")
    public ResponseEntity<RegistrationProgressResponse> verifyEmail(
            @Valid @RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(authService.verifyRegistrationEmail(request));
    }

    @PostMapping("/register/complete")
    public ResponseEntity<AuthResponse> completeRegistration(
            @Valid @RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(authService.completeRegistration(request));
    }

    @PostMapping("/register/resend-otp")
    public ResponseEntity<RegistrationProgressResponse> resendOtp(
            @RequestParam String recipient,
            @RequestParam String purpose) {
        return ResponseEntity.ok(authService.resendRegistrationOtp(recipient, purpose));
    }

    // ---- New secure login flow ----
    @PostMapping("/login/initiate")
    public ResponseEntity<LoginInitiateResponse> initiateLogin(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest http) {
        return ResponseEntity.ok(authService.initiateLogin(request, clientIp(http)));
    }

    @PostMapping("/login/verify-mfa")
    public ResponseEntity<AuthResponse> verifyMfa(
            @Valid @RequestBody MfaVerificationRequest request) {
        return ResponseEntity.ok(authService.verifyMfa(request));
    }

    private String clientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        passwordResetService.generateResetOtp(request.getPhoneNumber());
        return ResponseEntity.ok(Map.of("message", "OTP sent successfully"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.getPhoneNumber(), request.getOtp(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }
}