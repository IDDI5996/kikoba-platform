package com.kikoba.kikoba_backend.chat.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreatePrivateChatRequest {
    @NotNull
    private UUID otherUserId;
}