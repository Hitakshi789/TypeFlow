package com.typeflow.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PersonalStatsDto {
    private Double personalBestNetWpm;
    private Double averageNetWpm;
    private Double bestAccuracy;
    private long totalTestsCompleted;

    private Map<String, DurationStat> durationStats = new HashMap<>();
    private List<TypingResultResponseDto> recentResults = new ArrayList<>();
    private List<ProgressPoint> progressHistory = new ArrayList<>();
    private List<KeyStat> topMistypedKeys = new ArrayList<>();
    private List<SubstitutionStat> topSubstitutions = new ArrayList<>();
    private Map<String, KeyDetailStat> keyAccuracyMap = new HashMap<>();

    public PersonalStatsDto() {}

    public static class DurationStat {
        private Double personalBestNetWpm;
        private Double averageNetWpm;
        private Double bestAccuracy;
        private long testCount;

        public DurationStat() {}

        public DurationStat(Double personalBestNetWpm, Double averageNetWpm, Double bestAccuracy, long testCount) {
            this.personalBestNetWpm = personalBestNetWpm;
            this.averageNetWpm = averageNetWpm;
            this.bestAccuracy = bestAccuracy;
            this.testCount = testCount;
        }

        public Double getPersonalBestNetWpm() { return personalBestNetWpm; }
        public void setPersonalBestNetWpm(Double personalBestNetWpm) { this.personalBestNetWpm = personalBestNetWpm; }
        public Double getAverageNetWpm() { return averageNetWpm; }
        public void setAverageNetWpm(Double averageNetWpm) { this.averageNetWpm = averageNetWpm; }
        public Double getBestAccuracy() { return bestAccuracy; }
        public void setBestAccuracy(Double bestAccuracy) { this.bestAccuracy = bestAccuracy; }
        public long getTestCount() { return testCount; }
        public void setTestCount(long testCount) { this.testCount = testCount; }
    }

    public static class ProgressPoint {
        private String date;
        private Double netWpm;
        private Double accuracy;
        private Integer duration;

        public ProgressPoint() {}

        public ProgressPoint(String date, Double netWpm, Double accuracy, Integer duration) {
            this.date = date;
            this.netWpm = netWpm;
            this.accuracy = accuracy;
            this.duration = duration;
        }

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public Double getNetWpm() { return netWpm; }
        public void setNetWpm(Double netWpm) { this.netWpm = netWpm; }
        public Double getAccuracy() { return accuracy; }
        public void setAccuracy(Double accuracy) { this.accuracy = accuracy; }
        public Integer getDuration() { return duration; }
        public void setDuration(Integer duration) { this.duration = duration; }
    }

    public static class KeyStat {
        private String key;
        private int errorCount;

        public KeyStat() {}
        public KeyStat(String key, int errorCount) {
            this.key = key;
            this.errorCount = errorCount;
        }

        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }
        public int getErrorCount() { return errorCount; }
        public void setErrorCount(int errorCount) { this.errorCount = errorCount; }
    }

    public static class SubstitutionStat {
        private String expected;
        private String actual;
        private int count;

        public SubstitutionStat() {}
        public SubstitutionStat(String expected, String actual, int count) {
            this.expected = expected;
            this.actual = actual;
            this.count = count;
        }

        public String getExpected() { return expected; }
        public void setExpected(String expected) { this.expected = expected; }
        public String getActual() { return actual; }
        public void setActual(String actual) { this.actual = actual; }
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
    }

    public static class KeyDetailStat {
        private int expectedCount;
        private int correctCount;
        private int errorCount;
        private double accuracyPercent;
        private String mostCommonWrongKey;

        public KeyDetailStat() {}
        public KeyDetailStat(int expectedCount, int correctCount, int errorCount, double accuracyPercent, String mostCommonWrongKey) {
            this.expectedCount = expectedCount;
            this.correctCount = correctCount;
            this.errorCount = errorCount;
            this.accuracyPercent = accuracyPercent;
            this.mostCommonWrongKey = mostCommonWrongKey;
        }

        public int getExpectedCount() { return expectedCount; }
        public void setExpectedCount(int expectedCount) { this.expectedCount = expectedCount; }
        public int getCorrectCount() { return correctCount; }
        public void setCorrectCount(int correctCount) { this.correctCount = correctCount; }
        public int getErrorCount() { return errorCount; }
        public void setErrorCount(int errorCount) { this.errorCount = errorCount; }
        public double getAccuracyPercent() { return accuracyPercent; }
        public void setAccuracyPercent(double accuracyPercent) { this.accuracyPercent = accuracyPercent; }
        public String getMostCommonWrongKey() { return mostCommonWrongKey; }
        public void setMostCommonWrongKey(String mostCommonWrongKey) { this.mostCommonWrongKey = mostCommonWrongKey; }
    }

    public Double getPersonalBestNetWpm() { return personalBestNetWpm; }
    public void setPersonalBestNetWpm(Double personalBestNetWpm) { this.personalBestNetWpm = personalBestNetWpm; }
    public Double getAverageNetWpm() { return averageNetWpm; }
    public void setAverageNetWpm(Double averageNetWpm) { this.averageNetWpm = averageNetWpm; }
    public Double getBestAccuracy() { return bestAccuracy; }
    public void setBestAccuracy(Double bestAccuracy) { this.bestAccuracy = bestAccuracy; }
    public long getTotalTestsCompleted() { return totalTestsCompleted; }
    public void setTotalTestsCompleted(long totalTestsCompleted) { this.totalTestsCompleted = totalTestsCompleted; }
    public Map<String, DurationStat> getDurationStats() { return durationStats; }
    public void setDurationStats(Map<String, DurationStat> durationStats) { this.durationStats = durationStats; }
    public List<TypingResultResponseDto> getRecentResults() { return recentResults; }
    public void setRecentResults(List<TypingResultResponseDto> recentResults) { this.recentResults = recentResults; }
    public List<ProgressPoint> getProgressHistory() { return progressHistory; }
    public void setProgressHistory(List<ProgressPoint> progressHistory) { this.progressHistory = progressHistory; }
    public List<KeyStat> getTopMistypedKeys() { return topMistypedKeys; }
    public void setTopMistypedKeys(List<KeyStat> topMistypedKeys) { this.topMistypedKeys = topMistypedKeys; }
    public List<SubstitutionStat> getTopSubstitutions() { return topSubstitutions; }
    public void setTopSubstitutions(List<SubstitutionStat> topSubstitutions) { this.topSubstitutions = topSubstitutions; }
    public Map<String, KeyDetailStat> getKeyAccuracyMap() { return keyAccuracyMap; }
    public void setKeyAccuracyMap(Map<String, KeyDetailStat> keyAccuracyMap) { this.keyAccuracyMap = keyAccuracyMap; }
}
