package com.kikoba.kikoba_backend.chat.dto;

import com.kikoba.kikoba_backend.chat.entity.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private UUID id;
    private UUID chatRoomId;
    private UUID senderId;
    private String senderName;
    private String senderAvatarUrl;
    private String content;
    private String iv;
    private MessageType messageType;

    // Reply
    private UUID replyToId;
    private String replyToContent;
    private String replyToSenderName;

    // Attachments
    private String attachmentUrl;
    private String attachmentType;

    // Status: SENT | DELIVERED | READ
    private String status;

    private LocalDateTime createdAt;
}