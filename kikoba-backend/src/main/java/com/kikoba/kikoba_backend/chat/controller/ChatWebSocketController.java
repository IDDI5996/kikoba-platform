package com.kikoba.kikoba_backend.chat.controller;

import com.kikoba.kikoba_backend.chat.dto.MarkAsReadDto;
import com.kikoba.kikoba_backend.chat.dto.MessageDto;
import com.kikoba.kikoba_backend.chat.dto.MessageResponse;
import com.kikoba.kikoba_backend.chat.entity.ChatRoom;
import com.kikoba.kikoba_backend.chat.entity.Message;
import com.kikoba.kikoba_backend.chat.repository.ChatRoomRepository;
import com.kikoba.kikoba_backend.chat.repository.MessageRepository;
import com.kikoba.kikoba_backend.chat.service.ChatService;
import com.kikoba.kikoba_backend.user.entity.User;
import com.kikoba.kikoba_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageRepository messageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final ChatService chatService;

    // ===========================================================
    //  SEND MESSAGE
    // ===========================================================
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload MessageDto messageDto, Principal principal) {
        if (principal == null) {
            log.error(">>> Principal is NULL");
            return;
        }
        try {
            UUID senderId = UUID.fromString(principal.getName());
            User sender = userRepository.findById(senderId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            ChatRoom chatRoom = chatRoomRepository.findById(messageDto.getChatRoomId())
                    .orElseThrow(() -> new RuntimeException("Chat room not found"));

            Message message = Message.builder()
                    .chatRoom(chatRoom)
                    .sender(sender)
                    .content(messageDto.getContent())
                    .iv(messageDto.getIv())
                    .messageType(messageDto.getMessageType())
                    .replyToId(messageDto.getReplyToId())
                    .attachmentUrl(messageDto.getAttachmentUrl())
                    .attachmentType(messageDto.getAttachmentType())
                    .build();

            Message saved = messageRepository.save(message);

            MessageResponse payload = buildResponse(saved);
            messagingTemplate.convertAndSend(
                    "/topic/chat/" + chatRoom.getId(),
                    (Object) payload);
            log.info(">>> Message {} sent to room {}", saved.getId(), chatRoom.getId());

        } catch (Exception e) {
            log.error(">>> sendMessage failed: {}", e.getMessage(), e);
        }
    }

    // ===========================================================
    //  MARK DELIVERED
    // ===========================================================
    @MessageMapping("/chat.markDelivered")
    public void markDelivered(@Payload MarkAsReadDto dto, Principal principal) {
        if (principal == null || dto.getMessageId() == null) return;
        try {
            UUID userId = UUID.fromString(principal.getName());
            chatService.markDelivered(dto.getMessageId(), userId);
            broadcastStatus(dto.getMessageId(), dto.getChatRoomId(), "DELIVERED");
        } catch (Exception e) {
            log.error(">>> markDelivered failed: {}", e.getMessage());
        }
    }

    // ===========================================================
    //  MARK READ
    // ===========================================================
    @MessageMapping("/chat.markRead")
    public void markRead(@Payload MarkAsReadDto dto, Principal principal) {
        if (principal == null || dto.getChatRoomId() == null) return;
        try {
            UUID userId = UUID.fromString(principal.getName());
            chatService.markChatAsRead(dto.getChatRoomId(), userId);
            broadcastStatus(null, dto.getChatRoomId(), "READ");
        } catch (Exception e) {
            log.error(">>> markRead failed: {}", e.getMessage());
        }
    }

    // ===========================================================
    //  BROADCAST STATUS
    // ===========================================================
    private void broadcastStatus(UUID messageId, UUID chatRoomId, String status) {
        if (chatRoomId == null) return;
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "STATUS_UPDATE");
        payload.put("messageId", messageId);
        payload.put("chatRoomId", chatRoomId);
        payload.put("status", status);
        messagingTemplate.convertAndSend(
                "/topic/chat/" + chatRoomId + "/status",
                (Object) payload);
    }

    // ===========================================================
    //  BUILD RESPONSE
    //  Uses findByIdWithSender for the parent — eager fetch
    //  prevents LazyInitializationException on replyToId.
    // ===========================================================
    private MessageResponse buildResponse(Message saved) {
        User sender = saved.getSender();

        String status = saved.getReadAt() != null ? "READ"
                : saved.getDeliveredAt() != null ? "DELIVERED"
                : "SENT";

        String replyToContent = null;
        String replyToSenderName = null;
        if (saved.getReplyToId() != null) {
            Message parent = messageRepository
                    .findByIdWithSender(saved.getReplyToId())    // <-- FIX
                    .orElse(null);
            if (parent != null) {
                replyToContent = parent.getContent();
                User parentSender = parent.getSender();
                replyToSenderName = parentSender.getFirstName()
                        + " " + parentSender.getLastName();
            }
        }

        return MessageResponse.builder()
                .id(saved.getId())
                .chatRoomId(saved.getChatRoom().getId())
                .senderId(sender.getId())
                .senderName(sender.getFirstName() + " " + sender.getLastName())
                .senderAvatarUrl(sender.getProfilePictureUrl())
                .content(saved.getContent())
                .iv(saved.getIv())
                .messageType(saved.getMessageType())
                .replyToId(saved.getReplyToId())
                .replyToContent(replyToContent)
                .replyToSenderName(replyToSenderName)
                .attachmentUrl(saved.getAttachmentUrl())
                .attachmentType(saved.getAttachmentType())
                .status(status)
                .createdAt(saved.getCreatedAt() != null
                        ? saved.getCreatedAt()
                        : LocalDateTime.now())
                .build();
    }
}