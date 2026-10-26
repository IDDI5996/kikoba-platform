package com.kikoba.kikoba_backend.share.repository;

import com.kikoba.kikoba_backend.share.entity.Share;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShareRepository extends JpaRepository<Share, UUID> {

    List<Share> findByGroupMemberIdOrderByCreatedAtDesc(UUID groupMemberId);

    List<Share> findByGroupIdOrderByCreatedAtDesc(UUID groupId);

    // ---- JOIN FETCH queries ----
    @Query("""
        SELECT s FROM Share s
        JOIN FETCH s.groupMember gm
        JOIN FETCH gm.user u
        JOIN FETCH gm.group g
        WHERE s.id = :id
    """)
    Optional<Share> findByIdWithDetails(@Param("id") UUID id);

    @Query("""
        SELECT s FROM Share s
        JOIN FETCH s.groupMember gm
        JOIN FETCH gm.user u
        JOIN FETCH gm.group g
        WHERE gm.id = :memberId
        ORDER BY s.createdAt DESC
    """)
    List<Share> findAllByMemberIdWithDetails(@Param("memberId") UUID memberId);

    @Query("""
        SELECT s FROM Share s
        JOIN FETCH s.groupMember gm
        JOIN FETCH gm.user u
        JOIN FETCH gm.group g
        WHERE s.groupId = :groupId
        ORDER BY s.createdAt DESC
    """)
    List<Share> findAllByGroupIdWithDetails(@Param("groupId") UUID groupId);

    @Query("""
        SELECT s FROM Share s
        JOIN FETCH s.groupMember gm
        JOIN FETCH gm.user u
        JOIN FETCH gm.group g
        WHERE gm.user.id = :userId
        ORDER BY s.createdAt DESC
    """)
    List<Share> findAllByUserIdWithDetails(@Param("userId") UUID userId);
}