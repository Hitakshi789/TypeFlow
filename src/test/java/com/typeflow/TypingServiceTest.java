package com.typeflow;

import com.typeflow.dto.KeyErrorDto;
import com.typeflow.dto.TypingResultResponseDto;
import com.typeflow.dto.TypingSubmissionDto;
import com.typeflow.entity.TypingResult;
import com.typeflow.entity.User;
import com.typeflow.exception.ApiException;
import com.typeflow.repository.TypingResultRepository;
import com.typeflow.service.TypingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class TypingServiceTest {

    private TypingResultRepository typingResultRepository;
    private TypingService typingService;

    @BeforeEach
    void setUp() {
        typingResultRepository = Mockito.mock(TypingResultRepository.class);
        typingService = new TypingService(typingResultRepository);
    }

    @Test
    @DisplayName("Verify WPM and Accuracy formulas against prompt specification (600 chars, 566 correct, 34 errors in 1 min)")
    void testOfficialPromptWorkedExample() {
        // Prompt specification:
        // A user types 600 characters during a one-minute test. Of those, 566 are correct and 34 are incorrect.
        // Raw WPM: 600 / 5 = 120.
        // Net WPM: 566 / 5 = 113.2.
        // Accuracy: 566 / 600 * 100 = 94.33%.

        int totalChars = 600;
        int correctChars = 566;
        double elapsedSeconds = 60.0;

        double rawWpm = typingService.calculateRawWpm(totalChars, elapsedSeconds);
        double netWpm = typingService.calculateNetWpm(correctChars, elapsedSeconds);
        double accuracy = typingService.calculateAccuracy(correctChars, totalChars);

        assertEquals(120.0, rawWpm, 0.01, "Raw WPM should be exactly 120.0");
        assertEquals(113.2, netWpm, 0.01, "Net WPM should be 113.2");
        assertEquals(94.33, accuracy, 0.01, "Accuracy should be 94.33%");
    }

    @Test
    @DisplayName("Verify 30-second and 120-second duration calculations")
    void testDifferentDurations() {
        // 30 seconds (0.5 minutes): 250 characters typed, 240 correct
        // Raw WPM: (250 / 5) / 0.5 = 100.0
        // Net WPM: (240 / 5) / 0.5 = 96.0
        double raw30 = typingService.calculateRawWpm(250, 30.0);
        double net30 = typingService.calculateNetWpm(240, 30.0);
        assertEquals(100.0, raw30, 0.01);
        assertEquals(96.0, net30, 0.01);

        // 120 seconds (2.0 minutes): 1000 characters typed, 950 correct
        // Raw WPM: (1000 / 5) / 2.0 = 100.0
        // Net WPM: (950 / 5) / 2.0 = 95.0
        double raw120 = typingService.calculateRawWpm(1000, 120.0);
        double net120 = typingService.calculateNetWpm(950, 120.0);
        assertEquals(100.0, raw120, 0.01);
        assertEquals(95.0, net120, 0.01);
    }

    @Test
    @DisplayName("Verify boundary edge cases: 0 characters, 0 elapsed time, prevent division by zero")
    void testBoundaryEdgeCases() {
        assertEquals(0.0, typingService.calculateRawWpm(0, 60.0));
        assertEquals(0.0, typingService.calculateNetWpm(0, 60.0));
        assertEquals(0.0, typingService.calculateAccuracy(0, 0));
        assertEquals(0.0, typingService.calculateRawWpm(100, 0.0));
        assertEquals(0.0, typingService.calculateNetWpm(100, 0.0));
    }

    @Test
    @DisplayName("Verify cheat / superhuman speed rejection (>300 WPM)")
    void testAntiCheatValidation() {
        TypingSubmissionDto submission = new TypingSubmissionDto();
        submission.setDuration(60);
        submission.setElapsedSeconds(60.0);
        submission.setTotalChars(2000); // 2000 / 5 = 400 WPM!
        submission.setCorrectChars(1900);
        submission.setIncorrectChars(100);

        assertThrows(ApiException.class, () -> {
            typingService.processSubmission(submission, null);
        });
    }

    @Test
    @DisplayName("Verify submission processing and key error persistence")
    void testProcessSubmissionSuccess() {
        when(typingResultRepository.save(any(TypingResult.class))).thenAnswer(invocation -> {
            TypingResult tr = invocation.getArgument(0);
            tr.setId(101L);
            return tr;
        });

        TypingSubmissionDto sub = new TypingSubmissionDto();
        sub.setDuration(60);
        sub.setElapsedSeconds(60.0);
        sub.setTotalChars(300);
        sub.setCorrectChars(290);
        sub.setIncorrectChars(10);
        sub.setBackspaces(4);
        sub.setKeyErrors(Collections.singletonList(
                new KeyErrorDto("e", "r", "CASE_A_SUBSTITUTION", 2)
        ));

        TypingResultResponseDto resp = typingService.processSubmission(sub, null);
        assertNotNull(resp);
        assertEquals(60.0, resp.getRawWpm());
        assertEquals(58.0, resp.getNetWpm());
        assertEquals(96.67, resp.getAccuracy());
        assertEquals(1, resp.getKeyErrors().size());
        assertEquals("e", resp.getKeyErrors().get(0).getExpectedKey());
    }
}
