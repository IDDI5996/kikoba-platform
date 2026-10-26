package com.kikoba.kikoba_backend.governance.repository;

import com.kikoba.kikoba_backend.governance.entity.Announcement;
import com.kikoba.kikoba_backend.governance.enums.AnnouncementCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, UUID> {

    List<Announcement> findByGroupIdOrderByPinnedDescCreatedAtDesc(UUID groupId);

    List<Announcement> findByGroupIdAndCategoryOrderByCreatedAtDesc(UUID groupId, AnnouncementCategory category);

    @Query("""
        SELECT a FROM Announcement a
        JOIN FETCH a.group g
        LEFT JOIN FETCH a.createdBy
        WHERE a.id = :id
    """)
    Optional<Announcement> findByIdWithDetails(@Param("id") UUID id);

    @Query("""
        SELECT a FROM Announcement a
        JOIN FETCH a.group g
        LEFT JOIN FETCH a.createdBy
        WHERE a.group.id = :groupId
        ORDER BY a.pinned DESC, a.createdAt DESC
    """)
    List<Announcement> findAllByGroupIdWithDetails(@Param("groupId") UUID groupId);
}