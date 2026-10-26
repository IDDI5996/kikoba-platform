package com.kikoba.kikoba_backend.loan.repository;

import com.kikoba.kikoba_backend.loan.entity.Loan;
import com.kikoba.kikoba_backend.loan.enums.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoanRepository extends JpaRepository<Loan, UUID> {

    // ---- Simple queries ----
    List<Loan> findByGroupMemberId(UUID groupMemberId);
    List<Loan> findByGroupMemberIdAndStatus(UUID groupMemberId, LoanStatus status);
    long countByGroupMemberIdAndStatus(UUID groupMemberId, LoanStatus status);

    // ---- JOIN FETCH queries ----
    @Query("""
        SELECT l FROM Loan l
        JOIN FETCH l.groupMember gm
        JOIN FETCH gm.user u
        JOIN FETCH gm.group g
        LEFT JOIN FETCH l.approvedBy
        WHERE l.id = :id
    """)
    Optional<Loan> findByIdWithDetails(@Param("id") UUID id);

    @Query("""
        SELECT l FROM Loan l
        JOIN FETCH l.groupMember gm
        JOIN FETCH gm.user u
        JOIN FETCH gm.group g
        LEFT JOIN FETCH l.approvedBy
        WHERE gm.user.id = :userId
        ORDER BY l.createdAt DESC
    """)
    List<Loan> findAllByUserIdWithDetails(@Param("userId") UUID userId);

    @Query("""
        SELECT l FROM Loan l
        JOIN FETCH l.groupMember gm
        JOIN FETCH gm.user u
        JOIN FETCH gm.group g
        LEFT JOIN FETCH l.approvedBy
        WHERE gm.group.id = :groupId
        ORDER BY l.createdAt DESC
    """)
    List<Loan> findAllByGroupIdWithDetails(@Param("groupId") UUID groupId);

    @Query("""
        SELECT l FROM Loan l
        JOIN FETCH l.groupMember gm
        JOIN FETCH gm.user u
        JOIN FETCH gm.group g
        LEFT JOIN FETCH l.approvedBy
        WHERE gm.group.id = :groupId AND l.status = :status
        ORDER BY l.createdAt DESC
    """)
    List<Loan> findAllByGroupIdAndStatus(
            @Param("groupId") UUID groupId,
            @Param("status") LoanStatus status
    );

    /**
     * Find active loans with overdue repayments (for scheduled tasks).
     */
    @Query("""
        SELECT l FROM Loan l
        WHERE l.status = 'ACTIVE'
        AND l.balance > 0
    """)
    List<Loan> findActiveLoans();

    // ---- Aggregations ----
    @Query("""
        SELECT COALESCE(SUM(l.amount), 0) FROM Loan l
        JOIN l.groupMember gm
        WHERE gm.group.id = :groupId AND l.status IN ('ACTIVE', 'PAID')
    """)
    BigDecimal sumDisbursedByGroupId(@Param("groupId") UUID groupId);

    @Query("""
        SELECT COALESCE(SUM(l.balance), 0) FROM Loan l
        JOIN l.groupMember gm
        WHERE gm.group.id = :groupId AND l.status = 'ACTIVE'
    """)
    BigDecimal sumOutstandingBalanceByGroupId(@Param("groupId") UUID groupId);

    @Query("""
        SELECT COALESCE(SUM(l.totalRepayable - l.amount), 0) FROM Loan l
        JOIN l.groupMember gm
        WHERE gm.group.id = :groupId AND l.status = 'PAID'
    """)
    BigDecimal sumInterestEarnedByGroupId(@Param("groupId") UUID groupId);

    @Query("""
        SELECT COUNT(l) FROM Loan l
        JOIN l.groupMember gm
        WHERE gm.group.id = :groupId AND l.status = :status
    """)
    long countByGroupIdAndStatus(@Param("groupId") UUID groupId, @Param("status") LoanStatus status);
}