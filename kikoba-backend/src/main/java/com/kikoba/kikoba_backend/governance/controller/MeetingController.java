package com.kikoba.kikoba_backend.governance.controller;

import com.kikoba.kikoba_backend.governance.dto.*;
import com.kikoba.kikoba_backend.governance.enums.AttendanceStatus;
import com.kikoba.kikoba_backend.governance.service.MeetingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;

    /**
     * Schedule a meeting
     * POST /api/groups/{groupId}/meetings
     */
    @PostMapping("/groups/{groupId}/meetings")
    public ResponseEntity<MeetingResponse> scheduleMeeting(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID groupId,
            @Valid @RequestBody CreateMeetingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(meetingService.scheduleMeeting(userId, groupId, request));
    }

    /**
     * Start a meeting
     * PUT /api/meetings/{id}/start
     */
    @PutMapping("/meetings/{id}/start")
    public ResponseEntity<MeetingResponse> startMeeting(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(meetingService.startMeeting(id, userId));
    }

    /**
     * Complete a meeting
     * PUT /api/meetings/{id}/complete
     */
    @PutMapping("/meetings/{id}/complete")
    public ResponseEntity<MeetingResponse> completeMeeting(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id,
            @RequestBody(required = false) UpdateMinutesRequest request) {
        return ResponseEntity.ok(meetingService.completeMeeting(id, userId, request));
    }

    /**
     * Cancel a meeting
     * PUT /api/meetings/{id}/cancel
     */
    @PutMapping("/meetings/{id}/cancel")
    public ResponseEntity<MeetingResponse> cancelMeeting(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id,
            @RequestParam String reason) {
        return ResponseEntity.ok(meetingService.cancelMeeting(id, userId, reason));
    }

    /**
     * Update meeting minutes
     * PUT /api/meetings/{id}/minutes
     */
    @PutMapping("/meetings/{id}/minutes")
    public ResponseEntity<MeetingResponse> updateMinutes(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateMinutesRequest request) {
        return ResponseEntity.ok(meetingService.updateMinutes(id, userId, request));
    }

    /**
     * Get meeting by ID
     * GET /api/meetings/{id}
     */
    @GetMapping("/meetings/{id}")
    public ResponseEntity<MeetingResponse> getMeetingById(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(meetingService.getMeetingById(id, userId));
    }

    /**
     * Get all meetings for a group
     * GET /api/groups/{groupId}/meetings
     */
    @GetMapping("/groups/{groupId}/meetings")
    public ResponseEntity<List<MeetingResponse>> getGroupMeetings(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID groupId) {
        return ResponseEntity.ok(meetingService.getGroupMeetings(groupId, userId));
    }

    /**
     * Get upcoming meetings
     * GET /api/groups/{groupId}/meetings/upcoming
     */
    @GetMapping("/groups/{groupId}/meetings/upcoming")
    public ResponseEntity<List<MeetingResponse>> getUpcomingMeetings(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID groupId) {
        return ResponseEntity.ok(meetingService.getUpcomingMeetings(groupId, userId));
    }

    /**
     * Get attendance for a meeting
     * GET /api/meetings/{id}/attendance
     */
    @GetMapping("/meetings/{id}/attendance")
    public ResponseEntity<List<AttendanceResponse>> getAttendance(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(meetingService.getMeetingAttendance(id, userId));
    }

    /**
     * Mark self attendance (during ongoing meeting)
     * PUT /api/meetings/{id}/attendance/me
     */
    @PutMapping("/meetings/{id}/attendance/me")
    public ResponseEntity<AttendanceResponse> markSelfAttendance(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id,
            @RequestParam AttendanceStatus status) {
        return ResponseEntity.ok(meetingService.markSelfAttendance(id, userId, status));
    }

    /**
     * Update another member's attendance (Chairperson/Secretary)
     * PUT /api/meetings/{id}/attendance/{memberId}
     */
    @PutMapping("/meetings/{id}/attendance/{memberId}")
    public ResponseEntity<AttendanceResponse> updateAttendance(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id,
            @PathVariable UUID memberId,
            @RequestParam AttendanceStatus status) {
        return ResponseEntity.ok(meetingService.updateAttendance(id, memberId, userId, status));
    }

    /**
     * Record a user joining the meeting
     * POST /api/meetings/{id}/join
     */
    @PostMapping("/meetings/{id}/join")
    public ResponseEntity<Void> recordJoin(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id) {
        meetingService.recordJoin(id, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * Heartbeat — frontend pings every 30 seconds while user is present
     * POST /api/meetings/{id}/heartbeat
     */
    @PostMapping("/meetings/{id}/heartbeat")
    public ResponseEntity<Void> recordHeartbeat(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id) {
        meetingService.recordHeartbeat(id, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * Record a user leaving the meeting (called via sendBeacon on page close)
     * POST /api/meetings/{id}/leave
     */
    @PostMapping("/meetings/{id}/leave")
    public ResponseEntity<Void> recordLeave(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id) {
        meetingService.recordLeave(id, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * Get current user's attendance for this meeting
     * GET /api/meetings/{id}/attendance/me
     */
    @GetMapping("/meetings/{id}/attendance/me")
    public ResponseEntity<AttendanceResponse> getMyAttendance(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(meetingService.getMyAttendance(id, userId));
    }
}