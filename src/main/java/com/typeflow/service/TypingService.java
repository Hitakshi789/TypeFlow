package com.typeflow.service;

import com.typeflow.dto.KeyErrorDto;
import com.typeflow.dto.TypingResultResponseDto;
import com.typeflow.dto.TypingSubmissionDto;
import com.typeflow.entity.KeyError;
import com.typeflow.entity.TypingResult;
import com.typeflow.entity.User;
import com.typeflow.exception.ApiException;
import com.typeflow.repository.TypingResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TypingService {

    private final TypingResultRepository typingResultRepository;

    @Autowired
    public TypingService(TypingResultRepository typingResultRepository) {
        this.typingResultRepository = typingResultRepository;
    }

    /**
     * Calculates Raw WPM using the standard 5-characters per word convention.
     * Raw WPM = (Total characters typed / 5.0) / Elapsed time in minutes.
     */
    public double calculateRawWpm(int totalChars, double elapsedSeconds) {
        if (elapsedSeconds <= 0 || totalChars <= 0) {
            return 0.0;
        }
        double elapsedMinutes = elapsedSeconds / 60.0;
        double raw = (totalChars / 5.0) / elapsedMinutes;
        return round(raw, 1);
    }

    /**
     * Calculates Net WPM using the standard 5-characters per word convention.
     * Net WPM = (Correct characters / 5.0) / Elapsed time in minutes.
     */
    public double calculateNetWpm(int correctChars, double elapsedSeconds) {
        if (elapsedSeconds <= 0 || correctChars <= 0) {
            return 0.0;
        }
        double elapsedMinutes = elapsedSeconds / 60.0;
        double net = (correctChars / 5.0) / elapsedMinutes;
        return round(Math.max(0.0, net), 1);
    }

    /**
     * Calculates Character Accuracy Percentage.
     * Accuracy = (Correct characters / Total characters typed) * 100.
     */
    public double calculateAccuracy(int correctChars, int totalChars) {
        if (totalChars <= 0) {
            return 0.0;
        }
        double acc = ((double) correctChars / totalChars) * 100.0;
        return round(Math.min(100.0, Math.max(0.0, acc)), 2);
    }

    private double round(double value, int places) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return 0.0;
        }
        return BigDecimal.valueOf(value)
                .setScale(places, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * Validates and processes a typing test submission on the server side.
     * Server calculates and verifies all metrics to prevent client tampering.
     */
    @Transactional
    public TypingResultResponseDto processSubmission(TypingSubmissionDto submission, User user) {
        if (submission.getDuration() == null || submission.getDuration() <= 0) {
            throw new ApiException("Invalid test duration specified");
        }

        double elapsedSeconds = submission.getElapsedSeconds() != null && submission.getElapsedSeconds() > 0
                ? submission.getElapsedSeconds()
                : submission.getDuration();

        int correctChars = submission.getCorrectChars() != null ? submission.getCorrectChars() : 0;
        int incorrectChars = submission.getIncorrectChars() != null ? submission.getIncorrectChars() : 0;
        int totalChars = submission.getTotalChars() != null ? submission.getTotalChars() : 0;

        // Mathematical consistency verification
        if (totalChars != correctChars + incorrectChars) {
            // Adjust total to sum of correct and incorrect if slightly off due to event timing
            totalChars = correctChars + incorrectChars;
        }

        if (totalChars < 0 || correctChars < 0 || incorrectChars < 0) {
            throw new ApiException("Negative character counts are not allowed");
        }

        // Independent server-side calculation
        double rawWpm = calculateRawWpm(totalChars, elapsedSeconds);
        double netWpm = calculateNetWpm(correctChars, elapsedSeconds);
        double accuracy = calculateAccuracy(correctChars, totalChars);

        // Sanity rate limit / cheat check: world record is ~250 WPM
        if (rawWpm > 300.0) {
            throw new ApiException("Submission exceeded human typing threshold (300 WPM). Result flagged as invalid.");
        }

        TypingResult result = new TypingResult();
        result.setUser(user);
        result.setDuration(submission.getDuration());
        result.setRawWpm(rawWpm);
        result.setNetWpm(netWpm);
        result.setAccuracy(accuracy);
        result.setCorrectChars(correctChars);
        result.setIncorrectChars(incorrectChars);
        result.setTotalChars(totalChars);
        result.setBackspaces(submission.getBackspaces() != null ? submission.getBackspaces() : 0);
        result.setSpeedHistoryJson(submission.getSpeedHistoryJson());
        result.setCompletionTimestamp(LocalDateTime.now());
        result.setExpirationTimestamp(result.getCompletionTimestamp().plusHours(24));

        // Attach key error events for heatmap reconstruction
        if (submission.getKeyErrors() != null) {
            for (KeyErrorDto errDto : submission.getKeyErrors()) {
                if (errDto.getExpectedKey() != null && !errDto.getExpectedKey().isEmpty()) {
                    KeyError ke = new KeyError(
                            errDto.getExpectedKey(),
                            errDto.getActualKey() != null ? errDto.getActualKey() : "[UNKNOWN]",
                            errDto.getErrorType() != null ? errDto.getErrorType() : "CASE_A_SUBSTITUTION",
                            errDto.getErrorCount() != null ? errDto.getErrorCount() : 1
                    );
                    result.addKeyError(ke);
                }
            }
        }

        TypingResult saved = typingResultRepository.save(result);

        // Check if personal best for logged-in user
        boolean isPb = false;
        if (user != null) {
            List<TypingResult> userHistory = typingResultRepository.findByUserIdOrderByCompletionTimestampDesc(user.getId());
            double maxPreviousNetWpm = userHistory.stream()
                    .filter(r -> !r.getId().equals(saved.getId()))
                    .mapToDouble(TypingResult::getNetWpm)
                    .max()
                    .orElse(0.0);
            isPb = saved.getNetWpm() > maxPreviousNetWpm;
        }

        return mapToDto(saved, isPb);
    }

    public TypingResultResponseDto getResultById(Long id) {
        TypingResult result = typingResultRepository.findById(id)
                .orElseThrow(() -> new ApiException("Typing result not found with id " + id));
        return mapToDto(result, false);
    }

    public TypingResultResponseDto mapToDto(TypingResult result, boolean isPersonalBest) {
        TypingResultResponseDto dto = new TypingResultResponseDto();
        dto.setId(result.getId());
        if (result.getUser() != null) {
            dto.setUserId(result.getUser().getId());
            dto.setUsername(result.getUser().getUsername());
            dto.setUserAvatar(result.getUser().getAvatar());
        } else {
            dto.setUsername("Guest Typist");
            dto.setUserAvatar("G");
        }
        dto.setDuration(result.getDuration());
        dto.setRawWpm(result.getRawWpm());
        dto.setNetWpm(result.getNetWpm());
        dto.setAccuracy(result.getAccuracy());
        dto.setCorrectChars(result.getCorrectChars());
        dto.setIncorrectChars(result.getIncorrectChars());
        dto.setTotalChars(result.getTotalChars());
        dto.setBackspaces(result.getBackspaces());
        dto.setSpeedHistoryJson(result.getSpeedHistoryJson());
        dto.setCompletionTimestamp(result.getCompletionTimestamp());
        dto.setExpirationTimestamp(result.getExpirationTimestamp());
        dto.setPersonalBest(isPersonalBest);

        List<KeyErrorDto> keyErrors = new ArrayList<>();
        if (result.getKeyErrors() != null) {
            for (KeyError ke : result.getKeyErrors()) {
                keyErrors.add(new KeyErrorDto(
                        ke.getExpectedKey(),
                        ke.getActualKey(),
                        ke.getErrorType(),
                        ke.getErrorCount()
                ));
            }
        }
        dto.setKeyErrors(keyErrors);
        return dto;
    }
}
