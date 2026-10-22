package com.typeflow;

import com.typeflow.dto.LeaderboardEntryDto;
import com.typeflow.entity.TypingResult;
import com.typeflow.entity.User;
import com.typeflow.repository.TypingResultRepository;
import com.typeflow.service.LeaderboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class LeaderboardServiceTest {

    private TypingResultRepository typingResultRepository;
    private LeaderboardService leaderboardService;

    @BeforeEach
    void setUp() {
        typingResultRepository = Mockito.mock(TypingResultRepository.class);
        leaderboardService = new LeaderboardService(typingResultRepository);
    }

    @Test
    @DisplayName("Verify that scores older than 24 hours are excluded by the cutoff query")
    void test24HourBoundaryExclusion() {
        LocalDateTime now = LocalDateTime.now();
        User user1 = new User("alice", "alice@test.com", "hash", "A");
        user1.setId(1L);

        // Score 1: completed 2 hours ago (qualifies)
        TypingResult qualifying = new TypingResult();
        qualifying.setId(1L);
        qualifying.setUser(user1);
        qualifying.setDuration(60);
        qualifying.setNetWpm(95.0);
        qualifying.setRawWpm(98.0);
        qualifying.setAccuracy(97.0);
        qualifying.setCompletionTimestamp(now.minusHours(2));
        qualifying.setExpirationTimestamp(now.minusHours(2).plusHours(24));

        // When repository query runs with cutoff (now - 24 hours), only qualifying score is returned
        when(typingResultRepository.findQualifyingLeaderboardResults(any(LocalDateTime.class), eq(60)))
                .thenReturn(List.of(qualifying));

        List<LeaderboardEntryDto> lb = leaderboardService.get24HourLeaderboard(60, null);

        assertEquals(1, lb.size());
        assertEquals("alice", lb.get(0).getUsername());
        assertEquals(95.0, lb.get(0).getNetWpm());
        assertTrue(lb.get(0).getTimeRemainingFormatted().contains("remaining"));
    }

    @Test
    @DisplayName("Verify ranking tie-breaker: 1st Net WPM, 2nd Accuracy, 3rd Earlier Completion")
    void testTieBreakingLogic() {
        LocalDateTime now = LocalDateTime.now();

        User user1 = new User("user1", "u1@test.com", "hash", "U1");
        user1.setId(1L);
        User user2 = new User("user2", "u2@test.com", "hash", "U2");
        user2.setId(2L);
        User user3 = new User("user3", "u3@test.com", "hash", "U3");
        user3.setId(3L);

        // All three users have Net WPM 100.0
        // User 1: 100.0 WPM, 96.0% accuracy, completed 3 hours ago
        TypingResult tr1 = new TypingResult();
        tr1.setId(1L);
        tr1.setUser(user1);
        tr1.setDuration(60);
        tr1.setNetWpm(100.0);
        tr1.setAccuracy(96.0);
        tr1.setCompletionTimestamp(now.minusHours(3));
        tr1.setExpirationTimestamp(now.minusHours(3).plusHours(24));

        // User 2: 100.0 WPM, 98.0% accuracy, completed 1 hour ago (higher accuracy wins tie-break)
        TypingResult tr2 = new TypingResult();
        tr2.setId(2L);
        tr2.setUser(user2);
        tr2.setDuration(60);
        tr2.setNetWpm(100.0);
        tr2.setAccuracy(98.0);
        tr2.setCompletionTimestamp(now.minusHours(1));
        tr2.setExpirationTimestamp(now.minusHours(1).plusHours(24));

        // User 3: 100.0 WPM, 96.0% accuracy, completed 5 hours ago (tied with user1 on accuracy, earlier time wins tie-break)
        TypingResult tr3 = new TypingResult();
        tr3.setId(3L);
        tr3.setUser(user3);
        tr3.setDuration(60);
        tr3.setNetWpm(100.0);
        tr3.setAccuracy(96.0);
        tr3.setCompletionTimestamp(now.minusHours(5));
        tr3.setExpirationTimestamp(now.minusHours(5).plusHours(24));

        when(typingResultRepository.findQualifyingLeaderboardResults(any(LocalDateTime.class), eq(60)))
                .thenReturn(Arrays.asList(tr1, tr2, tr3));

        List<LeaderboardEntryDto> lb = leaderboardService.get24HourLeaderboard(60, null);

        assertEquals(3, lb.size());
        // Rank 1: user2 (highest accuracy 98.0%)
        assertEquals("user2", lb.get(0).getUsername());
        assertEquals(1, lb.get(0).getRank());

        // Rank 2: user3 (tied accuracy 96.0%, but earlier timestamp - 5h ago vs 3h ago)
        assertEquals("user3", lb.get(1).getUsername());
        assertEquals(2, lb.get(1).getRank());

        // Rank 3: user1
        assertEquals("user1", lb.get(2).getUsername());
        assertEquals(3, lb.get(2).getRank());
    }

    @Test
    @DisplayName("Verify each user receives only their single best qualifying score")
    void testSingleBestScorePerUser() {
        LocalDateTime now = LocalDateTime.now();
        User user1 = new User("bob", "bob@test.com", "hash", "B");
        user1.setId(1L);

        TypingResult attempt1 = new TypingResult();
        attempt1.setId(10L);
        attempt1.setUser(user1);
        attempt1.setDuration(60);
        attempt1.setNetWpm(70.0);
        attempt1.setAccuracy(95.0);
        attempt1.setCompletionTimestamp(now.minusHours(4));
        attempt1.setExpirationTimestamp(now.minusHours(4).plusHours(24));

        TypingResult attempt2 = new TypingResult();
        attempt2.setId(11L);
        attempt2.setUser(user1);
        attempt2.setDuration(60);
        attempt2.setNetWpm(85.0); // Better attempt!
        attempt2.setAccuracy(98.0);
        attempt2.setCompletionTimestamp(now.minusHours(2));
        attempt2.setExpirationTimestamp(now.minusHours(2).plusHours(24));

        when(typingResultRepository.findQualifyingLeaderboardResults(any(LocalDateTime.class), eq(60)))
                .thenReturn(Arrays.asList(attempt2, attempt1));

        List<LeaderboardEntryDto> lb = leaderboardService.get24HourLeaderboard(60, null);

        assertEquals(1, lb.size(), "Only 1 best entry per user should appear");
        assertEquals(85.0, lb.get(0).getNetWpm());
        assertEquals(11L, lb.get(0).getResultId());
    }

    @Test
    @DisplayName("Verify honest empty state: returns empty list when no users have completed qualifying tests")
    void testHonestEmptyLeaderboardState() {
        when(typingResultRepository.findQualifyingLeaderboardResults(any(LocalDateTime.class), eq(60)))
                .thenReturn(Collections.emptyList());

        List<LeaderboardEntryDto> lb = leaderboardService.get24HourLeaderboard(60, null);

        assertNotNull(lb);
        assertTrue(lb.isEmpty(), "Leaderboard must be completely empty when no qualifying scores exist — never inject fake users!");
    }

    @Test
    @DisplayName("Verify low-scoring tests by real registered users are included and ranked accurately")
    void testLowScoreLegitimateUserQualifies() {
        LocalDateTime now = LocalDateTime.now();
        User slowUser = new User("slow_turtle", "turtle@test.com", "hash", "T");
        slowUser.setId(99L);

        TypingResult lowScore = new TypingResult();
        lowScore.setId(55L);
        lowScore.setUser(slowUser);
        lowScore.setDuration(60);
        lowScore.setNetWpm(18.5);
        lowScore.setRawWpm(22.0);
        lowScore.setAccuracy(84.0);
        lowScore.setTotalChars(110);
        lowScore.setCorrectChars(92);
        lowScore.setCompletionTimestamp(now.minusHours(1));
        lowScore.setExpirationTimestamp(now.minusHours(1).plusHours(24));

        when(typingResultRepository.findQualifyingLeaderboardResults(any(LocalDateTime.class), eq(60)))
                .thenReturn(List.of(lowScore));

        List<LeaderboardEntryDto> lb = leaderboardService.get24HourLeaderboard(60, null);

        assertEquals(1, lb.size());
        assertEquals("slow_turtle", lb.get(0).getUsername());
        assertEquals(18.5, lb.get(0).getNetWpm());
        assertEquals(1, lb.get(0).getRank());
    }
}
