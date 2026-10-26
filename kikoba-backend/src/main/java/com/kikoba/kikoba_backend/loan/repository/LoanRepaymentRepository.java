package com.kikoba.kikoba_backend.loan.repository;

import com.kikoba.kikoba_backend.loan.entity.LoanRepayment;
import com.kikoba.kikoba_backend.loan.enums.RepaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoanRepaymentRepository extends JpaRepository<LoanRepayment, UUID> {

    List<LoanRepayment> findByLoanIdOrderByInstallmentNumberAsc(UUID loanId);

    Optional<LoanRepayment> findByLoanIdAndInstallmentNumber(UUID loanId, Integer installmentNumber);

    long countByLoanIdAndStatus(UUID loanId, RepaymentStatus status);

    // ---- JOIN FETCH queries ----
    @Query("""
        SELECT lr FROM LoanRepayment lr
        JOIN FETCH lr.loan l
        JOIN FETCH l.groupMember gm
        JOIN FETCH gm.user u
        JOIN FETCH gm.group g
        WHERE lr.id = :id
    """)
    Optional<LoanRepayment> findByIdWithDetails(@Param("id") UUID id);

    @Query("""
        SELECT lr FROM LoanRepayment lr
        JOIN FETCH lr.loan l
        WHERE l.id = :loanId
        ORDER BY lr.installmentNumber ASC
    """)
    List<LoanRepayment> findAllByLoanIdWithDetails(@Param("loanId") UUID loanId);

    /**
     * Find overdue repayments (for scheduled penalty task).
     */
    @Query("""
        SELECT lr FROM LoanRepayment lr
        JOIN FETCH lr.loan l
        WHERE lr.status IN ('PENDING', 'PARTIAL', 'OVERDUE')
        AND lr.dueDate < :today
        AND l.status = 'ACTIVE'
    """)
    List<LoanRepayment> findOverdueRepayments(@Param("today") LocalDate today);

    /**
     * Find next unpaid installment for a loan.
     */
    @Query("""
        SELECT lr FROM LoanRepayment lr
        WHERE lr.loan.id = :loanId
        AND lr.status IN ('PENDING', 'PARTIAL', 'OVERDUE')
        ORDER BY lr.installmentNumber ASC
        LIMIT 1
    """)
    Optional<LoanRepayment> findNextUnpaidInstallment(@Param("loanId") UUID loanId);

    /**
     * Sum of all penalties collected for a group.
     * Only counts penalties that were actually PAID by members.
     */
    @Query("""
    SELECT COALESCE(SUM(lr.penaltyPaid), 0) FROM LoanRepayment lr
    JOIN lr.loan l
    JOIN l.groupMember gm
    WHERE gm.group.id = :groupId
      AND lr.penaltyPaid > 0
""")
    BigDecimal sumPenaltiesCollectedByGroupId(@Param("groupId") UUID groupId);
}