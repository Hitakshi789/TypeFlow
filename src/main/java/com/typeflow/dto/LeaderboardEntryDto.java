package com.typeflow.dto;

import java.time.LocalDateTime;

public class LeaderboardEntryDto {
    private Integer rank;
    private Long resultId;
    private Long userId;
    private String username;
    private String avatar;
    private Double netWpm;
    private Double rawWpm;
    private Double accuracy;
    private Integer duration;
    private LocalDateTime completionTimestamp;
    private String timeRemainingFormatted; // e.g., "14h 22m"
    private boolean isCurrentUser;

    public LeaderboardEntryDto() {}

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public Long getResultId() {
        return resultId;
    }

    public void setResultId(Long resultId) {
        this.resultId = resultId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Double getNetWpm() {
        return netWpm;
    }

    public void setNetWpm(Double netWpm) {
        this.netWpm = netWpm;
    }

    public Double getRawWpm() {
        return rawWpm;
    }

    public void setRawWpm(Double rawWpm) {
        this.rawWpm = rawWpm;
    }

    public Double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Double accuracy) {
        this.accuracy = accuracy;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public LocalDateTime getCompletionTimestamp() {
        return completionTimestamp;
    }

    public void setCompletionTimestamp(LocalDateTime completionTimestamp) {
        this.completionTimestamp = completionTimestamp;
    }

    public String getTimeRemainingFormatted() {
        return timeRemainingFormatted;
    }

    public void setTimeRemainingFormatted(String timeRemainingFormatted) {
        this.timeRemainingFormatted = timeRemainingFormatted;
    }

    public boolean isCurrentUser() {
        return isCurrentUser;
    }

    public void setCurrentUser(boolean currentUser) {
        isCurrentUser = currentUser;
    }
}
