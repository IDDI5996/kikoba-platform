package com.kikoba.kikoba_backend.auth.service;

import com.kikoba.kikoba_backend.auth.dto.InitiateRegistrationRequest;
import com.kikoba.kikoba_backend.auth.dto.LoginInitiateResponse;
import com.kikoba.kikoba_backend.auth.dto.MfaVerificationRequest;
import com.kikoba.kikoba_backend.auth.dto.RegistrationProgressResponse;
import com.kikoba.kikoba_backend.auth.dto.VerifyOtpRequest;
import com.kikoba.kikoba_backend.user.dto.AuthResponse;
import com.kikoba.kikoba_backend.user.dto.LoginRequest;
import com.kikoba.kikoba_backend.user.dto.RegisterRequest;

public interface AuthService {

    // ---- Legacy methods (kept for backward compatibility) ----
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);

    // ---- New secure registration flow (OTP-based) ----
    RegistrationProgressResponse initiateRegistration(InitiateRegistrationRequest request, String ipAddress);
    RegistrationProgressResponse verifyRegistrationPhone(VerifyOtpRequest request);
    RegistrationProgressResponse verifyRegistrationEmail(VerifyOtpRequest request);
    AuthResponse completeRegistration(VerifyOtpRequest request);
    RegistrationProgressResponse resendRegistrationOtp(String recipient, String purpose);

    // ---- New secure login flow (with optional MFA) ----
    LoginInitiateResponse initiateLogin(LoginRequest request, String ipAddress);
    AuthResponse verifyMfa(MfaVerificationRequest request);
}