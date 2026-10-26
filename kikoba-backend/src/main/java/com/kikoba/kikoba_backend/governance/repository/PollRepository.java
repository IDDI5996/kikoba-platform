package com.kikoba.kikoba_backend.governance.repository;

import com.kikoba.kikoba_backend.governance.entity.Poll;
import com.kikoba.kikoba_backend.governance.enums.PollStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PollRepository extends JpaRepository<Poll, UUID> {

    List<Poll> findByGroupIdOrderByCreatedAtDesc(UUID groupId);

    List<Poll> findByGroupIdAndStatusOrderByCreatedAtDesc(UUID groupId, PollStatus status);

    long countByGroupIdAndStatus(UUID groupId, PollStatus status);

    @Query("""
        SELECT p FROM Poll p
        JOIN FETCH p.group g
        LEFT JOIN FETCH p.createdBy
        WHERE p.id = :id
    """)
    Optional<Poll> findByIdWithDetails(@Param("id") UUID id);

    @Query("""
        SELECT p FROM Poll p
        JOIN FETCH p.group g
        LEFT JOIN FETCH p.createdBy
        WHERE p.group.id = :groupId
        ORDER BY p.createdAt DESC
    """)
    List<Poll> findAllByGroupIdWithDetails(@Param("groupId") UUID groupId);
}