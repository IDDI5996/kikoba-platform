package com.kikoba.kikoba_backend.governance.controller;

import com.kikoba.kikoba_backend.governance.dto.AnnouncementResponse;
import com.kikoba.kikoba_backend.governance.dto.CreateAnnouncementRequest;
import com.kikoba.kikoba_backend.governance.service.AnnouncementService;
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
public class AnnouncementController {

    private final AnnouncementService announcementService;

    /**
     * Create an announcement
     * POST /api/groups/{groupId}/announcements
     */
    @PostMapping("/groups/{groupId}/announcements")
    public ResponseEntity<AnnouncementResponse> createAnnouncement(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID groupId,
            @Valid @RequestBody CreateAnnouncementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(announcementService.createAnnouncement(userId, groupId, request));
    }

    /**
     * Update an announcement
     * PUT /api/announcements/{id}
     */
    @PutMapping("/announcements/{id}")
    public ResponseEntity<AnnouncementResponse> updateAnnouncement(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody CreateAnnouncementRequest request) {
        return ResponseEntity.ok(announcementService.updateAnnouncement(id, userId, request));
    }

    /**
     * Toggle pin status
     * PUT /api/announcements/{id}/pin
     */
    @PutMapping("/announcements/{id}/pin")
    public ResponseEntity<AnnouncementResponse> togglePin(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(announcementService.togglePin(id, userId));
    }

    /**
     * Delete an announcement
     * DELETE /api/announcements/{id}
     */
    @DeleteMapping("/announcements/{id}")
    public ResponseEntity<Void> deleteAnnouncement(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id) {
        announcementService.deleteAnnouncement(id, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get announcement by ID
     * GET /api/announcements/{id}
     */
    @GetMapping("/announcements/{id}")
    public ResponseEntity<AnnouncementResponse> getAnnouncementById(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(announcementService.getAnnouncementById(id, userId));
    }

    /**
     * Get all group announcements
     * GET /api/groups/{groupId}/announcements
     */
    @GetMapping("/groups/{groupId}/announcements")
    public ResponseEntity<List<AnnouncementResponse>> getGroupAnnouncements(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID groupId) {
        return ResponseEntity.ok(announcementService.getGroupAnnouncements(groupId, userId));
    }
}