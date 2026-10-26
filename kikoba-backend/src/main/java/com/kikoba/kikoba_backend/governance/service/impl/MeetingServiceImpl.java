package com.kikoba.kikoba_backend.governance.service.impl;

import com.kikoba.kikoba_backend.exception.BadRequestException;
import com.kikoba.kikoba_backend.exception.ResourceNotFoundException;
import com.kikoba.kikoba_backend.exception.UnauthorizedException;
import com.kikoba.kikoba_backend.governance.dto.*;
import com.kikoba.kikoba_backend.governance.entity.Meeting;
import com.kikoba.kikoba_backend.governance.entity.MeetingAttendance;
import com.kikoba.kikoba_backend.governance.enums.AttendanceStatus;
import com.kikoba.kikoba_backend.governance.enums.MeetingStatus;
import com.kikoba.kikoba_backend.governance.mapper.MeetingMapper;
import com.kikoba.kikoba_backend.governance.repository.MeetingAttendanceRepository;
import com.kikoba.kikoba_backend.governance.repository.MeetingRepository;
import com.kikoba.kikoba_backend.governance.service.MeetingService;
import com.kikoba.kikoba_backend.governance.util.JitsiRoomGenerator;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MeetingServiceImpl implements MeetingService {

    private static final int HEARTBEAT_TIMEOUT_SECONDS = 120;     // 2 minutes
    private static final double PRESENT_THRESHOLD = 0.80;         // ≥80%
    private static final double PARTIAL_THRESHOLD = 0.50;         // 50-80%
    private static final double LATE_THRESHOLD = 0.20;            // 20-50%

    private final MeetingRepository meetingRepository;
    private final MeetingAttendanceRepository attendanceRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final MeetingMapper meetingMapper;
    private final JitsiRoomGenerator jitsiRoomGenerator;
    private final NotificationService notificationService;

    // ============================================
    // SCHEDULE MEETING
    // ============================================
    @Override
    @Transactional
    public MeetingResponse scheduleMeeting(UUID userId, UUID groupId, CreateMeetingRequest request) {
        log.info("User {} scheduling meeting for group {}", userId, groupId);

        GroupMember requester = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        if (requester.getRole() != GroupRole.CHAIRPERSON && requester.getRole() != GroupRole.SECRETARY) {
            throw new UnauthorizedException("Only Chairperson or Secretary can schedule meetings");
        }

        Group group = requester.getGroup();

        // Generate Jitsi room
        String roomId = jitsiRoomGenerator.generateRoomId(groupId);
        String roomUrl = jitsiRoomGenerator.generateRoomUrl(roomId);

        Meeting meeting = Meeting.builder()
                .group(group)
                .title(request.getTitle())
                .agenda(request.getAgenda())
                .scheduledAt(request.getScheduledAt())
                .location(request.getLocation())
                .status(MeetingStatus.SCHEDULED)
                .videoMeetingId(roomId)
                .videoMeetingUrl(roomUrl)
                .createdBy(requester.getUser())
                .build();

        Meeting saved = meetingRepository.save(meeting);

        // Auto-create attendance records for all active members (status: ABSENT)
        List<GroupMember> members = groupMemberRepository.findByGroupIdAndIsActiveTrue(groupId);
        for (GroupMember member : members) {
            MeetingAttendance attendance = MeetingAttendance.builder()
                    .meeting(saved)
                    .groupMember(member)
                    .status(AttendanceStatus.ABSENT)
                    .build();
            attendanceRepository.save(attendance);
        }

        log.info("Meeting {} scheduled with room {}", saved.getId(), roomId);

        // Notify all active members
        String scheduledDate = saved.getScheduledAt().toLocalDate().toString();
        String scheduledTime = saved.getScheduledAt().toLocalTime().toString();

        for (GroupMember member : members) {
            notificationService.notifyUserAsync(
                    member.getUser().getId(),
                    NotificationType.MEETING_SCHEDULED,
                    "Meeting Scheduled: " + saved.getTitle(),
                    "A new meeting is scheduled for " + scheduledDate + " at " + scheduledTime
                            + ". Location: " + (saved.getLocation() != null ? saved.getLocation() : "Online"),
                    "/meetings/" + saved.getId(),
                    "Meeting",
                    saved.getId(),
                    NotificationPriority.NORMAL
            );
        }

        return meetingMapper.toResponse(saved, 0, members.size());
    }

    // ============================================
    // START MEETING
    // ============================================
    @Override
    @Transactional
    public MeetingResponse startMeeting(UUID meetingId, UUID requesterId) {
        Meeting meeting = meetingRepository.findByIdWithDetails(meetingId)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found"));

        Group group = meeting.getGroup();

        GroupMember requester = groupMemberRepository.findByGroupIdAndUserId(group.getId(), requesterId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        if (requester.getRole() != GroupRole.CHAIRPERSON && requester.getRole() != GroupRole.SECRETARY) {
            throw new UnauthorizedException("Only Chairperson or Secretary can start meetings");
        }

        if (meeting.getStatus() != MeetingStatus.SCHEDULED) {
            throw new BadRequestException("Only SCHEDULED meetings can be started. Current: " + meeting.getStatus());
        }

        meeting.setStatus(MeetingStatus.ONGOING);
        meeting.setStartedAt(LocalDateTime.now());

        Meeting saved = meetingRepository.save(meeting);

        long present = attendanceRepository.countByMeetingIdAndStatus(meetingId, AttendanceStatus.PRESENT);
        long absent = attendanceRepository.countByMeetingIdAndStatus(meetingId, AttendanceStatus.ABSENT);

        // Notify all members the meeting is starting
        List<GroupMember> allMembers = groupMemberRepository.findByGroupIdAndIsActiveTrue(group.getId());
        for (GroupMember member : allMembers) {
            notificationService.notifyUserAsync(
                    member.getUser().getId(),
                    NotificationType.MEETING_STARTED,
                    "Meeting Started: " + meeting.getTitle(),
                    "The meeting is now in progress. Join here: " + meeting.getVideoMeetingUrl(),
                    meeting.getVideoMeetingUrl(),
                    "Meeting",
                    meeting.getId(),
                    NotificationPriority.URGENT
            );
        }

        return meetingMapper.toResponse(saved, present, absent);
    }

    // ============================================
    // COMPLETE MEETING
    // ============================================
    @Override
    @Transactional
    public MeetingResponse completeMeeting(UUID meetingId, UUID requesterId, UpdateMinutesRequest request) {
        Meeting meeting = meetingRepository.findByIdWithDetails(meetingId)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found"));

        Group group = meeting.getGroup();

        GroupMember requester = groupMemberRepository.findByGroupIdAndUserId(group.getId(), requesterId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        if (requester.getRole() != GroupRole.CHAIRPERSON && requester.getRole() != GroupRole.SECRETARY) {
            throw new UnauthorizedException("Only Chairperson or Secretary can complete meetings");
        }

        if (meeting.getStatus() != MeetingStatus.ONGOING && meeting.getStatus() != MeetingStatus.SCHEDULED) {
            throw new BadRequestException("Meeting cannot be completed. Current: " + meeting.getStatus());
        }

        LocalDateTime now = LocalDateTime.now();
        meeting.setStatus(MeetingStatus.COMPLETED);
        meeting.setEndedAt(now);
        meeting.setCompletedBy(requester.getUser());

        if (request != null && request.getMinutes() != null) {
            meeting.setMinutes(request.getMinutes());
        }

        Meeting saved = meetingRepository.save(meeting);

        // ✅ Compute attendance for every participant
        computeFinalAttendance(meetingId, meeting.getStartedAt(), now);

        long present = attendanceRepository.countByMeetingIdAndStatus(meetingId, AttendanceStatus.PRESENT);
        long absent = attendanceRepository.countByMeetingIdAndStatus(meetingId, AttendanceStatus.ABSENT);

        log.info("Meeting {} completed. Present: {}, Absent: {}", meetingId, present, absent);

        return meetingMapper.toResponse(saved, present, absent);
    }

    // ============================================
    // CANCEL MEETING
    // ============================================
    @Override
    @Transactional
    public MeetingResponse cancelMeeting(UUID meetingId, UUID requesterId, String reason) {
        Meeting meeting = meetingRepository.findByIdWithDetails(meetingId)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found"));

        Group group = meeting.getGroup();

        GroupMember requester = groupMemberRepository.findByGroupIdAndUserId(group.getId(), requesterId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        if (requester.getRole() != GroupRole.CHAIRPERSON && requester.getRole() != GroupRole.SECRETARY) {
            throw new UnauthorizedException("Only Chairperson or Secretary can cancel meetings");
        }

        if (meeting.getStatus() == MeetingStatus.COMPLETED) {
            throw new BadRequestException("Cannot cancel a completed meeting");
        }

        meeting.setStatus(MeetingStatus.CANCELLED);
        meeting.setCancellationReason(reason);

        Meeting saved = meetingRepository.save(meeting);

        List<GroupMember> allMembers = groupMemberRepository.findByGroupIdAndIsActiveTrue(group.getId());
        for (GroupMember member : allMembers) {
            notificationService.notifyUserAsync(
                    member.getUser().getId(),
                    NotificationType.MEETING_CANCELLED,
                    "Meeting Cancelled: " + meeting.getTitle(),
                    "The scheduled meeting has been cancelled. Reason: " + reason,
                    "/meetings/" + meeting.getId(),
                    "Meeting",
                    meeting.getId(),
                    NotificationPriority.HIGH
            );
        }

        return meetingMapper.toResponse(saved, 0, 0);
    }

    // ============================================
    // UPDATE MINUTES
    // ============================================
    @Override
    @Transactional
    public MeetingResponse updateMinutes(UUID meetingId, UUID requesterId, UpdateMinutesRequest request) {
        Meeting meeting = meetingRepository.findByIdWithDetails(meetingId)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found"));

        Group group = meeting.getGroup();

        GroupMember requester = groupMemberRepository.findByGroupIdAndUserId(group.getId(), requesterId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        if (requester.getRole() != GroupRole.CHAIRPERSON && requester.getRole() != GroupRole.SECRETARY) {
            throw new UnauthorizedException("Only Chairperson or Secretary can update minutes");
        }

        if (meeting.getStatus() == MeetingStatus.COMPLETED) {
            throw new BadRequestException("Cannot edit minutes of a completed meeting");
        }

        meeting.setMinutes(request.getMinutes());
        Meeting saved = meetingRepository.save(meeting);

        long present = attendanceRepository.countByMeetingIdAndStatus(meetingId, AttendanceStatus.PRESENT);
        long absent = attendanceRepository.countByMeetingIdAndStatus(meetingId, AttendanceStatus.ABSENT);

        return meetingMapper.toResponse(saved, present, absent);
    }

    // ============================================
    // QUERY
    // ============================================
    @Override
    public MeetingResponse getMeetingById(UUID meetingId, UUID requesterId) {
        Meeting meeting = meetingRepository.findByIdWithDetails(meetingId)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found"));

        verifyMemberAccess(meeting.getGroup().getId(), requesterId);

        long present = attendanceRepository.countByMeetingIdAndStatus(meetingId, AttendanceStatus.PRESENT);
        long absent = attendanceRepository.countByMeetingIdAndStatus(meetingId, AttendanceStatus.ABSENT);

        return meetingMapper.toResponse(meeting, present, absent);
    }

    @Override
    public List<MeetingResponse> getGroupMeetings(UUID groupId, UUID requesterId) {
        verifyMemberAccess(groupId, requesterId);

        return meetingRepository.findAllByGroupIdWithDetails(groupId).stream()
                .map(m -> {
                    long present = attendanceRepository.countByMeetingIdAndStatus(m.getId(), AttendanceStatus.PRESENT);
                    long absent = attendanceRepository.countByMeetingIdAndStatus(m.getId(), AttendanceStatus.ABSENT);
                    return meetingMapper.toResponse(m, present, absent);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<MeetingResponse> getUpcomingMeetings(UUID groupId, UUID requesterId) {
        verifyMemberAccess(groupId, requesterId);

        return meetingRepository.findUpcomingByGroupId(groupId, LocalDateTime.now()).stream()
                .map(m -> {
                    long present = attendanceRepository.countByMeetingIdAndStatus(m.getId(), AttendanceStatus.PRESENT);
                    long absent = attendanceRepository.countByMeetingIdAndStatus(m.getId(), AttendanceStatus.ABSENT);
                    return meetingMapper.toResponse(m, present, absent);
                })
                .collect(Collectors.toList());
    }

    // ============================================
    // ATTENDANCE
    // ============================================
    @Override
    public List<AttendanceResponse> getMeetingAttendance(UUID meetingId, UUID requesterId) {
        Meeting meeting = meetingRepository.findByIdWithDetails(meetingId)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found"));

        verifyMemberAccess(meeting.getGroup().getId(), requesterId);

        return attendanceRepository.findAllByMeetingIdWithDetails(meetingId).stream()
                .map(meetingMapper::toAttendanceResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AttendanceResponse updateAttendance(UUID meetingId, UUID memberId, UUID requesterId,
                                               AttendanceStatus status) {
        Meeting meeting = meetingRepository.findByIdWithDetails(meetingId)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found"));

        Group group = meeting.getGroup();

        GroupMember requester = groupMemberRepository.findByGroupIdAndUserId(group.getId(), requesterId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        if (requester.getRole() != GroupRole.CHAIRPERSON && requester.getRole() != GroupRole.SECRETARY) {
            throw new UnauthorizedException("Only Chairperson or Secretary can update attendance");
        }

        MeetingAttendance attendance = attendanceRepository.findByMeetingIdAndGroupMemberId(meetingId, memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found"));

        attendance.setStatus(status);
        if (status == AttendanceStatus.PRESENT && attendance.getJoinedAt() == null) {
            attendance.setJoinedAt(LocalDateTime.now());
        }

        MeetingAttendance saved = attendanceRepository.save(attendance);
        return meetingMapper.toAttendanceResponse(saved);
    }

    @Override
    @Transactional
    public AttendanceResponse markSelfAttendance(UUID meetingId, UUID userId, AttendanceStatus status) {
        Meeting meeting = meetingRepository.findByIdWithDetails(meetingId)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found"));

        Group group = meeting.getGroup();

        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(group.getId(), userId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        if (meeting.getStatus() != MeetingStatus.ONGOING) {
            throw new BadRequestException("Can only mark attendance during an ongoing meeting");
        }

        MeetingAttendance attendance = attendanceRepository.findByMeetingIdAndGroupMemberId(meetingId, member.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found"));

        attendance.setStatus(status);
        attendance.setRsvpAt(LocalDateTime.now());
        if (status == AttendanceStatus.PRESENT) {
            attendance.setJoinedAt(LocalDateTime.now());
        }

        MeetingAttendance saved = attendanceRepository.save(attendance);
        return meetingMapper.toAttendanceResponse(saved);
    }

    // ============================================
    // HELPERS
    // ============================================
    private void verifyMemberAccess(UUID groupId, UUID requesterId) {
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, requesterId)) {
            throw new UnauthorizedException("You are not a member of this group");
        }
    }

    // ============================================
    // ATTENDANCE TRACKING
    // ============================================

    @Override
    @Transactional
    public void recordJoin(UUID meetingId, UUID userId) {
        Meeting meeting = meetingRepository.findByIdWithDetails(meetingId)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found"));

        if (meeting.getStatus() != MeetingStatus.ONGOING) {
            log.debug("Ignoring join event — meeting not ongoing: {}", meetingId);
            return;
        }

        GroupMember member = groupMemberRepository
                .findByGroupIdAndUserId(meeting.getGroup().getId(), userId)
                .orElseThrow(() -> new UnauthorizedException("Not a member of this group"));

        MeetingAttendance attendance = attendanceRepository
                .findByMeetingIdAndGroupMemberId(meetingId, member.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found"));

        LocalDateTime now = LocalDateTime.now();

        if (attendance.getFirstJoinedAt() == null) {
            attendance.setFirstJoinedAt(now);
            attendance.setJoinedAt(now);
        }

        // If a stale session exists, close it first
        if (attendance.getCurrentSessionStartedAt() != null
                && attendance.getLastHeartbeatAt() != null
                && attendance.getLastHeartbeatAt().plusSeconds(HEARTBEAT_TIMEOUT_SECONDS).isBefore(now)) {
            closeCurrentSession(attendance, attendance.getLastHeartbeatAt());
        }

        // Start a new session if none active
        if (attendance.getCurrentSessionStartedAt() == null) {
            attendance.setCurrentSessionStartedAt(now);
            attendance.setSessionCount((attendance.getSessionCount() == null ? 0 : attendance.getSessionCount()) + 1);
        }

        attendance.setLastHeartbeatAt(now);

        attendanceRepository.save(attendance);
        log.debug("Join recorded: meeting={}, member={}", meetingId, member.getId());
    }

    @Override
    @Transactional
    public void recordHeartbeat(UUID meetingId, UUID userId) {
        Meeting meeting = meetingRepository.findByIdWithDetails(meetingId)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found"));

        if (meeting.getStatus() != MeetingStatus.ONGOING) {
            return;
        }

        GroupMember member = groupMemberRepository
                .findByGroupIdAndUserId(meeting.getGroup().getId(), userId)
                .orElseThrow(() -> new UnauthorizedException("Not a member of this group"));

        MeetingAttendance attendance = attendanceRepository
                .findByMeetingIdAndGroupMemberId(meetingId, member.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found"));

        LocalDateTime now = LocalDateTime.now();

        // If session was auto-closed due to timeout, restart it
        if (attendance.getCurrentSessionStartedAt() == null) {
            attendance.setCurrentSessionStartedAt(now);
            attendance.setSessionCount((attendance.getSessionCount() == null ? 0 : attendance.getSessionCount()) + 1);
        }

        attendance.setLastHeartbeatAt(now);
        attendance.setHeartbeatCount((attendance.getHeartbeatCount() == null ? 0 : attendance.getHeartbeatCount()) + 1);

        attendanceRepository.save(attendance);
    }

    @Override
    @Transactional
    public void recordLeave(UUID meetingId, UUID userId) {
        Meeting meeting = meetingRepository.findByIdWithDetails(meetingId)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found"));

        GroupMember member = groupMemberRepository
                .findByGroupIdAndUserId(meeting.getGroup().getId(), userId)
                .orElseThrow(() -> new UnauthorizedException("Not a member of this group"));

        MeetingAttendance attendance = attendanceRepository
                .findByMeetingIdAndGroupMemberId(meetingId, member.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found"));

        if (attendance.getCurrentSessionStartedAt() != null) {
            closeCurrentSession(attendance, LocalDateTime.now());
        }

        attendance.setLeftAt(LocalDateTime.now());
        attendanceRepository.save(attendance);

        log.debug("Leave recorded: meeting={}, member={}", meetingId, member.getId());
    }

    @Override
    public AttendanceResponse getMyAttendance(UUID meetingId, UUID userId) {
        Meeting meeting = meetingRepository.findByIdWithDetails(meetingId)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found"));

        GroupMember member = groupMemberRepository
                .findByGroupIdAndUserId(meeting.getGroup().getId(), userId)
                .orElseThrow(() -> new UnauthorizedException("Not a member of this group"));

        MeetingAttendance attendance = attendanceRepository
                .findByMeetingIdAndGroupMemberId(meetingId, member.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found"));

        return meetingMapper.toAttendanceResponse(attendance);
    }

    /**
     * Close the currently active session and add its duration to the total.
     */
    private void closeCurrentSession(MeetingAttendance attendance, LocalDateTime closeAt) {
        if (attendance.getCurrentSessionStartedAt() == null) return;

        long seconds = java.time.temporal.ChronoUnit.SECONDS
                .between(attendance.getCurrentSessionStartedAt(), closeAt);
        if (seconds < 0) seconds = 0;

        int current = attendance.getTotalDurationSeconds() == null ? 0 : attendance.getTotalDurationSeconds();
        attendance.setTotalDurationSeconds(current + (int) seconds);

        attendance.setCurrentSessionStartedAt(null);
    }

    /**
     * Compute the final attendance status for every member of the meeting.
     * Called when the meeting is marked COMPLETED.
     */
    private void computeFinalAttendance(UUID meetingId, LocalDateTime startedAt, LocalDateTime endedAt) {
        if (startedAt == null) startedAt = endedAt;
        long meetingDuration = java.time.temporal.ChronoUnit.SECONDS.between(startedAt, endedAt);
        if (meetingDuration <= 0) meetingDuration = 1;

        List<MeetingAttendance> allAttendance = attendanceRepository.findAllByMeetingIdWithDetails(meetingId);

        for (MeetingAttendance attendance : allAttendance) {
            // Only compute for people who joined
            if (attendance.getFirstJoinedAt() == null) {
                // They never joined at all
                if (attendance.getStatus() == AttendanceStatus.PRESENT
                        || attendance.getStatus() == AttendanceStatus.LATE) {
                    // Someone manually marked them present — keep it
                    attendance.setComputedStatusReason("Manually marked by manager");
                } else {
                    attendance.setStatus(AttendanceStatus.ABSENT);
                    attendance.setComputedStatusReason("Never joined");
                }
                attendanceRepository.save(attendance);
                continue;
            }

            // Close any still-open session
            if (attendance.getCurrentSessionStartedAt() != null) {
                // Close at last heartbeat (if recent) or at meeting end
                LocalDateTime closeAt = endedAt;
                if (attendance.getLastHeartbeatAt() != null
                        && attendance.getLastHeartbeatAt().isBefore(endedAt)) {
                    closeAt = attendance.getLastHeartbeatAt();
                }
                closeCurrentSession(attendance, closeAt);
            }

            int totalSeconds = attendance.getTotalDurationSeconds() == null
                    ? 0 : attendance.getTotalDurationSeconds();
            double ratio = (double) totalSeconds / meetingDuration;

            AttendanceStatus computed;
            String reason;

            if (ratio >= PRESENT_THRESHOLD) {
                computed = AttendanceStatus.PRESENT;
                reason = String.format("Attended %.0f%% of meeting (%ds of %ds)",
                        ratio * 100, totalSeconds, meetingDuration);
            } else if (ratio >= PARTIAL_THRESHOLD) {
                computed = AttendanceStatus.PARTIAL;
                reason = String.format("Partial attendance: %.0f%% (%ds of %ds)",
                        ratio * 100, totalSeconds, meetingDuration);
            } else if (ratio >= LATE_THRESHOLD) {
                computed = AttendanceStatus.LATE;
                reason = String.format("Late/early leave: %.0f%% (%ds of %ds)",
                        ratio * 100, totalSeconds, meetingDuration);
            } else {
                computed = AttendanceStatus.ABSENT;
                reason = String.format("Insufficient attendance: %.0f%% (%ds of %ds)",
                        ratio * 100, totalSeconds, meetingDuration);
            }

            attendance.setStatus(computed);
            attendance.setDurationSeconds(totalSeconds);
            attendance.setComputedStatusReason(reason);
            attendanceRepository.save(attendance);
        }
    }
}