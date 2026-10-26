package com.kikoba.kikoba_backend.auth.service.impl;

import com.kikoba.kikoba_backend.auth.entity.OtpCode;
import com.kikoba.kikoba_backend.auth.enums.OtpPurpose;
import com.kikoba.kikoba_backend.auth.repository.OtpRepository;
import com.kikoba.kikoba_backend.auth.service.OtpService;
import com.kikoba.kikoba_backend.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OtpServiceImpl implements OtpService {

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 3;
    private static final int RATE_LIMIT_PER_HOUR = 5;

    private final OtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public String generateAndSave(String recipient, OtpPurpose purpose, String ipAddress) {
        if (recipient == null || recipient.isBlank()) {
            throw new BadRequestException("Recipient is required");
        }

        // Rate limiting: max 5 OTPs per recipient per hour
        long recentCount = otpRepository.countRecentByRecipient(
                recipient, purpose, LocalDateTime.now().minusHours(1));
        if (recentCount >= RATE_LIMIT_PER_HOUR) {
            throw new BadRequestException("Too many codes requested. Please try again later.");
        }

        // Invalidate previous OTPs for this recipient + purpose
        otpRepository.invalidateAllActive(recipient, purpose, LocalDateTime.now());

        // Generate 6-digit code
        String code = generateNumericCode();

        // Hash and save
        OtpCode otp = OtpCode.builder()
                .recipient(recipient)
                .codeHash(passwordEncoder.encode(code))
                .purpose(purpose)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .attempts(0)
                .isUsed(false)
                .ipAddress(ipAddress)
                .build();

        otpRepository.save(otp);
        log.info("OTP generated for {} (purpose={}, expires in {}min)",
                maskRecipient(recipient), purpose, OTP_EXPIRY_MINUTES);

        return code;
    }

    @Override
    @Transactional
    public void verify(String recipient, OtpPurpose purpose, String code) {
        if (code == null || code.isBlank()) {
            throw new BadRequestException("Verification code is required");
        }

        List<OtpCode> active = otpRepository.findActiveByRecipient(
                recipient, purpose, LocalDateTime.now());

        if (active.isEmpty()) {
            throw new BadRequestException("Invalid or expired verification code");
        }

        // Try to match against any active code (usually 1)
        OtpCode matched = null;
        for (OtpCode otp : active) {
            if (passwordEncoder.matches(code, otp.getCodeHash())) {
                matched = otp;
                break;
            }
        }

        if (matched == null) {
            // Increment attempt on the most recent one
            OtpCode latest = active.get(0);
            latest.setAttempts(latest.getAttempts() + 1);
            if (latest.getAttempts() >= MAX_ATTEMPTS) {
                latest.setIsUsed(true);
                latest.setUsedAt(LocalDateTime.now());
                log.warn("OTP invalidated after {} failed attempts for {}",
                        MAX_ATTEMPTS, maskRecipient(recipient));
            }
            otpRepository.save(latest);

            throw new BadRequestException("Incorrect verification code");
        }

        // Mark as used
        matched.setIsUsed(true);
        matched.setUsedAt(LocalDateTime.now());
        otpRepository.save(matched);

        log.info("OTP verified for {} (purpose={})", maskRecipient(recipient), purpose);
    }

    @Override
    @Transactional
    public void invalidateAll(String recipient, OtpPurpose purpose) {
        otpRepository.invalidateAllActive(recipient, purpose, LocalDateTime.now());
    }

    @Override
    @Transactional
    public int cleanupExpired() {
        int count = otpRepository.deleteExpiredBefore(LocalDateTime.now().minusDays(1));
        if (count > 0) {
            log.info("Cleaned up {} expired OTP codes", count);
        }
        return count;
    }

    // ---- Helpers ----

    private String generateNumericCode() {
        int bound = (int) Math.pow(10, OTP_LENGTH);
        int code = secureRandom.nextInt(bound);
        return String.format("%0" + OTP_LENGTH + "d", code);
    }

    private String maskRecipient(String recipient) {
        if (recipient == null || recipient.length() < 4) return "***";
        return recipient.substring(0, 3) + "***"
                + recipient.substring(recipient.length() - 2);
    }
}