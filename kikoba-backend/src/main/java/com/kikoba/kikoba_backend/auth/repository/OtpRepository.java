package com.kikoba.kikoba_backend.auth.repository;

import com.kikoba.kikoba_backend.auth.entity.OtpCode;
import com.kikoba.kikoba_backend.auth.enums.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpRepository extends JpaRepository<OtpCode, UUID> {

    @Query("""
        SELECT o FROM OtpCode o
        WHERE o.recipient = :recipient
          AND o.purpose = :purpose
          AND o.isUsed = false
          AND o.expiresAt > :now
        ORDER BY o.createdAt DESC
    """)
    List<OtpCode> findActiveByRecipient(
            @Param("recipient") String recipient,
            @Param("purpose") OtpPurpose purpose,
            @Param("now") LocalDateTime now
    );

    @Query("""
        SELECT COUNT(o) FROM OtpCode o
        WHERE o.recipient = :recipient
          AND o.purpose = :purpose
          AND o.createdAt > :since
    """)
    long countRecentByRecipient(
            @Param("recipient") String recipient,
            @Param("purpose") OtpPurpose purpose,
            @Param("since") LocalDateTime since
    );

    @Modifying
    @Query("""
        UPDATE OtpCode o SET o.isUsed = true, o.usedAt = :now
        WHERE o.recipient = :recipient
          AND o.purpose = :purpose
          AND o.isUsed = false
    """)
    int invalidateAllActive(
            @Param("recipient") String recipient,
            @Param("purpose") OtpPurpose purpose,
            @Param("now") LocalDateTime now
    );

    @Modifying
    @Query("DELETE FROM OtpCode o WHERE o.expiresAt < :cutoff")
    int deleteExpiredBefore(@Param("cutoff") LocalDateTime cutoff);
}