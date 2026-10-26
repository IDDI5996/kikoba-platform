package com.kikoba.kikoba_backend.dividend.repository;

import com.kikoba.kikoba_backend.dividend.entity.DividendPayout;
import com.kikoba.kikoba_backend.dividend.enums.PayoutStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DividendPayoutRepository extends JpaRepository<DividendPayout, UUID> {

    List<DividendPayout> findByDividendIdOrderByAmountDesc(UUID dividendId);

    List<DividendPayout> findByGroupMemberIdOrderByCreatedAtDesc(UUID groupMemberId);

    boolean existsByDividendIdAndGroupMemberId(UUID dividendId, UUID groupMemberId);

    long countByDividendIdAndStatus(UUID dividendId, PayoutStatus status);

    // ---- JOIN FETCH queries ----
    @Query("""
        SELECT p FROM DividendPayout p
        JOIN FETCH p.dividend d
        JOIN FETCH d.group g
        JOIN FETCH p.groupMember gm
        JOIN FETCH gm.user u
        WHERE p.id = :id
    """)
    Optional<DividendPayout> findByIdWithDetails(@Param("id") UUID id);

    @Query("""
        SELECT p FROM DividendPayout p
        JOIN FETCH p.dividend d
        JOIN FETCH p.groupMember gm
        JOIN FETCH gm.user u
        WHERE d.id = :dividendId
        ORDER BY p.amount DESC
    """)
    List<DividendPayout> findAllByDividendIdWithDetails(@Param("dividendId") UUID dividendId);

    @Query("""
        SELECT p FROM DividendPayout p
        JOIN FETCH p.dividend d
        JOIN FETCH d.group g
        JOIN FETCH p.groupMember gm
        JOIN FETCH gm.user u
        WHERE gm.user.id = :userId
        ORDER BY d.cycleNumber DESC
    """)
    List<DividendPayout> findAllByUserIdWithDetails(@Param("userId") UUID userId);

    /**
     * Sum of payouts for a dividend (for verification).
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM DividendPayout p WHERE p.dividend.id = :dividendId")
    BigDecimal sumAmountByDividendId(@Param("dividendId") UUID dividendId);

    /**
     * Sum of paid-out amounts for a dividend.
     */
    @Query("""
        SELECT COALESCE(SUM(p.amount), 0) FROM DividendPayout p
        WHERE p.dividend.id = :dividendId AND p.status = 'PAID'
    """)
    BigDecimal sumPaidByDividendId(@Param("dividendId") UUID dividendId);
}