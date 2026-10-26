package com.kikoba.kikoba_backend.group.service.impl;

import com.kikoba.kikoba_backend.exception.BadRequestException;
import com.kikoba.kikoba_backend.exception.ResourceAlreadyExistsException;
import com.kikoba.kikoba_backend.exception.ResourceNotFoundException;
import com.kikoba.kikoba_backend.exception.UnauthorizedException;
import com.kikoba.kikoba_backend.group.dto.*;
import com.kikoba.kikoba_backend.group.entity.Group;
import com.kikoba.kikoba_backend.group.entity.GroupMember;
import com.kikoba.kikoba_backend.group.enums.GroupRole;
import com.kikoba.kikoba_backend.group.mapper.GroupMapper;
import com.kikoba.kikoba_backend.group.repository.GroupMemberRepository;
import com.kikoba.kikoba_backend.group.repository.GroupRepository;
import com.kikoba.kikoba_backend.group.service.GroupService;
import com.kikoba.kikoba_backend.group.util.InvitationCodeGenerator;
import com.kikoba.kikoba_backend.user.entity.User;
import com.kikoba.kikoba_backend.user.repository.UserRepository;
import com.kikoba.kikoba_backend.notification.enums.NotificationPriority;
import com.kikoba.kikoba_backend.notification.enums.NotificationType;
import com.kikoba.kikoba_backend.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)   // <-- Default: read-only transactions for all methods
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final GroupMapper groupMapper;
    private final InvitationCodeGenerator codeGenerator;
    private final NotificationService notificationService;

    @Override
    @Transactional   // <-- Override: write transaction
    public GroupResponse createGroup(UUID userId, CreateGroupRequest request) {
        log.info("Creating group '{}' for user {}", request.getName(), userId);

        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getMinContribution() != null && request.getMaxContribution() != null
                && request.getMinContribution().compareTo(request.getMaxContribution()) > 0) {
            throw new BadRequestException("Minimum contribution cannot exceed maximum contribution");
        }

        String invitationCode = generateUniqueInvitationCode();

        Group group = Group.builder()
                .name(request.getName())
                .type(request.getType())
                .description(request.getDescription())
                .createdBy(creator)
                .invitationCode(invitationCode)
                .meetingFrequency(request.getMeetingFrequency())
                .meetingDay(request.getMeetingDay())
                .meetingTime(request.getMeetingTime())
                .minContribution(request.getMinContribution())
                .maxContribution(request.getMaxContribution())
                .interestRate(request.getInterestRate())
                .cycleDurationMonths(request.getCycleDurationMonths())
                .currency(request.getCurrency() != null ? request.getCurrency() : "TZS")
                .isActive(true)
                .build();

        Group savedGroup = groupRepository.save(group);

        GroupMember founderMember = GroupMember.builder()
                .group(savedGroup)
                .user(creator)
                .role(GroupRole.CHAIRPERSON)
                .joinedAt(LocalDateTime.now())
                .isActive(true)
                .build();
        groupMemberRepository.save(founderMember);

        log.info("Group created: {} (code: {})", savedGroup.getId(), invitationCode);

        // Reload with creator fetched for the mapper
        Group reloaded = groupRepository.findByIdWithCreator(savedGroup.getId()).orElse(savedGroup);
        return groupMapper.toResponse(reloaded, 1L);
    }

    @Override
    public GroupResponse getGroupById(UUID groupId, UUID requesterId) {
        // Fetch group with creator eagerly
        Group group = groupRepository.findByIdWithCreator(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, requesterId)) {
            throw new UnauthorizedException("You are not a member of this group");
        }

        long memberCount = groupMemberRepository.countByGroupIdAndIsActiveTrue(groupId);
        return groupMapper.toResponse(group, memberCount);
    }

    @Override
    public List<GroupResponse> getUserGroups(UUID userId) {
        List<GroupMember> memberships = groupMemberRepository.findByUserIdWithGroup(userId);

        return memberships.stream()
                .map(m -> {
                    long count = groupMemberRepository.countByGroupIdAndIsActiveTrue(m.getGroup().getId());
                    return groupMapper.toResponse(m.getGroup(), count, m.getRole().name());
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public GroupResponse joinGroup(UUID userId, JoinGroupRequest request) {
        log.info("User {} attempting to join group with code {}", userId, request.getInvitationCode());

        Group group = groupRepository.findByInvitationCodeWithCreator(request.getInvitationCode().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid invitation code"));

        if (!group.getIsActive()) {
            throw new BadRequestException("This group is no longer active");
        }

        if (groupMemberRepository.existsByGroupIdAndUserId(group.getId(), userId)) {
            throw new ResourceAlreadyExistsException("You are already a member of this group");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        GroupMember newMember = GroupMember.builder()
                .group(group)
                .user(user)
                .role(GroupRole.MEMBER)
                .joinedAt(LocalDateTime.now())
                .isActive(true)
                .build();

        groupMemberRepository.save(newMember);

        long memberCount = groupMemberRepository.countByGroupIdAndIsActiveTrue(group.getId());
        log.info("User {} joined group {} successfully", userId, group.getId());

        notificationService.notifyUserAsync(
                userId,
                NotificationType.GROUP_JOINED,
                "Welcome to " + group.getName(),
                "You have joined the group successfully.",
                "/groups/" + group.getId(),
                "GroupMember",
                newMember.getId(),
                NotificationPriority.NORMAL
        );

        // Also notify the Chairperson
        if (group.getCreatedBy() != null) {
            notificationService.notifyUserAsync(
                    group.getCreatedBy().getId(),
                    NotificationType.GROUP_JOINED,
                    "New Member Joined",
                    user.getFirstName() + " " + user.getLastName() + " joined " + group.getName() + ".",
                    "/groups/" + group.getId(),
                    "GroupMember",
                    newMember.getId(),
                    NotificationPriority.NORMAL
            );
        }

        return groupMapper.toResponse(group, memberCount);
    }

    @Override
    @Transactional
    public void leaveGroup(UUID userId, UUID groupId) {
        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("You are not a member of this group"));

        Group group = member.getGroup();

        if (group.getCreatedBy() != null && group.getCreatedBy().getId().equals(userId)) {
            throw new BadRequestException("Group creator cannot leave. Transfer ownership or deactivate the group.");
        }

        member.setIsActive(false);
        member.setLeftAt(LocalDateTime.now());
        groupMemberRepository.save(member);

        log.info("User {} left group {}", userId, groupId);
    }

    @Override
    public List<GroupMemberResponse> getGroupMembers(UUID groupId, UUID requesterId) {
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, requesterId)) {
            throw new UnauthorizedException("You are not a member of this group");
        }

        //JOIN FETCH to eagerly load user
        return groupMemberRepository.findByGroupIdWithUser(groupId).stream()
                .map(groupMapper::toMemberResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public GroupMemberResponse updateMemberRole(UUID groupId, UUID memberId, UUID requesterId,
                                                UpdateMemberRoleRequest request) {
        GroupMember requester = groupMemberRepository.findByGroupIdAndUserId(groupId, requesterId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        if (requester.getRole() != GroupRole.CHAIRPERSON) {
            throw new UnauthorizedException("Only the Chairperson can change member roles");
        }

        GroupMember targetMember = groupMemberRepository.findByIdWithDetails(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        Group group = targetMember.getGroup();

        if (!targetMember.getGroup().getId().equals(groupId)) {
            throw new BadRequestException("Member does not belong to this group");
        }

        if (!targetMember.getIsActive()) {
            throw new BadRequestException("Cannot update role of an inactive member");
        }

        if (targetMember.getId().equals(requester.getId()) && request.getRole() != GroupRole.CHAIRPERSON) {
            throw new BadRequestException("You cannot change your own role. Transfer chairperson role to someone else.");
        }

        // ---- GOLDEN RULE: Prevent privilege escalation on critical roles ----
        // If the target holds a critical role (TREASURER/CHAIRPERSON), the change
        // should ideally require a vote. For now, log it as HIGH priority and notify everyone.
        GroupRole previousRole = targetMember.getRole();

        if (previousRole == request.getRole()) {
            throw new BadRequestException("Member already has this role");
        }

        targetMember.setRole(request.getRole());
        GroupMember updated = groupMemberRepository.save(targetMember);

        log.warn("[AUDIT] Role change by {} in group {}: {} {} → {} | Reason: {}",
                requester.getUser().getPhoneNumber(),
                group.getName(),
                targetMember.getUser().getFirstName() + " " + targetMember.getUser().getLastName(),
                previousRole,
                request.getRole(),
                request.getReason());

        // ---- Notify the affected member ----
        notificationService.notifyUserAsync(
                targetMember.getUser().getId(),
                NotificationType.GROUP_ROLE_CHANGED,
                "Your Role Has Changed",
                "Your role in " + group.getName() + " changed from " + previousRole
                        + " to " + request.getRole() + ". Reason: " + request.getReason(),
                "/groups/" + group.getId(),
                "GroupMember",
                updated.getId(),
                NotificationPriority.HIGH
        );

        // ---- Notify ALL OTHER active members for transparency ----
        List<GroupMember> allMembers = groupMemberRepository.findByGroupIdAndIsActiveTrue(groupId);
        String targetName = targetMember.getUser().getFirstName() + " " + targetMember.getUser().getLastName();

        for (GroupMember member : allMembers) {
            if (member.getId().equals(targetMember.getId())
                    || member.getId().equals(requester.getId())) {
                continue;
            }

            notificationService.notifyUserAsync(
                    member.getUser().getId(),
                    NotificationType.GROUP_ROLE_CHANGED,
                    "Leadership Change in " + group.getName(),
                    requester.getUser().getFirstName() + " " + requester.getUser().getLastName()
                            + " changed " + targetName + "'s role from " + previousRole
                            + " to " + request.getRole() + ". Reason: " + request.getReason(),
                    "/groups/" + group.getId() + "/settings",
                    "GroupMember",
                    updated.getId(),
                    NotificationPriority.NORMAL
            );
        }

        return groupMapper.toMemberResponse(updated);
    }

    @Override
    @Transactional
    public void removeMember(UUID groupId, UUID memberId, UUID requesterId) {
        GroupMember requester = groupMemberRepository.findByGroupIdAndUserId(groupId, requesterId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        if (requester.getRole() != GroupRole.CHAIRPERSON && requester.getRole() != GroupRole.TREASURER) {
            throw new UnauthorizedException("Only the Chairperson or Treasurer can remove members");
        }

        GroupMember targetMember = groupMemberRepository.findByIdWithDetails(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        if (!targetMember.getGroup().getId().equals(groupId)) {
            throw new BadRequestException("Member does not belong to this group");
        }

        if (targetMember.getId().equals(requester.getId())) {
            throw new BadRequestException("You cannot remove yourself. Use 'Leave Group' instead.");
        }

        if (targetMember.getGroup().getCreatedBy() != null
                && targetMember.getGroup().getCreatedBy().getId().equals(targetMember.getUser().getId())) {
            throw new BadRequestException("Cannot remove the group creator");
        }

        if (targetMember.getTotalLoanBalance() != null && targetMember.getTotalLoanBalance().signum() > 0) {
            throw new BadRequestException("Cannot remove member with outstanding loan balance. Balance: "
                    + targetMember.getTotalLoanBalance());
        }

        targetMember.setIsActive(false);
        targetMember.setLeftAt(LocalDateTime.now());
        groupMemberRepository.save(targetMember);

        log.info("Member {} removed from group {}", memberId, groupId);

        notificationService.notifyUserAsync(
                targetMember.getUser().getId(),
                NotificationType.GROUP_MEMBER_REMOVED,
                "Removed from " + targetMember.getGroup().getName(),
                "You have been removed from the group.",
                null,
                "GroupMember",
                targetMember.getId(),
                NotificationPriority.URGENT
        );
    }

    @Override
    @Transactional
    public void deactivateGroup(UUID groupId, UUID requesterId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        if (group.getCreatedBy() == null || !group.getCreatedBy().getId().equals(requesterId)) {
            throw new UnauthorizedException("Only the group creator can deactivate this group");
        }

        group.setIsActive(false);
        groupRepository.save(group);

        log.info("Group {} deactivated", groupId);
    }

    // ----- Helper -----

    private String generateUniqueInvitationCode() {
        String code;
        int attempts = 0;
        do {
            code = codeGenerator.generate();
            attempts++;
            if (attempts > 10) {
                throw new IllegalStateException("Could not generate unique invitation code");
            }
        } while (groupRepository.existsByInvitationCode(code));
        return code;
    }
}