package com.kikoba.kikoba_backend.auth.service.impl;

import com.kikoba.kikoba_backend.auth.dto.InitiateRegistrationRequest;
import com.kikoba.kikoba_backend.auth.dto.LoginInitiateResponse;
import com.kikoba.kikoba_backend.auth.dto.MfaVerificationRequest;
import com.kikoba.kikoba_backend.auth.dto.RegistrationProgressResponse;
import com.kikoba.kikoba_backend.auth.dto.VerifyOtpRequest;
import com.kikoba.kikoba_backend.auth.enums.OtpPurpose;
import com.kikoba.kikoba_backend.auth.service.AuthService;
import com.kikoba.kikoba_backend.auth.service.OtpService;
import com.kikoba.kikoba_backend.exception.BadRequestException;
import com.kikoba.kikoba_backend.exception.InvalidCredentialsException;
import com.kikoba.kikoba_backend.exception.ResourceAlreadyExistsException;
import com.kikoba.kikoba_backend.exception.ResourceNotFoundException;
import com.kikoba.kikoba_backend.notification.util.EmailProvider;
import com.kikoba.kikoba_backend.notification.util.SmsProvider;
import com.kikoba.kikoba_backend.security.JwtService;
import com.kikoba.kikoba_backend.user.dto.AuthResponse;
import com.kikoba.kikoba_backend.user.dto.LoginRequest;
import com.kikoba.kikoba_backend.user.dto.RegisterRequest;
import com.kikoba.kikoba_backend.user.dto.UserResponse;
import com.kikoba.kikoba_backend.user.entity.User;
import com.kikoba.kikoba_backend.user.mapper.UserMapper;
import com.kikoba.kikoba_backend.user.repository.UserRepository;
import com.kikoba.kikoba_backend.user.service.UserService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private static final String STATUS_PENDING         = "PENDING";
    private static final String STATUS_PHONE_VERIFIED  = "PHONE_VERIFIED";
    private static final String STATUS_EMAIL_VERIFIED  = "EMAIL_VERIFIED";
    private static final String STATUS_COMPLETE        = "COMPLETE";

    private final UserRepository userRepository;
    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final OtpService otpService;
    private final SmsProvider smsProvider;
    private final EmailProvider emailProvider;

    // ============================================================
    // LEGACY METHODS (kept for backward compatibility)
    // ============================================================

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        UserResponse userResponse = userService.registerUser(request);

        User user = userRepository.findById(userResponse.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found after registration"));

        // Mark as fully verified in the legacy path so login still works
        user.setRegistrationStatus(STATUS_COMPLETE);
        user.setPhoneVerified(true);
        user.setEmailVerified(true);
        user.setPhoneVerifiedAt(LocalDateTime.now());
        if (user.getEmail() != null) {
            user.setEmailVerifiedAt(LocalDateTime.now());
        }
        userRepository.save(user);

        return buildAuthResponse(user, userResponse);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = findUserByIdentifier(request.getIdentifier());

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid phone/email or password");
        }
        if (!user.getIsActive()) {
            throw new InvalidCredentialsException("Account is disabled. Please contact support.");
        }

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        log.info("User logged in (legacy path): {}", user.getId());
        return buildAuthResponse(user, userMapper.toResponse(user));
    }

    // ============================================================
    // NEW REGISTRATION FLOW
    // ============================================================

    @Override
    @Transactional
    public RegistrationProgressResponse initiateRegistration(InitiateRegistrationRequest request,
                                                             String ipAddress) {
        log.info("Initiating registration for phone={}", mask(request.getPhoneNumber()));

        // ---- Check if phone already exists ----
        Optional<User> existingOpt = userRepository.findByPhoneNumber(request.getPhoneNumber());

        if (existingOpt.isPresent()) {
            User existing = existingOpt.get();

            // Fully registered → user must login
            if (STATUS_COMPLETE.equals(existing.getRegistrationStatus())) {
                throw new ResourceAlreadyExistsException("Phone number already registered. Please login.");
            }

            // ---- Email uniqueness check FIRST (before updating) ----
            if (request.getEmail() != null && !request.getEmail().isBlank()
                    && !request.getEmail().equalsIgnoreCase(existing.getEmail())
                    && userRepository.existsByEmail(request.getEmail())) {
                throw new ResourceAlreadyExistsException("Email already registered to another account");
            }

            // ---- RESUME FLOW: user already verified phone, same phone, keep verification ----
            boolean canResume = Boolean.TRUE.equals(existing.getPhoneVerified())
                    && existing.getPhoneNumber().equals(request.getPhoneNumber());

            if (canResume) {
                log.info("Resuming registration for phone={}", mask(request.getPhoneNumber()));

                String oldEmail = existing.getEmail();
                existing.setFirstName(request.getFirstName());
                existing.setLastName(request.getLastName());
                existing.setEmail(request.getEmail());
                existing.setPasswordHash(passwordEncoder.encode(request.getPassword()));

                // If email changed → reset email verification
                if (request.getEmail() == null
                        || !request.getEmail().equalsIgnoreCase(oldEmail)) {
                    existing.setEmailVerified(false);
                    existing.setEmailVerifiedAt(null);
                }

                userRepository.save(existing);

                // Send email OTP if an email is provided
                if (request.getEmail() != null && !request.getEmail().isBlank()) {
                    sendEmailOtp(existing.getEmail());
                    return buildProgressResponse(existing, "EMAIL_VERIFICATION_SENT");
                }

                // No email → mark complete immediately
                existing.setRegistrationStatus(STATUS_COMPLETE);
                userRepository.save(existing);
                return buildProgressResponse(existing, "PHONE_VERIFIED");
            }

            // ---- Fresh restart: user wants to change phone or was not yet verified ----
            updatePendingUser(existing, request);
            sendPhoneOtp(existing.getPhoneNumber(), ipAddress);
            return buildProgressResponse(existing, "PHONE_VERIFICATION_SENT");
        }

        // ---- Brand new user ----
        if (request.getEmail() != null && !request.getEmail().isBlank()
                && userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Email already registered");
        }

        User user = User.builder()
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .preferredLanguage("sw")
                .isActive(true)
                .kycVerified(false)
                .mfaEnabled(false)
                .phoneVerified(false)
                .emailVerified(request.getEmail() == null)
                .registrationStatus(STATUS_PENDING)
                .build();

        user = userRepository.save(user);
        sendPhoneOtp(user.getPhoneNumber(), ipAddress);

        log.info("Pending user created: {}", user.getId());
        return buildProgressResponse(user, "PHONE_VERIFICATION_SENT");
    }

    @Override
    @Transactional
    public RegistrationProgressResponse verifyRegistrationPhone(VerifyOtpRequest request) {
        log.info("Verifying phone OTP for {}", mask(request.getRecipient()));

        User user = userRepository.findByPhoneNumber(request.getRecipient())
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found"));

        if (STATUS_COMPLETE.equals(user.getRegistrationStatus())) {
            throw new BadRequestException("This account is already verified");
        }

        // Verify the OTP — throws BadRequestException on mismatch
        otpService.verify(request.getRecipient(), OtpPurpose.PHONE_VERIFICATION, request.getCode());

        user.setPhoneVerified(true);
        user.setPhoneVerifiedAt(LocalDateTime.now());

        // If email is required and not yet verified → advance to email step
        if (user.getEmail() != null && !Boolean.TRUE.equals(user.getEmailVerified())) {
            user.setRegistrationStatus(STATUS_PHONE_VERIFIED);
            userRepository.save(user);

            sendEmailOtp(user.getEmail());

            return buildProgressResponse(user, "PHONE_VERIFIED");
        }

        // Otherwise, we're done — mark complete
        user.setRegistrationStatus(STATUS_COMPLETE);
        user.setEmailVerified(true);
        userRepository.save(user);

        return buildProgressResponse(user, "PHONE_VERIFIED");
    }

    @Override
    @Transactional
    public RegistrationProgressResponse verifyRegistrationEmail(VerifyOtpRequest request) {
        log.info("Verifying email OTP for {}", mask(request.getRecipient()));

        User user = userRepository.findByEmail(request.getRecipient())
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found"));

        if (!Boolean.TRUE.equals(user.getPhoneVerified())) {
            throw new BadRequestException("Please verify your phone number first");
        }

        if (!STATUS_PHONE_VERIFIED.equals(user.getRegistrationStatus())) {
            throw new BadRequestException("Email verification is not expected at this stage");
        }

        otpService.verify(request.getRecipient(), OtpPurpose.EMAIL_VERIFICATION, request.getCode());

        user.setEmailVerified(true);
        user.setEmailVerifiedAt(LocalDateTime.now());
        user.setRegistrationStatus(STATUS_COMPLETE);
        userRepository.save(user);

        log.info("Registration complete: {}", user.getId());

        return buildProgressResponse(user, "EMAIL_VERIFIED");
    }

    @Override
    @Transactional
    public AuthResponse completeRegistration(VerifyOtpRequest request) {
        // This is a convenience method — it assumes both verifications are done
        // (used if the client sends one final call instead of two)
        User user = userRepository.findByPhoneNumber(request.getRecipient())
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found"));

        if (!STATUS_COMPLETE.equals(user.getRegistrationStatus())) {
            throw new BadRequestException("Please complete phone and email verification first");
        }

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        return buildAuthResponse(user, userMapper.toResponse(user));
    }

    @Override
    @Transactional
    public RegistrationProgressResponse resendRegistrationOtp(String recipient, String purpose) {
        log.info("Resending OTP for {} purpose={}", mask(recipient), purpose);

        OtpPurpose otpPurpose;
        try {
            otpPurpose = OtpPurpose.valueOf(purpose);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid purpose");
        }

        if (otpPurpose == OtpPurpose.PHONE_VERIFICATION) {
            User user = userRepository.findByPhoneNumber(recipient)
                    .orElseThrow(() -> new ResourceNotFoundException("Registration not found"));
            sendPhoneOtp(user.getPhoneNumber(), null);
            return buildProgressResponse(user, "PHONE_VERIFICATION_SENT");
        }

        if (otpPurpose == OtpPurpose.EMAIL_VERIFICATION) {
            User user = userRepository.findByEmail(recipient)
                    .orElseThrow(() -> new ResourceNotFoundException("Registration not found"));
            sendEmailOtp(user.getEmail());
            return buildProgressResponse(user, "EMAIL_VERIFICATION_SENT");
        }

        throw new BadRequestException("Unsupported purpose for resend");
    }

    // ============================================================
    // NEW LOGIN FLOW (WITH MFA)
    // ============================================================

    @Override
    @Transactional
    public LoginInitiateResponse initiateLogin(LoginRequest request, String ipAddress) {
        log.info("Initiating login for {}", mask(request.getIdentifier()));

        User user = findUserByIdentifier(request.getIdentifier());

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid phone/email or password");
        }
        if (!user.getIsActive()) {
            throw new InvalidCredentialsException("Account is disabled. Please contact support.");
        }
        if (!STATUS_COMPLETE.equals(user.getRegistrationStatus())) {
            throw new BadRequestException("Please complete your registration first");
        }

        // ---- If MFA is enabled → return a session token ----
        if (Boolean.TRUE.equals(user.getMfaEnabled())) {
            String sessionToken = jwtService.generateMfaSessionToken(user.getId());

            // Send MFA code via SMS
            String code = otpService.generateAndSave(
                    user.getPhoneNumber(), OtpPurpose.LOGIN_MFA, ipAddress);
            smsProvider.send(user.getPhoneNumber(),
                    "KikobaConnect login code: " + code + ". Expires in 5 minutes.");

            log.info("MFA challenge issued for user {}", user.getId());

            return LoginInitiateResponse.builder()
                    .mfaRequired(true)
                    .sessionToken(sessionToken)
                    .maskedPhone(mask(user.getPhoneNumber()))
                    .mfaMethod("SMS")
                    .build();
        }

        // ---- Otherwise: login completes immediately ----
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        AuthResponse auth = buildAuthResponse(user, userMapper.toResponse(user));

        log.info("User logged in (no MFA): {}", user.getId());

        return LoginInitiateResponse.builder()
                .mfaRequired(false)
                .accessToken(auth.getAccessToken())
                .refreshToken(auth.getRefreshToken())
                .tokenType(auth.getTokenType())
                .expiresIn(auth.getExpiresIn())
                .user(auth.getUser())
                .build();
    }

    @Override
    @Transactional
    public AuthResponse verifyMfa(MfaVerificationRequest request) {
        UUID userId = jwtService.extractUserIdFromMfaToken(request.getSessionToken());
        if (userId == null) {
            throw new InvalidCredentialsException("Invalid or expired session. Please login again.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Verify the OTP
        otpService.verify(user.getPhoneNumber(), OtpPurpose.LOGIN_MFA, request.getCode());

        // Update last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        log.info("MFA verified for user {}", user.getId());

        return buildAuthResponse(user, userMapper.toResponse(user));
    }

    // ============================================================
    // PRIVATE HELPERS
    // ============================================================

    private User findUserByIdentifier(String identifier) {
        return userRepository.findByPhoneNumber(identifier)
                .orElseGet(() -> userRepository.findByEmail(identifier)
                        .orElseThrow(() -> new InvalidCredentialsException(
                                "Invalid phone/email or password")));
    }

    private void updatePendingUser(User user, InitiateRegistrationRequest request) {
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRegistrationStatus(STATUS_PENDING);
        user.setPhoneVerified(false);
        user.setEmailVerified(request.getEmail() == null);
        userRepository.save(user);
    }

    private void sendPhoneOtp(String phoneNumber, String ipAddress) {
        String code = otpService.generateAndSave(
                phoneNumber, OtpPurpose.PHONE_VERIFICATION, ipAddress);

        String message = "KikobaConnect: Your verification code is " + code
                + ". Expires in 5 minutes. Do not share this code.";
        SmsProvider.SmsResult result = smsProvider.send(phoneNumber, message);

        if (!result.success()) {
            log.warn("Failed to send phone OTP to {}: {}", mask(phoneNumber), result.error());
        }
    }

    private void sendEmailOtp(String email) {
        if (email == null || email.isBlank()) return;

        String code = otpService.generateAndSave(
                email, OtpPurpose.EMAIL_VERIFICATION, null);

        String subject = "KikobaConnect — Verification Code";
        String body = "Your KikobaConnect verification code is: " + code
                + "\n\nThis code expires in 5 minutes."
                + "\nIf you did not request this, ignore this email.";

        EmailProvider.EmailResult result = emailProvider.send(email, subject, body);

        if (!result.success()) {
            log.warn("Failed to send email OTP to {}: {}", mask(email), result.error());
        }
    }

    private AuthResponse buildAuthResponse(User user, UserResponse userResponse) {
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getPhoneNumber());
        String refreshToken = jwtService.generateRefreshToken(user.getId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpiration() / 1000)
                .user(userResponse)
                .build();
    }

    private RegistrationProgressResponse buildProgressResponse(User user, String status) {
        boolean emailRequired = user.getEmail() != null && !user.getEmail().isBlank();
        boolean emailPending  = emailRequired && !Boolean.TRUE.equals(user.getEmailVerified());
        boolean phonePending  = !Boolean.TRUE.equals(user.getPhoneVerified());

        String nextStep;
        if (STATUS_COMPLETE.equals(user.getRegistrationStatus())) {
            nextStep = "COMPLETE";
        } else if (phonePending) {
            nextStep = "VERIFY_PHONE";
        } else if (emailPending) {
            nextStep = "VERIFY_EMAIL";
        } else {
            nextStep = "COMPLETE";
        }

        return RegistrationProgressResponse.builder()
                .status(status)
                .nextStep(nextStep)
                .maskedPhone(mask(user.getPhoneNumber()))
                .maskedEmail(emailRequired ? maskEmail(user.getEmail()) : null)
                .emailRequired(emailRequired)
                .codeExpiresInSeconds(300)
                .build();
    }

    private String mask(String value) {
        if (value == null || value.length() < 5) return "***";
        return value.substring(0, 3) + "***" + value.substring(value.length() - 2);
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        int at = email.indexOf("@");
        if (at < 3) return "***" + email.substring(at);
        return email.substring(0, 3) + "***" + email.substring(at);
    }
}