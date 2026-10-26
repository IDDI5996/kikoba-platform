package com.kikoba.kikoba_backend.chat.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class MarkAsReadDto {
    private UUID chatRoomId;
    private UUID messageId; // optional — for single-message mark
}