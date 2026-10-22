package com.typeflow.service;

import com.typeflow.dto.LeaderboardEntryDto;
import com.typeflow.entity.TypingResult;
import com.typeflow.entity.User;
import com.typeflow.repository.TypingResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class LeaderboardService {

    private final TypingResultRepository typingResultRepository;

    @Autowired
    public LeaderboardService(TypingResultRepository typingResultRepository) {
        this.typingResultRepository = typingResultRepository;
    }

    /**
     * Retrieves the rolling 24-hour leaderboard rankings.
     * Only scores with completionTimestamp >= (now - 24 hours) are eligible.
     * Takes each user's best qualifying score in the window.
     * Tie-breaker 1: Accuracy (descending)
     * Tie-breaker 2: Completion timestamp (earlier is better)
     */
    public List<LeaderboardEntryDto> get24HourLeaderboard(Integer duration, Long currentUserId) {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        List<TypingResult> rawList = typingResultRepository.findQualifyingLeaderboardResults(cutoff, duration);

        // Group by user, pick the best qualifying score
        Map<Long, TypingResult> bestPerUser = new LinkedHashMap<>();
        for (TypingResult tr : rawList) {
            if (tr.getUser() == null) continue;
            Long uid = tr.getUser().getId();
            if (!bestPerUser.containsKey(uid)) {
                bestPerUser.put(uid, tr);
            } else {
                TypingResult currentBest = bestPerUser.get(uid);
                if (isBetterScore(tr, currentBest)) {
                    bestPerUser.put(uid, tr);
                }
            }
        }

        // Sort all distinct best results: Net WPM desc, Accuracy desc, CompletionTimestamp asc
        List<TypingResult> sortedResults = new ArrayList<>(bestPerUser.values());
        sortedResults.sort((a, b) -> {
            int cmpWpm = Double.compare(b.getNetWpm(), a.getNetWpm());
            if (cmpWpm != 0) return cmpWpm;
            int cmpAcc = Double.compare(b.getAccuracy(), a.getAccuracy());
            if (cmpAcc != 0) return cmpAcc;
            return a.getCompletionTimestamp().compareTo(b.getCompletionTimestamp());
        });

        // Convert to DTOs and assign ranks
        List<LeaderboardEntryDto> leaderboard = new ArrayList<>();
        int rank = 1;
        LocalDateTime now = LocalDateTime.now();

        for (TypingResult tr : sortedResults) {
            LeaderboardEntryDto dto = new LeaderboardEntryDto();
            dto.setRank(rank++);
            dto.setResultId(tr.getId());
            dto.setUserId(tr.getUser().getId());
            dto.setUsername(tr.getUser().getUsername());
            dto.setAvatar(tr.getUser().getAvatar());
            dto.setNetWpm(tr.getNetWpm());
            dto.setRawWpm(tr.getRawWpm());
            dto.setAccuracy(tr.getAccuracy());
            dto.setDuration(tr.getDuration());
            dto.setCompletionTimestamp(tr.getCompletionTimestamp());
            dto.setTimeRemainingFormatted(formatTimeRemaining(tr.getExpirationTimestamp(), now));
            dto.setCurrentUser(currentUserId != null && currentUserId.equals(tr.getUser().getId()));
            leaderboard.add(dto);
        }

        return leaderboard;
    }

    /**
     * Determines whether result 'candidate' is superior to 'currentBest'.
     */
    public boolean isBetterScore(TypingResult candidate, TypingResult currentBest) {
        if (candidate.getNetWpm() > currentBest.getNetWpm()) return true;
        if (candidate.getNetWpm() < currentBest.getNetWpm()) return false;
        // First tie-breaker: Accuracy
        if (candidate.getAccuracy() > currentBest.getAccuracy()) return true;
        if (candidate.getAccuracy() < currentBest.getAccuracy()) return false;
        // Second tie-breaker: earlier completion
        return candidate.getCompletionTimestamp().isBefore(currentBest.getCompletionTimestamp());
    }

    /**
     * Formats duration until 24-hour expiration as "Xh Ym remaining".
     */
    public String formatTimeRemaining(LocalDateTime expiration, LocalDateTime now) {
        if (expiration == null || now.isAfter(expiration)) {
            return "Expired";
        }
        Duration diff = Duration.between(now, expiration);
        long hours = diff.toHours();
        long minutes = diff.toMinutesPart();
        if (hours > 0) {
            return hours + "h " + minutes + "m remaining";
        }
        long seconds = diff.toSecondsPart();
        return minutes + "m " + seconds + "s remaining";
    }

    /**
     * Retrieves recent qualifying activity within the 24-hour window.
     */
    public List<LeaderboardEntryDto> getRecentActivity(int limit) {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        List<TypingResult> recents = typingResultRepository.findRecentActiveScores(cutoff, PageRequest.of(0, limit));
        List<LeaderboardEntryDto> list = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (TypingResult tr : recents) {
            LeaderboardEntryDto dto = new LeaderboardEntryDto();
            dto.setResultId(tr.getId());
            dto.setUserId(tr.getUser().getId());
            dto.setUsername(tr.getUser().getUsername());
            dto.setAvatar(tr.getUser().getAvatar());
            dto.setNetWpm(tr.getNetWpm());
            dto.setRawWpm(tr.getRawWpm());
            dto.setAccuracy(tr.getAccuracy());
            dto.setDuration(tr.getDuration());
            dto.setCompletionTimestamp(tr.getCompletionTimestamp());
            dto.setTimeRemainingFormatted(formatTimeRemaining(tr.getExpirationTimestamp(), now));
            list.add(dto);
        }
        return list;
    }

    /**
     * Finds the current rank of a user within the 24h leaderboard for a given duration.
     */
    public Integer getUserRank(Long userId, Integer duration) {
        if (userId == null) return null;
        List<LeaderboardEntryDto> lb = get24HourLeaderboard(duration, userId);
        for (LeaderboardEntryDto entry : lb) {
            if (entry.getUserId().equals(userId)) {
                return entry.getRank();
            }
        }
        return null;
    }
}
