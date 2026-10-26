package com.kikoba.kikoba_backend.notification.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailProvider {

    /**
     * ObjectProvider makes JavaMailSender OPTIONAL.
     * If SMTP is not configured, this will be null and we fall back to logging.
     */
    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${email.from:no-reply@kikobaconnect.com}")
    private String fromAddress;

    @Value("${notification.email.enabled:false}")
    private boolean enabled;

    public EmailResult send(String toEmail, String subject, String body) {
        if (!enabled) {
            log.info("[EMAIL-DISABLED] Would send to {}: {} - {}", toEmail, subject, body);
            return EmailResult.success("EMAIL-DISABLED-" + System.currentTimeMillis());
        }

        if (toEmail == null || toEmail.isBlank()) {
            return EmailResult.failure("Recipient email is empty");
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.warn("[EMAIL] JavaMailSender not configured. Enable SMTP or set notification.email.enabled=false");
            return EmailResult.failure("Email provider not configured");
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);

            log.info("[EMAIL] Sent to {}: {}", toEmail, subject);
            return EmailResult.success("SMTP-" + System.currentTimeMillis());

        } catch (Exception e) {
            log.error("[EMAIL] Failed to send to {}: {}", toEmail, e.getMessage());
            return EmailResult.failure(e.getMessage());
        }
    }

    public record EmailResult(boolean success, String reference, String error) {
        public static EmailResult success(String ref) {
            return new EmailResult(true, ref, null);
        }
        public static EmailResult failure(String error) {
            return new EmailResult(false, null, error);
        }
    }

    /**
     * Send an OTP code via email using a standard template.
     */
    public EmailResult sendOtp(String toEmail, String otpCode) {
        String subject = "KikobaConnect — Verification Code";
        String body = "Your KikobaConnect verification code is: " + otpCode
                + "\n\nThis code expires in 5 minutes."
                + "\nIf you did not request this, ignore this email.";
        return send(toEmail, subject, body);
    }
}