package com.kikoba.kikoba_backend.auth.entity;

import com.kikoba.kikoba_backend.auth.enums.OtpPurpose;
import com.kikoba.kikoba_backend.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "otp_codes", indexes = {
        @Index(name = "idx_otp_recipient", columnList = "recipient"),
        @Index(name = "idx_otp_purpose", columnList = "purpose"),
        @Index(name = "idx_otp_expires_at", columnList = "expires_at"),
        @Index(name = "idx_otp_is_used", columnList = "is_used")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpCode extends BaseEntity {

    @Column(name = "recipient", nullable = false, length = 255)
    private String recipient;

    @Column(name = "code_hash", nullable = false, length = 255)
    private String codeHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", nullable = false, length = 30)
    private OtpPurpose purpose;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "attempts")
    @Builder.Default
    private Integer attempts = 0;

    @Column(name = "is_used")
    @Builder.Default
    private Boolean isUsed = false;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;
}