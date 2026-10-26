package com.kikoba.kikoba_backend.governance.repository;

import com.kikoba.kikoba_backend.governance.entity.MeetingAttendance;
import com.kikoba.kikoba_backend.governance.enums.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MeetingAttendanceRepository extends JpaRepository<MeetingAttendance, UUID> {

    List<MeetingAttendance> findByMeetingId(UUID meetingId);

    Optional<MeetingAttendance> findByMeetingIdAndGroupMemberId(UUID meetingId, UUID groupMemberId);

    long countByMeetingIdAndStatus(UUID meetingId, AttendanceStatus status);

    @Query("""
        SELECT a FROM MeetingAttendance a
        JOIN FETCH a.meeting m
        JOIN FETCH a.groupMember gm
        JOIN FETCH gm.user u
        JOIN FETCH gm.group g
        WHERE a.id = :id
    """)
    Optional<MeetingAttendance> findByIdWithDetails(@Param("id") UUID id);

    @Query("""
        SELECT a FROM MeetingAttendance a
        JOIN FETCH a.groupMember gm
        JOIN FETCH gm.user u
        WHERE a.meeting.id = :meetingId
        ORDER BY u.firstName ASC
    """)
    List<MeetingAttendance> findAllByMeetingIdWithDetails(@Param("meetingId") UUID meetingId);
}