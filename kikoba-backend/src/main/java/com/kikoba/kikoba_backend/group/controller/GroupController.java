package com.kikoba.kikoba_backend.group.controller;

import com.kikoba.kikoba_backend.group.dto.*;
import com.kikoba.kikoba_backend.group.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    /**
     * Create a new group
     * POST /api/groups
     */
    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody CreateGroupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(groupService.createGroup(userId, request));
    }

    /**
     * Get all groups the current user belongs to
     * GET /api/groups
     */
    @GetMapping
    public ResponseEntity<List<GroupResponse>> getMyGroups(@AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(groupService.getUserGroups(userId));
    }

    /**
     * Get a specific group by ID
     * GET /api/groups/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<GroupResponse> getGroupById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(groupService.getGroupById(id, userId));
    }

    /**
     * Join a group using an invitation code
     * POST /api/groups/join
     */
    @PostMapping("/join")
    public ResponseEntity<GroupResponse> joinGroup(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody JoinGroupRequest request) {
        return ResponseEntity.ok(groupService.joinGroup(userId, request));
    }

    /**
     * Leave a group
     * DELETE /api/groups/{id}/leave
     */
    @DeleteMapping("/{id}/leave")
    public ResponseEntity<Void> leaveGroup(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID userId) {
        groupService.leaveGroup(userId, id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all members of a group
     * GET /api/groups/{id}/members
     */
    @GetMapping("/{id}/members")
    public ResponseEntity<List<GroupMemberResponse>> getGroupMembers(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(groupService.getGroupMembers(id, userId));
    }

    /**
     * Update a member's role (Chairperson only)
     * PUT /api/groups/{groupId}/members/{memberId}/role
     */
    @PutMapping("/{groupId}/members/{memberId}/role")
    public ResponseEntity<GroupMemberResponse> updateMemberRole(
            @PathVariable UUID groupId,
            @PathVariable UUID memberId,
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody UpdateMemberRoleRequest request) {
        return ResponseEntity.ok(groupService.updateMemberRole(groupId, memberId, userId, request));
    }

    /**
     * Remove a member from the group (Chairperson/Treasurer only)
     * DELETE /api/groups/{groupId}/members/{memberId}
     */
    @DeleteMapping("/{groupId}/members/{memberId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID groupId,
            @PathVariable UUID memberId,
            @AuthenticationPrincipal UUID userId) {
        groupService.removeMember(groupId, memberId, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Deactivate a group (Creator only)
     * DELETE /api/groups/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateGroup(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID userId) {
        groupService.deactivateGroup(id, userId);
        return ResponseEntity.noContent().build();
    }
}