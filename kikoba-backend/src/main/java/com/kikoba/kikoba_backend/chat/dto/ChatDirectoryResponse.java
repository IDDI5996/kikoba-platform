package com.kikoba.kikoba_backend.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatDirectoryResponse {
    private List<GroupEntry> groups;
    private List<UserEntry> members;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupEntry {
        private UUID id;
        private String name;
        private int memberCount;
        private UUID chatRoomId; // null if the group has no chat room yet
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserEntry {
        private UUID id;
        private String firstName;
        private String lastName;
        private String profilePictureUrl;
        private String phoneNumber;
    }
}