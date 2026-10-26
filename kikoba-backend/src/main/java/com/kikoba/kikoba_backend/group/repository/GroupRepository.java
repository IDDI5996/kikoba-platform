package com.kikoba.kikoba_backend.group.repository;

import com.kikoba.kikoba_backend.group.entity.Group;
import com.kikoba.kikoba_backend.group.enums.GroupType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupRepository extends JpaRepository<Group, UUID> {

    Optional<Group> findByInvitationCode(String invitationCode);

    boolean existsByInvitationCode(String invitationCode);

    List<Group> findByCreatedById(UUID userId);

    List<Group> findByTypeAndIsActiveTrue(GroupType type);

    List<Group> findByIsActiveTrue();

    // ---- JOIN FETCH queries ----

    /**
     * Fetch a group with the creator loaded eagerly.
     */
    @Query("""
        SELECT g FROM Group g
        LEFT JOIN FETCH g.createdBy
        WHERE g.id = :groupId
    """)
    Optional<Group> findByIdWithCreator(@Param("groupId") UUID groupId);

    /**
     * Fetch a group by invitation code with creator loaded.
     */
    @Query("""
        SELECT g FROM Group g
        LEFT JOIN FETCH g.createdBy
        WHERE g.invitationCode = :code
    """)
    Optional<Group> findByInvitationCodeWithCreator(@Param("code") String code);
}