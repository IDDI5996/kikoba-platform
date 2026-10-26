package com.kikoba.kikoba_backend.chat.repository;

import com.kikoba.kikoba_backend.chat.entity.ChatRoom;
import com.kikoba.kikoba_backend.chat.entity.ChatType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, UUID> {

    @Query("SELECT c FROM ChatRoom c JOIN c.participants p WHERE p.id = :userId")
    List<ChatRoom> findAllByParticipantId(@Param("userId") UUID userId);

    Optional<ChatRoom> findByKikobaGroupId(UUID kikobaGroupId);

    @Query("""
        SELECT c FROM ChatRoom c
        JOIN c.participants p1
        JOIN c.participants p2
        WHERE c.type = :type
          AND p1.id = :userAId
          AND p2.id = :userBId
    """)
    Optional<ChatRoom> findPrivateChatBetween(
            @Param("userAId") UUID userAId,
            @Param("userBId") UUID userBId,
            @Param("type") ChatType type
    );
}