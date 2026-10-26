package com.kikoba.kikoba_backend.chat.service;

import com.kikoba.kikoba_backend.chat.dto.ChatDirectoryResponse;
import com.kikoba.kikoba_backend.chat.dto.ChatRoomResponse;
import com.kikoba.kikoba_backend.chat.dto.MessageResponse;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface ChatService {
    List<ChatRoomResponse> getMyChats(UUID userId);
    Page<MessageResponse> getMessages(UUID chatId, UUID userId, int page, int size);
    ChatRoomResponse getOrCreatePrivateChat(UUID userId, UUID otherUserId);
    ChatRoomResponse getOrCreateGroupChat(UUID userId, UUID kikobaGroupId);
    ChatDirectoryResponse getChatDirectory(UUID userId);

    /** Mark a single message as delivered to this user. */
    void markDelivered(UUID messageId, UUID userId);

    /** Mark every message in a chat as read by this user (skips own messages). */
    void markChatAsRead(UUID chatId, UUID userId);
}