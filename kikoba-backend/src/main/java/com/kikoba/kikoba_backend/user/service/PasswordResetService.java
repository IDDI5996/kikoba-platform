package com.kikoba.kikoba_backend.user.service;

import com.kikoba.kikoba_backend.exception.BadRequestException;
import com.kikoba.kikoba_backend.exception.ResourceNotFoundException;
import com.kikoba.kikoba_backend.user.entity.PasswordResetToken;
import com.kikoba.kikoba_backend.user.entity.User;
import com.kikoba.kikoba_backend.user.repository.PasswordResetTokenRepository;
import com.kikoba.kikoba_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void generateResetOtp(String phoneNumber) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with this phone number"));

        // Generate 6 digit OTP
        String otp = String.format("%06d", new Random().nextInt(999999));

        // Delete any existing tokens for this user
        tokenRepository.deleteAll(tokenRepository.findAll().stream()
                .filter(t -> t.getUser().getId().equals(user.getId()))
                .toList());

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .otp(otp)
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(15))
                .used(false)
                .build();

        tokenRepository.save(resetToken);

        // IN PRODUCTION: Integrate Twilio or Africa's Talking here to send SMS
        log.info("==================================================");
        log.info("PASSWORD RESET OTP FOR {}: {}", phoneNumber, otp);
        log.info("==================================================");
    }

    @Transactional
    public void resetPassword(String phoneNumber, String otp, String newPassword) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        PasswordResetToken token = tokenRepository.findByOtpAndUsedFalse(otp)
                .orElseThrow(() -> new BadRequestException("Invalid OTP code"));

        if (!token.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Invalid OTP for this user");
        }

        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("OTP has expired. Please request a new one.");
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Mark token as used
        token.setUsed(true);
        tokenRepository.save(token);

        log.info("Password successfully reset for user: {}", phoneNumber);
    }
}