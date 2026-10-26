package com.kikoba.kikoba_backend.dividend.repository;

import com.kikoba.kikoba_backend.dividend.entity.Dividend;
import com.kikoba.kikoba_backend.dividend.enums.DividendStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DividendRepository extends JpaRepository<Dividend, UUID> {

    Optional<Dividend> findByGroupIdAndCycleNumber(UUID groupId, Integer cycleNumber);

    boolean existsByGroupIdAndCycleNumber(UUID groupId, Integer cycleNumber);

    List<Dividend> findByGroupIdOrderByCycleNumberDesc(UUID groupId);

    long countByGroupIdAndStatus(UUID groupId, DividendStatus status);

    // ---- JOIN FETCH queries ----
    @Query("""
        SELECT d FROM Dividend d
        JOIN FETCH d.group g
        LEFT JOIN FETCH d.calculatedBy
        LEFT JOIN FETCH d.approvedBy
        LEFT JOIN FETCH d.distributedBy
        WHERE d.id = :id
    """)
    Optional<Dividend> findByIdWithDetails(@Param("id") UUID id);

    @Query("""
        SELECT d FROM Dividend d
        JOIN FETCH d.group g
        LEFT JOIN FETCH d.calculatedBy
        LEFT JOIN FETCH d.approvedBy
        LEFT JOIN FETCH d.distributedBy
        WHERE d.group.id = :groupId
        ORDER BY d.cycleNumber DESC
    """)
    List<Dividend> findAllByGroupIdWithDetails(@Param("groupId") UUID groupId);

    /**
     * Find the maximum cycle number for a group.
     */
    @Query("SELECT COALESCE(MAX(d.cycleNumber), 0) FROM Dividend d WHERE d.group.id = :groupId")
    Integer findMaxCycleNumber(@Param("groupId") UUID groupId);
}