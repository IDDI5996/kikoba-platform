package com.kikoba.kikoba_backend.chat.controller;

import com.kikoba.kikoba_backend.chat.dto.ChatDirectoryResponse;
import com.kikoba.kikoba_backend.chat.dto.ChatRoomResponse;
import com.kikoba.kikoba_backend.chat.dto.CreatePrivateChatRequest;
import com.kikoba.kikoba_backend.chat.dto.MessageResponse;
import com.kikoba.kikoba_backend.chat.service.ChatService;
import com.kikoba.kikoba_backend.user.service.FileStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final FileStorageService fileStorageService;

    @GetMapping
    public ResponseEntity<List<ChatRoomResponse>> getMyChats(
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(chatService.getMyChats(userId));
    }

    @GetMapping("/directory")
    public ResponseEntity<ChatDirectoryResponse> getDirectory(
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(chatService.getChatDirectory(userId));
    }

    @GetMapping("/{chatId}/messages")
    public ResponseEntity<Page<MessageResponse>> getMessages(
            @PathVariable UUID chatId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(chatService.getMessages(chatId, userId, page, size));
    }

    @PostMapping("/private")
    public ResponseEntity<ChatRoomResponse> createOrGetPrivateChat(
            @Valid @RequestBody CreatePrivateChatRequest request,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(
                chatService.getOrCreatePrivateChat(userId, request.getOtherUserId()));
    }

    @PostMapping("/group/{kikobaGroupId}")
    public ResponseEntity<ChatRoomResponse> createOrGetGroupChat(
            @PathVariable UUID kikobaGroupId,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(
                chatService.getOrCreateGroupChat(userId, kikobaGroupId));
    }

    // ---- Attachment upload ----
    @PostMapping(value = "/attachments", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, String>> uploadAttachment(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UUID userId) {
        String url = fileStorageService.saveChatAttachment(userId, file);
        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
        return ResponseEntity.ok(Map.of("url", url, "type", contentType));
    }
}