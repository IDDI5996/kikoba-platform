package com.kikoba.kikoba_backend.chat.dto;

import com.kikoba.kikoba_backend.chat.entity.MessageType;
import lombok.Data;

import java.util.UUID;

@Data
public class MessageDto {
    private UUID chatRoomId;
    private String content;
    private String iv;
    private MessageType messageType;
    private UUID replyToId;
    private String attachmentUrl;
    private String attachmentType;
}