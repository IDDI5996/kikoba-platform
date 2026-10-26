package com.kikoba.kikoba_backend.governance.repository;

import com.kikoba.kikoba_backend.governance.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VoteRepository extends JpaRepository<Vote, UUID> {

    List<Vote> findByPollId(UUID pollId);

    List<Vote> findByPollIdAndGroupMemberId(UUID pollId, UUID groupMemberId);

    Optional<Vote> findByPollIdAndGroupMemberIdAndChoice(UUID pollId, UUID groupMemberId, String choice);

    boolean existsByPollIdAndGroupMemberId(UUID pollId, UUID groupMemberId);

    long countByPollId(UUID pollId);

    @Query("""
        SELECT v.choice, COUNT(v) FROM Vote v
        WHERE v.poll.id = :pollId
        GROUP BY v.choice
    """)
    List<Object[]> countVotesByChoice(@Param("pollId") UUID pollId);

    @Query("""
        SELECT DISTINCT v.groupMember.id FROM Vote v
        WHERE v.poll.id = :pollId
    """)
    List<UUID> findVoterMemberIds(@Param("pollId") UUID pollId);
}