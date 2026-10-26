package com.kikoba.kikoba_backend.governance.service.impl;

import com.kikoba.kikoba_backend.exception.ResourceNotFoundException;
import com.kikoba.kikoba_backend.exception.UnauthorizedException;
import com.kikoba.kikoba_backend.governance.dto.AnnouncementResponse;
import com.kikoba.kikoba_backend.governance.dto.CreateAnnouncementRequest;
import com.kikoba.kikoba_backend.governance.entity.Announcement;
import com.kikoba.kikoba_backend.governance.mapper.AnnouncementMapper;
import com.kikoba.kikoba_backend.governance.repository.AnnouncementRepository;
import com.kikoba.kikoba_backend.governance.service.AnnouncementService;
import com.kikoba.kikoba_backend.group.entity.Group;
import com.kikoba.kikoba_backend.group.entity.GroupMember;
import com.kikoba.kikoba_backend.group.enums.GroupRole;
import com.kikoba.kikoba_backend.group.repository.GroupMemberRepository;
import com.kikoba.kikoba_backend.notification.enums.NotificationPriority;
import com.kikoba.kikoba_backend.notification.enums.NotificationType;
import com.kikoba.kikoba_backend.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final AnnouncementMapper announcementMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public AnnouncementResponse createAnnouncement(UUID userId, UUID groupId, CreateAnnouncementRequest request) {
        GroupMember requester = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        if (requester.getRole() != GroupRole.CHAIRPERSON && requester.getRole() != GroupRole.SECRETARY) {
            throw new UnauthorizedException("Only Chairperson or Secretary can create announcements");
        }

        Group group = requester.getGroup();

        Announcement announcement = Announcement.builder()
                .group(group)
                .title(request.getTitle())
                .body(request.getBody())
                .category(request.getCategory())
                .pinned(request.getPinned() != null ? request.getPinned() : false)
                .createdBy(requester.getUser())
                .build();

        Announcement saved = announcementRepository.save(announcement);
        log.info("Announcement {} created in group {}", saved.getId(), groupId);

        // Notify all group members
        List<GroupMember> members = groupMemberRepository.findByGroupIdAndIsActiveTrue(groupId);
        NotificationPriority priority = announcement.getCategory().name().equals("EMERGENCY")
                ? NotificationPriority.URGENT
                : NotificationPriority.NORMAL;

        for (GroupMember member : members) {
            notificationService.notifyUserAsync(
                    member.getUser().getId(),
                    NotificationType.ANNOUNCEMENT_POSTED,
                    announcement.getTitle(),
                    announcement.getBody(),
                    "/announcements/" + saved.getId(),
                    "Announcement",
                    saved.getId(),
                    priority
            );
        }

        return announcementMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public AnnouncementResponse updateAnnouncement(UUID announcementId, UUID requesterId,
                                                   CreateAnnouncementRequest request) {
        Announcement announcement = announcementRepository.findByIdWithDetails(announcementId)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found"));

        Group group = announcement.getGroup();

        GroupMember requester = groupMemberRepository.findByGroupIdAndUserId(group.getId(), requesterId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        if (requester.getRole() != GroupRole.CHAIRPERSON && requester.getRole() != GroupRole.SECRETARY) {
            throw new UnauthorizedException("Only Chairperson or Secretary can update announcements");
        }

        announcement.setTitle(request.getTitle());
        announcement.setBody(request.getBody());
        announcement.setCategory(request.getCategory());
        if (request.getPinned() != null) {
            announcement.setPinned(request.getPinned());
        }

        Announcement saved = announcementRepository.save(announcement);
        return announcementMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public AnnouncementResponse togglePin(UUID announcementId, UUID requesterId) {
        Announcement announcement = announcementRepository.findByIdWithDetails(announcementId)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found"));

        Group group = announcement.getGroup();

        GroupMember requester = groupMemberRepository.findByGroupIdAndUserId(group.getId(), requesterId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        if (requester.getRole() != GroupRole.CHAIRPERSON && requester.getRole() != GroupRole.SECRETARY) {
            throw new UnauthorizedException("Only Chairperson or Secretary can pin announcements");
        }

        announcement.setPinned(!announcement.getPinned());
        Announcement saved = announcementRepository.save(announcement);
        return announcementMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteAnnouncement(UUID announcementId, UUID requesterId) {
        Announcement announcement = announcementRepository.findByIdWithDetails(announcementId)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found"));

        Group group = announcement.getGroup();

        GroupMember requester = groupMemberRepository.findByGroupIdAndUserId(group.getId(), requesterId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        if (requester.getRole() != GroupRole.CHAIRPERSON) {
            throw new UnauthorizedException("Only Chairperson can delete announcements");
        }

        announcementRepository.delete(announcement);
    }

    @Override
    public AnnouncementResponse getAnnouncementById(UUID announcementId, UUID requesterId) {
        Announcement announcement = announcementRepository.findByIdWithDetails(announcementId)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found"));

        if (!groupMemberRepository.existsByGroupIdAndUserId(announcement.getGroup().getId(), requesterId)) {
            throw new UnauthorizedException("You are not a member of this group");
        }

        return announcementMapper.toResponse(announcement);
    }

    @Override
    public List<AnnouncementResponse> getGroupAnnouncements(UUID groupId, UUID requesterId) {
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, requesterId)) {
            throw new UnauthorizedException("You are not a member of this group");
        }

        return announcementRepository.findAllByGroupIdWithDetails(groupId).stream()
                .map(announcementMapper::toResponse)
                .collect(Collectors.toList());
    }
}