package com.kikoba.kikoba_backend.governance.service;

import com.kikoba.kikoba_backend.governance.dto.*;
import com.kikoba.kikoba_backend.governance.enums.AttendanceStatus;

import java.util.List;
import java.util.UUID;

public interface MeetingService {

    MeetingResponse scheduleMeeting(UUID userId, UUID groupId, CreateMeetingRequest request);
    MeetingResponse startMeeting(UUID meetingId, UUID requesterId);
    MeetingResponse completeMeeting(UUID meetingId, UUID requesterId, UpdateMinutesRequest request);
    MeetingResponse cancelMeeting(UUID meetingId, UUID requesterId, String reason);
    MeetingResponse updateMinutes(UUID meetingId, UUID requesterId, UpdateMinutesRequest request);

    MeetingResponse getMeetingById(UUID meetingId, UUID requesterId);
    List<MeetingResponse> getGroupMeetings(UUID groupId, UUID requesterId);
    List<MeetingResponse> getUpcomingMeetings(UUID groupId, UUID requesterId);

    List<AttendanceResponse> getMeetingAttendance(UUID meetingId, UUID requesterId);
    AttendanceResponse updateAttendance(UUID meetingId, UUID memberId, UUID requesterId, AttendanceStatus status);
    AttendanceResponse markSelfAttendance(UUID meetingId, UUID userId, AttendanceStatus status);

    void recordJoin(UUID meetingId, UUID userId);
    void recordHeartbeat(UUID meetingId, UUID userId);
    void recordLeave(UUID meetingId, UUID userId);
    AttendanceResponse getMyAttendance(UUID meetingId, UUID userId);
}