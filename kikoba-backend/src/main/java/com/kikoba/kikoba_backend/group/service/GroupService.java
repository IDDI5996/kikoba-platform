package com.kikoba.kikoba_backend.group.service;

import com.kikoba.kikoba_backend.group.dto.*;
import com.kikoba.kikoba_backend.group.enums.GroupRole;

import java.util.List;
import java.util.UUID;

public interface GroupService {
    GroupResponse createGroup(UUID userId, CreateGroupRequest request);
    GroupResponse getGroupById(UUID groupId, UUID requesterId);
    List<GroupResponse> getUserGroups(UUID userId);
    GroupResponse joinGroup(UUID userId, JoinGroupRequest request);
    void leaveGroup(UUID userId, UUID groupId);
    List<GroupMemberResponse> getGroupMembers(UUID groupId, UUID requesterId);
    GroupMemberResponse updateMemberRole(UUID groupId, UUID memberId, UUID requesterId, UpdateMemberRoleRequest request);
    void removeMember(UUID groupId, UUID memberId, UUID requesterId);
    void deactivateGroup(UUID groupId, UUID requesterId);
}