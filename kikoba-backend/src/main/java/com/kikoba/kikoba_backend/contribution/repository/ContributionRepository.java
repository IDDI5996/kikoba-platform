package com.kikoba.kikoba_backend.contribution.repository;

import com.kikoba.kikoba_backend.contribution.entity.Contribution;
import com.kikoba.kikoba_backend.contribution.enums.ContributionStatus;
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
public interface ContributionRepository extends JpaRepository<Contribution, UUID> {

    // ---- Simple queries ----

    List<Contribution> findByGroupMemberId(UUID groupMemberId);

    List<Contribution> findByGroupMemberIdAndStatus(UUID groupMemberId, ContributionStatus status);

    boolean existsByTransactionReference(String transactionReference);

    long countByGroupMemberIdAndStatus(UUID groupMemberId, ContributionStatus status);

    // ---- JOIN FETCH queries (Golden Rule #2) ----

    /**
     * Fetch a contribution with all related data for DTO mapping.
     */
    @Query("""
        SELECT c FROM Contribution c
        JOIN FETCH c.groupMember gm
        JOIN FETCH gm.user u
        JOIN FETCH gm.group g
        WHERE c.id = :id
    """)
    Optional<Contribution> findByIdWithDetails(@Param("id") UUID id);

    /**
     * Fetch all contributions of a member with group loaded.
     */
    @Query("""
        SELECT c FROM Contribution c
        JOIN FETCH c.groupMember gm
        JOIN FETCH gm.group g
        JOIN FETCH gm.user u
        WHERE gm.user.id = :userId
        ORDER BY c.createdAt DESC
    """)
    List<Contribution> findAllByUserIdWithDetails(@Param("userId") UUID userId);

    /**
     * Fetch all contributions of a member in a specific group.
     */
    @Query("""
        SELECT c FROM Contribution c
        JOIN FETCH c.groupMember gm
        JOIN FETCH gm.user u
        JOIN FETCH gm.group g
        WHERE gm.user.id = :userId AND gm.group.id = :groupId
        ORDER BY c.createdAt DESC
    """)
    List<Contribution> findAllByUserIdAndGroupId(
            @Param("userId") UUID userId,
            @Param("groupId") UUID groupId
    );

    /**
     * Fetch all contributions of a group (for Treasurer/Chairperson).
     */
    @Query("""
        SELECT c FROM Contribution c
        JOIN FETCH c.groupMember gm
        JOIN FETCH gm.user u
        JOIN FETCH gm.group g
        WHERE gm.group.id = :groupId
        ORDER BY c.createdAt DESC
    """)
    List<Contribution> findAllByGroupIdWithDetails(@Param("groupId") UUID groupId);

    /**
     * Fetch all contributions of a group by status.
     */
    @Query("""
        SELECT c FROM Contribution c
        JOIN FETCH c.groupMember gm
        JOIN FETCH gm.user u
        JOIN FETCH gm.group g
        WHERE gm.group.id = :groupId AND c.status = :status
        ORDER BY c.createdAt DESC
    """)
    List<Contribution> findAllByGroupIdAndStatus(
            @Param("groupId") UUID groupId,
            @Param("status") ContributionStatus status
    );

    // ---- Aggregations ----

    /**
     * Sum of completed contributions for a member in a group.
     */
    @Query("""
        SELECT COALESCE(SUM(c.amount), 0) FROM Contribution c
        WHERE c.groupMember.id = :memberId AND c.status = 'COMPLETED'
    """)
    BigDecimal sumCompletedByMemberId(@Param("memberId") UUID memberId);

    /**
     * Sum of completed contributions for an entire group.
     */
    @Query("""
        SELECT COALESCE(SUM(c.amount), 0) FROM Contribution c
        JOIN c.groupMember gm
        WHERE gm.group.id = :groupId AND c.status = 'COMPLETED'
    """)
    BigDecimal sumCompletedByGroupId(@Param("groupId") UUID groupId);

    /**
     * Sum of completed contributions for a specific period.
     */
    @Query("""
        SELECT COALESCE(SUM(c.amount), 0) FROM Contribution c
        JOIN c.groupMember gm
        WHERE gm.group.id = :groupId
          AND c.status = 'COMPLETED'
          AND c.contributionPeriod = :period
    """)
    BigDecimal sumCompletedByGroupAndPeriod(
            @Param("groupId") UUID groupId,
            @Param("period") LocalDate period
    );
}