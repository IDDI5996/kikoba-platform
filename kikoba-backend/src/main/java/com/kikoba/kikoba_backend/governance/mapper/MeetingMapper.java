package com.kikoba.kikoba_backend.governance.mapper;

import com.kikoba.kikoba_backend.governance.dto.AttendanceResponse;
import com.kikoba.kikoba_backend.governance.dto.MeetingResponse;
import com.kikoba.kikoba_backend.governance.entity.Meeting;
import com.kikoba.kikoba_backend.governance.entity.MeetingAttendance;
import org.springframework.stereotype.Component;

@Component
public class MeetingMapper {

    public MeetingResponse toResponse(Meeting meeting, long present, long absent) {
        if (meeting == null) return null;

        String createdByName = null;
        if (meeting.getCreatedBy() != null) {
            createdByName = meeting.getCreatedBy().getFirstName() + " "
                    + meeting.getCreatedBy().getLastName();
        }

        return MeetingResponse.builder()
                .id(meeting.getId())
                .groupId(meeting.getGroup().getId())
                .groupName(meeting.getGroup().getName())
                .title(meeting.getTitle())
                .agenda(meeting.getAgenda())
                .minutes(meeting.getMinutes())
                .scheduledAt(meeting.getScheduledAt())
                .startedAt(meeting.getStartedAt())
                .endedAt(meeting.getEndedAt())
                .status(meeting.getStatus())
                .location(meeting.getLocation())
                .videoMeetingUrl(meeting.getVideoMeetingUrl())
                .videoMeetingId(meeting.getVideoMeetingId())
                .createdBy(meeting.getCreatedBy() != null ? meeting.getCreatedBy().getId() : null)
                .createdByName(createdByName)
                .completedBy(meeting.getCompletedBy() != null ? meeting.getCompletedBy().getId() : null)
                .cancellationReason(meeting.getCancellationReason())
                .presentCount(present)
                .absentCount(absent)
                .createdAt(meeting.getCreatedAt())
                .build();
    }

    public AttendanceResponse toAttendanceResponse(MeetingAttendance attendance) {
        if (attendance == null) return null;

        var member = attendance.getGroupMember();
        var user = member.getUser();

        return AttendanceResponse.builder()
                .id(attendance.getId())
                .meetingId(attendance.getMeeting().getId())
                .memberId(member.getId())
                .userId(user.getId())
                .memberName(user.getFirstName() + " " + user.getLastName())
                .status(attendance.getStatus())
                .rsvpAt(attendance.getRsvpAt())
                .joinedAt(attendance.getJoinedAt())
                .firstJoinedAt(attendance.getFirstJoinedAt())
                .lastHeartbeatAt(attendance.getLastHeartbeatAt())
                .leftAt(attendance.getLeftAt())
                .durationSeconds(attendance.getTotalDurationSeconds())
                .totalDurationSeconds(attendance.getTotalDurationSeconds())
                .heartbeatCount(attendance.getHeartbeatCount())
                .sessionCount(attendance.getSessionCount())
                .computedStatusReason(attendance.getComputedStatusReason())
                .notes(attendance.getNotes())
                .build();
    }
}