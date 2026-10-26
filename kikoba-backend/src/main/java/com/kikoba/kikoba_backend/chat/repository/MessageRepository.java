package com.kikoba.kikoba_backend.chat.repository;

import com.kikoba.kikoba_backend.chat.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    Page<Message> findByChatRoomIdOrderByCreatedAtDesc(UUID chatRoomId, Pageable pageable);

    Optional<Message> findTopByChatRoomIdOrderByCreatedAtDesc(UUID chatRoomId);

    /**
     * Loads a message with its sender eagerly — safe to call outside a
     * transaction (used when resolving the parent of a reply).
     */
    @Query("""
        SELECT m FROM Message m
        JOIN FETCH m.sender
        WHERE m.id = :id
    """)
    Optional<Message> findByIdWithSender(@Param("id") UUID id);

    @Query("""
        SELECT m FROM Message m
        WHERE m.chatRoom.id = :chatId
          AND m.sender.id <> :userId
          AND m.readAt IS NULL
    """)
    List<Message> findUnreadForUser(
            @Param("chatId") UUID chatId,
            @Param("userId") UUID userId
    );
}