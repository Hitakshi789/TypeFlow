package com.typeflow.repository;

import com.typeflow.entity.TypingResult;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TypingResultRepository extends JpaRepository<TypingResult, Long> {

    List<TypingResult> findByUserIdOrderByCompletionTimestampDesc(Long userId);

    List<TypingResult> findByUserIdOrderByCompletionTimestampDesc(Long userId, Pageable pageable);

    List<TypingResult> findByUserIdAndDurationOrderByCompletionTimestampDesc(Long userId, Integer duration);

    void deleteByUserId(Long userId);

    // Leaderboard query: retrieve all eligible scores within rolling 24 hours
    @Query("SELECT tr FROM TypingResult tr JOIN FETCH tr.user u " +
           "WHERE tr.user IS NOT NULL " +
           "AND tr.totalChars > 0 " +
           "AND tr.duration IN (30, 60, 120) " +
           "AND tr.completionTimestamp >= :since " +
           "AND (:duration IS NULL OR tr.duration = :duration) " +
           "ORDER BY tr.netWpm DESC, tr.accuracy DESC, tr.completionTimestamp ASC")
    List<TypingResult> findQualifyingLeaderboardResults(
            @Param("since") LocalDateTime since,
            @Param("duration") Integer duration
    );

    // Recent activity: newly recorded scores in the last 24h
    @Query("SELECT tr FROM TypingResult tr JOIN FETCH tr.user u " +
           "WHERE tr.user IS NOT NULL " +
           "AND tr.totalChars > 0 " +
           "AND tr.duration IN (30, 60, 120) " +
           "AND tr.completionTimestamp >= :since " +
           "ORDER BY tr.completionTimestamp DESC")
    List<TypingResult> findRecentActiveScores(
            @Param("since") LocalDateTime since,
            Pageable pageable
    );

    // Total test counts
    long countByUserId(Long userId);
}
