package com.typeflow.service;

import com.typeflow.dto.PersonalStatsDto;
import com.typeflow.dto.TypingResultResponseDto;
import com.typeflow.entity.KeyError;
import com.typeflow.entity.TypingResult;
import com.typeflow.entity.User;
import com.typeflow.exception.ApiException;
import com.typeflow.repository.KeyErrorRepository;
import com.typeflow.repository.TypingResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatsService {

    private final TypingResultRepository typingResultRepository;
    private final KeyErrorRepository keyErrorRepository;
    private final TypingService typingService;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM dd, HH:mm");

    @Autowired
    public StatsService(TypingResultRepository typingResultRepository,
                        KeyErrorRepository keyErrorRepository,
                        TypingService typingService) {
        this.typingResultRepository = typingResultRepository;
        this.keyErrorRepository = keyErrorRepository;
        this.typingService = typingService;
    }

    public PersonalStatsDto getPersonalStats(User user) {
        PersonalStatsDto stats = new PersonalStatsDto();
        List<TypingResult> results = typingResultRepository.findByUserIdOrderByCompletionTimestampDesc(user.getId());
        stats.setTotalTestsCompleted(results.size());

        if (results.isEmpty()) {
            stats.setPersonalBestNetWpm(0.0);
            stats.setAverageNetWpm(0.0);
            stats.setBestAccuracy(0.0);
            return stats;
        }

        // Summary metrics
        double pb = results.stream().mapToDouble(TypingResult::getNetWpm).max().orElse(0.0);
        double avg = results.stream().mapToDouble(TypingResult::getNetWpm).average().orElse(0.0);
        double bestAcc = results.stream().mapToDouble(TypingResult::getAccuracy).max().orElse(0.0);

        stats.setPersonalBestNetWpm(Math.round(pb * 10.0) / 10.0);
        stats.setAverageNetWpm(Math.round(avg * 10.0) / 10.0);
        stats.setBestAccuracy(Math.round(bestAcc * 100.0) / 100.0);

        // Duration breakdowns (30s, 60s, 120s)
        int[] durations = {30, 60, 120};
        for (int d : durations) {
            List<TypingResult> filtered = results.stream()
                    .filter(r -> r.getDuration() == d)
                    .collect(Collectors.toList());
            if (!filtered.isEmpty()) {
                double dPb = filtered.stream().mapToDouble(TypingResult::getNetWpm).max().orElse(0.0);
                double dAvg = filtered.stream().mapToDouble(TypingResult::getNetWpm).average().orElse(0.0);
                double dAcc = filtered.stream().mapToDouble(TypingResult::getAccuracy).max().orElse(0.0);
                stats.getDurationStats().put(String.valueOf(d), new PersonalStatsDto.DurationStat(
                        Math.round(dPb * 10.0) / 10.0,
                        Math.round(dAvg * 10.0) / 10.0,
                        Math.round(dAcc * 100.0) / 100.0,
                        filtered.size()
                ));
            } else {
                stats.getDurationStats().put(String.valueOf(d), new PersonalStatsDto.DurationStat(0.0, 0.0, 0.0, 0));
            }
        }

        // Recent results (latest 20)
        List<TypingResultResponseDto> recentDtos = results.stream()
                .limit(20)
                .map(r -> typingService.mapToDto(r, r.getNetWpm() >= pb))
                .collect(Collectors.toList());
        stats.setRecentResults(recentDtos);

        // Progress History in chronological order (for Chart.js)
        List<TypingResult> chronological = new ArrayList<>(results);
        Collections.reverse(chronological);
        List<PersonalStatsDto.ProgressPoint> progressPoints = chronological.stream()
                .map(r -> new PersonalStatsDto.ProgressPoint(
                        r.getCompletionTimestamp().format(DATE_FMT),
                        r.getNetWpm(),
                        r.getAccuracy(),
                        r.getDuration()
                ))
                .collect(Collectors.toList());
        stats.setProgressHistory(progressPoints);

        // Key Error Analytics across all user's tests
        List<KeyError> allErrors = keyErrorRepository.findByUserId(user.getId());

        // Mistyped keys frequency
        Map<String, Integer> keyErrorCounts = new HashMap<>();
        Map<String, Map<String, Integer>> substitutions = new HashMap<>();

        for (KeyError ke : allErrors) {
            String exp = ke.getExpectedKey().toLowerCase();
            keyErrorCounts.put(exp, keyErrorCounts.getOrDefault(exp, 0) + ke.getErrorCount());

            String act = ke.getActualKey().toLowerCase();
            substitutions.computeIfAbsent(exp, k -> new HashMap<>());
            Map<String, Integer> subMap = substitutions.get(exp);
            subMap.put(act, subMap.getOrDefault(act, 0) + ke.getErrorCount());
        }

        // Top mistyped keys
        List<PersonalStatsDto.KeyStat> topMistyped = keyErrorCounts.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(10)
                .map(e -> new PersonalStatsDto.KeyStat(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
        stats.setTopMistypedKeys(topMistyped);

        // Top substitutions
        List<PersonalStatsDto.SubstitutionStat> topSubs = new ArrayList<>();
        for (Map.Entry<String, Map<String, Integer>> entry : substitutions.entrySet()) {
            for (Map.Entry<String, Integer> subEntry : entry.getValue().entrySet()) {
                topSubs.add(new PersonalStatsDto.SubstitutionStat(
                        entry.getKey(),
                        subEntry.getKey(),
                        subEntry.getValue()
                ));
            }
        }
        topSubs.sort((a, b) -> Integer.compare(b.getCount(), a.getCount()));
        stats.setTopSubstitutions(topSubs.stream().limit(10).collect(Collectors.toList()));

        // Cumulative Key Detail accuracy map
        for (Map.Entry<String, Integer> entry : keyErrorCounts.entrySet()) {
            String k = entry.getKey();
            int errors = entry.getValue();
            // Find most common substitution
            Map<String, Integer> subMap = substitutions.get(k);
            String mostCommon = subMap != null ? subMap.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("") : "";

            // Calculate estimated error rate based on user tests
            PersonalStatsDto.KeyDetailStat kd = new PersonalStatsDto.KeyDetailStat();
            kd.setErrorCount(errors);
            kd.setMostCommonWrongKey(mostCommon);
            stats.getKeyAccuracyMap().put(k, kd);
        }

        return stats;
    }

    @Transactional
    public void deleteResult(Long resultId, User user) {
        TypingResult result = typingResultRepository.findById(resultId)
                .orElseThrow(() -> new ApiException("Result not found"));
        if (result.getUser() == null || !result.getUser().getId().equals(user.getId())) {
            throw new ApiException("You do not have permission to delete this result");
        }
        typingResultRepository.delete(result);
    }

    @Transactional
    public void clearAllResults(User user) {
        typingResultRepository.deleteByUserId(user.getId());
    }
}
