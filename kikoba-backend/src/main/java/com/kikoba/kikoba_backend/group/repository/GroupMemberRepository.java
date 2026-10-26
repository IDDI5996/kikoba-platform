package com.kikoba.kikoba_backend.group.repository;

import com.kikoba.kikoba_backend.group.entity.GroupMember;
import com.kikoba.kikoba_backend.group.enums.GroupRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, UUID> {

    // ---- Simple queries (no join fetch) ----

    List<GroupMember> findByGroupId(UUID groupId);

    List<GroupMember> findByGroupIdAndIsActiveTrue(UUID groupId);

    List<GroupMember> findByUserId(UUID userId);

    List<GroupMember> findByUserIdAndIsActiveTrue(UUID userId);

    Optional<GroupMember> findByGroupIdAndUserId(UUID groupId, UUID userId);

    boolean existsByGroupIdAndUserId(UUID groupId, UUID userId);

    long countByGroupIdAndIsActiveTrue(UUID groupId);

    long countByGroupIdAndRoleAndIsActiveTrue(UUID groupId, GroupRole role);

    // ---- JOIN FETCH queries (fix LazyInitializationException) ----

    /**
     * Fetch memberships for a user with EAGER group + creator loaded.
     * Prevents LazyInitializationException when mapping to DTO.
     */
    @Query("""
        SELECT gm FROM GroupMember gm
        JOIN FETCH gm.group g
        LEFT JOIN FETCH g.createdBy
        WHERE gm.user.id = :userId AND gm.isActive = true
    """)
    List<GroupMember> findByUserIdWithGroup(@Param("userId") UUID userId);

    /**
     * Fetch all active members of a group with EAGER user loaded.
     */
    @Query("""
        SELECT gm FROM GroupMember gm
        JOIN FETCH gm.user u
        WHERE gm.group.id = :groupId AND gm.isActive = true
    """)
    List<GroupMember> findByGroupIdWithUser(@Param("groupId") UUID groupId);

    /**
     * Fetch a single membership with user and group loaded.
     */
    @Query("""
        SELECT gm FROM GroupMember gm
        JOIN FETCH gm.user u
        JOIN FETCH gm.group g
        LEFT JOIN FETCH g.createdBy
        WHERE gm.group.id = :groupId AND gm.user.id = :userId
    """)
    Optional<GroupMember> findByGroupIdAndUserIdWithDetails(
            @Param("groupId") UUID groupId,
            @Param("userId") UUID userId
    );

    /**
     * Fetch a single membership by ID with user loaded.
     */
    @Query("""
        SELECT gm FROM GroupMember gm
        JOIN FETCH gm.user u
        JOIN FETCH gm.group g
        LEFT JOIN FETCH g.createdBy
        WHERE gm.id = :memberId
    """)
    Optional<GroupMember> findByIdWithDetails(@Param("memberId") UUID memberId);
}