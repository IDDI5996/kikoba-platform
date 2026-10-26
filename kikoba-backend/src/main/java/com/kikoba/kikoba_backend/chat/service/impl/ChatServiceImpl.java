package com.kikoba.kikoba_backend.chat.service.impl;

import com.kikoba.kikoba_backend.chat.dto.ChatDirectoryResponse;
import com.kikoba.kikoba_backend.chat.dto.ChatRoomResponse;
import com.kikoba.kikoba_backend.chat.dto.MessageResponse;
import com.kikoba.kikoba_backend.chat.entity.ChatRoom;
import com.kikoba.kikoba_backend.chat.entity.ChatType;
import com.kikoba.kikoba_backend.chat.entity.Message;
import com.kikoba.kikoba_backend.chat.repository.ChatRoomRepository;
import com.kikoba.kikoba_backend.chat.repository.MessageRepository;
import com.kikoba.kikoba_backend.chat.service.ChatService;
import com.kikoba.kikoba_backend.exception.BadRequestException;
import com.kikoba.kikoba_backend.exception.ResourceNotFoundException;
import com.kikoba.kikoba_backend.group.entity.Group;
import com.kikoba.kikoba_backend.group.entity.GroupMember;
import com.kikoba.kikoba_backend.group.repository.GroupMemberRepository;
import com.kikoba.kikoba_backend.user.entity.User;
import com.kikoba.kikoba_backend.user.repository.UserRepository;
import com.kikoba.kikoba_backend.chat.dto.MarkAsReadDto;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;

    // ===========================================================
    //  GET MY CHATS
    // ===========================================================
    @Override
    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getMyChats(UUID userId) {
        List<ChatRoom> rooms = chatRoomRepository.findAllByParticipantId(userId);

        return rooms.stream()
                .map(room -> toResponse(room, userId))
                .sorted(Comparator.comparing(
                        ChatRoomResponse::getLastMessageAt,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .collect(Collectors.toList());
    }

    // ===========================================================
    //  GET MESSAGES (paginated)
    //  @Transactional keeps the Hibernate session open while we
    //  access lazy fields (participants, sender) — fixes 500 error
    // ===========================================================
    @Override
    @Transactional(readOnly = true)
    public Page<MessageResponse> getMessages(UUID chatId, UUID userId, int page, int size) {
        ChatRoom room = chatRoomRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));

        boolean isParticipant = room.getParticipants().stream()
                .anyMatch(u -> u.getId().equals(userId));
        if (!isParticipant) {
            throw new BadRequestException("You are not a participant in this chat");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messages =
                messageRepository.findByChatRoomIdOrderByCreatedAtDesc(chatId, pageable);

        return messages.map(this::toMessageResponse);
    }

    // ===========================================================
    //  GET OR CREATE PRIVATE CHAT
    // ===========================================================
    @Override
    @Transactional
    public ChatRoomResponse getOrCreatePrivateChat(UUID userId, UUID otherUserId) {
        if (userId.equals(otherUserId)) {
            throw new BadRequestException("You cannot create a chat with yourself");
        }

        Optional<ChatRoom> existing = chatRoomRepository.findPrivateChatBetween(
                userId, otherUserId, ChatType.PRIVATE);
        if (existing.isPresent()) {
            return toResponse(existing.get(), userId);
        }

        User me = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        User other = userRepository.findById(otherUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Other user not found"));

        ChatRoom room = ChatRoom.builder()
                .type(ChatType.PRIVATE)
                .participants(new HashSet<>(Set.of(me, other)))
                .build();

        ChatRoom saved = chatRoomRepository.save(room);
        log.info("Created private chat {} between {} and {}", saved.getId(), userId, otherUserId);

        return toResponse(saved, userId);
    }

    // ===========================================================
    //  GET OR CREATE GROUP CHAT (Kikoba group → chat room)
    // ===========================================================
    @Override
    @Transactional
    public ChatRoomResponse getOrCreateGroupChat(UUID userId, UUID kikobaGroupId) {
        // 1. Verify the user is actually a member of this Kikoba group
        GroupMember membership = groupMemberRepository
                .findByGroupIdAndUserId(kikobaGroupId, userId)
                .orElseThrow(() -> new BadRequestException(
                        "You are not a member of this group"));

        if (!Boolean.TRUE.equals(membership.getIsActive())) {
            throw new BadRequestException("Your membership is not active");
        }

        // 2. Return existing room if it exists
        Optional<ChatRoom> existing = chatRoomRepository.findByKikobaGroupId(kikobaGroupId);
        if (existing.isPresent()) {
            return toResponse(existing.get(), userId);
        }

        // 3. Otherwise, create the room with all active group members as participants
        Group group = membership.getGroup();

        List<GroupMember> allMembers =
                groupMemberRepository.findByGroupIdAndIsActiveTrue(kikobaGroupId);

        Set<User> participants = allMembers.stream()
                .map(GroupMember::getUser)
                .collect(Collectors.toSet());

        ChatRoom room = ChatRoom.builder()
                .type(ChatType.GROUP)
                .name(group.getName() + " Chat")
                .kikobaGroupId(kikobaGroupId)
                .participants(participants)
                .build();

        ChatRoom saved = chatRoomRepository.save(room);
        log.info("Created group chat {} for kikoba group {}", saved.getId(), kikobaGroupId);

        return toResponse(saved, userId);
    }

    // ===========================================================
    //  GET CHAT DIRECTORY (groups + shared members)
    // ===========================================================
    @Override
    @Transactional(readOnly = true)
    public ChatDirectoryResponse getChatDirectory(UUID userId) {

        // 1. Get all active memberships of the current user
        List<GroupMember> myMemberships =
                groupMemberRepository.findByUserIdAndIsActiveTrue(userId);

        // 2. Build the list of groups the user belongs to
        List<ChatDirectoryResponse.GroupEntry> groupEntries = myMemberships.stream()
                .map(gm -> {
                    Group g = gm.getGroup();
                    Optional<ChatRoom> room =
                            chatRoomRepository.findByKikobaGroupId(g.getId());
                    long memberCount =
                            groupMemberRepository.countByGroupIdAndIsActiveTrue(g.getId());
                    return ChatDirectoryResponse.GroupEntry.builder()
                            .id(g.getId())
                            .name(g.getName())
                            .memberCount((int) memberCount)
                            .chatRoomId(room.map(ChatRoom::getId).orElse(null))
                            .build();
                })
                .collect(Collectors.toList());

        // 3. Collect everyone who shares at least one group with me (excluding me)
        Set<UUID> sharedMemberIds = new HashSet<>();
        for (GroupMember gm : myMemberships) {
            UUID groupId = gm.getGroup().getId();
            List<GroupMember> members =
                    groupMemberRepository.findByGroupIdAndIsActiveTrue(groupId);
            for (GroupMember m : members) {
                UUID memberId = m.getUser().getId();
                if (!memberId.equals(userId)) {
                    sharedMemberIds.add(memberId);
                }
            }
        }

        List<User> sharedMembers = userRepository.findAllById(sharedMemberIds);

        List<ChatDirectoryResponse.UserEntry> memberEntries = sharedMembers.stream()
                .map(u -> ChatDirectoryResponse.UserEntry.builder()
                        .id(u.getId())
                        .firstName(u.getFirstName())
                        .lastName(u.getLastName())
                        .profilePictureUrl(u.getProfilePictureUrl())
                        .phoneNumber(u.getPhoneNumber())
                        .build())
                .collect(Collectors.toList());

        return ChatDirectoryResponse.builder()
                .groups(groupEntries)
                .members(memberEntries)
                .build();
    }

    // ===========================================================
    //  MAPPING HELPERS
    // ===========================================================
    private ChatRoomResponse toResponse(ChatRoom room, UUID currentUserId) {
        Message last = messageRepository
                .findTopByChatRoomIdOrderByCreatedAtDesc(room.getId())
                .orElse(null);

        String otherAvatar = null;
        UUID otherUserId = null;
        String displayName = room.getName();

        if (room.getType() == ChatType.PRIVATE) {
            Optional<User> other = room.getParticipants().stream()
                    .filter(u -> !u.getId().equals(currentUserId))
                    .findFirst();
            if (other.isPresent()) {
                otherAvatar = other.get().getProfilePictureUrl();
                otherUserId = other.get().getId();
                displayName = other.get().getFirstName() + " " + other.get().getLastName();
            }
        }

        return ChatRoomResponse.builder()
                .id(room.getId())
                .type(room.getType())
                .name(displayName)
                .kikobaGroupId(room.getKikobaGroupId())
                .otherUserAvatarUrl(otherAvatar)
                .otherUserId(otherUserId)
                .lastMessage(last != null ? last.getContent() : null)
                .lastMessageAt(last != null ? last.getCreatedAt() : null)
                .unreadCount(0)
                .build();
    }

    private MessageResponse toMessageResponse(Message message) {
        User sender = message.getSender();

        // Compute status
        String status;
        if (message.getReadAt() != null) {
            status = "READ";
        } else if (message.getDeliveredAt() != null) {
            status = "DELIVERED";
        } else {
            status = "SENT";
        }

        // Resolve reply
        UUID replyToId = message.getReplyToId();
        String replyToContent = null;
        String replyToSenderName = null;
        if (replyToId != null) {
            Message parent = messageRepository.findByIdWithSender(replyToId).orElse(null);            if (parent != null) {
                replyToContent = parent.getContent();
                User parentSender = parent.getSender();
                replyToSenderName = parentSender.getFirstName() + " " + parentSender.getLastName();
            }
        }

        return MessageResponse.builder()
                .id(message.getId())
                .chatRoomId(message.getChatRoom().getId())
                .senderId(sender.getId())
                .senderName(sender.getFirstName() + " " + sender.getLastName())
                .senderAvatarUrl(sender.getProfilePictureUrl())
                .content(message.getContent())
                .iv(message.getIv())
                .messageType(message.getMessageType())
                .replyToId(replyToId)
                .replyToContent(replyToContent)
                .replyToSenderName(replyToSenderName)
                .attachmentUrl(message.getAttachmentUrl())
                .attachmentType(message.getAttachmentType())
                .status(status)
                .createdAt(message.getCreatedAt())
                .build();
    }

    // ===========================================================
    //  MARK DELIVERED
    // ===========================================================
    @Override
    @Transactional
    public void markDelivered(UUID messageId, UUID userId) {
        Message msg = messageRepository.findById(messageId).orElse(null);
        if (msg == null) return;
        // Skip if the current user is the sender
        if (msg.getSender().getId().equals(userId)) return;

        if (msg.getDeliveredAt() == null) {
            msg.setDeliveredAt(LocalDateTime.now());
            messageRepository.save(msg);
        }
    }

    // ===========================================================
    //  MARK CHAT AS READ (all messages where sender != me)
    // ===========================================================
    @Override
    @Transactional
    public void markChatAsRead(UUID chatId, UUID userId) {
        ChatRoom room = chatRoomRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));

        boolean isParticipant = room.getParticipants().stream()
                .anyMatch(u -> u.getId().equals(userId));
        if (!isParticipant) return;

        List<Message> unread = messageRepository
                .findUnreadForUser(chatId, userId);

        LocalDateTime now = LocalDateTime.now();
        for (Message m : unread) {
            if (m.getDeliveredAt() == null) m.setDeliveredAt(now);
            m.setReadAt(now);
        }
        if (!unread.isEmpty()) messageRepository.saveAll(unread);
    }
}