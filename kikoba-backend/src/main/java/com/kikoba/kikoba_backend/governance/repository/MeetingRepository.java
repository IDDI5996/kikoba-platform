package com.kikoba.kikoba_backend.governance.repository;

import com.kikoba.kikoba_backend.governance.entity.Meeting;
import com.kikoba.kikoba_backend.governance.enums.MeetingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, UUID> {

    List<Meeting> findByGroupIdOrderByScheduledAtDesc(UUID groupId);

    List<Meeting> findByGroupIdAndStatusOrderByScheduledAtDesc(UUID groupId, MeetingStatus status);

    long countByGroupIdAndStatus(UUID groupId, MeetingStatus status);

    @Query("""
        SELECT m FROM Meeting m
        JOIN FETCH m.group g
        LEFT JOIN FETCH m.createdBy
        LEFT JOIN FETCH m.completedBy
        WHERE m.id = :id
    """)
    Optional<Meeting> findByIdWithDetails(@Param("id") UUID id);

    @Query("""
        SELECT m FROM Meeting m
        JOIN FETCH m.group g
        LEFT JOIN FETCH m.createdBy
        LEFT JOIN FETCH m.completedBy
        WHERE m.group.id = :groupId
        ORDER BY m.scheduledAt DESC
    """)
    List<Meeting> findAllByGroupIdWithDetails(@Param("groupId") UUID groupId);

    @Query("""
        SELECT m FROM Meeting m
        JOIN FETCH m.group g
        WHERE m.group.id = :groupId
          AND m.status IN ('SCHEDULED', 'ONGOING')
          AND m.scheduledAt >= :from
        ORDER BY m.scheduledAt ASC
    """)
    List<Meeting> findUpcomingByGroupId(
            @Param("groupId") UUID groupId,
            @Param("from") LocalDateTime from
    );
}