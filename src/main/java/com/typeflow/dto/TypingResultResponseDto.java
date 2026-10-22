package com.typeflow.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TypingResultResponseDto {
    private Long id;
    private Long userId;
    private String username;
    private String userAvatar;
    private Integer duration;
    private Double rawWpm;
    private Double netWpm;
    private Double accuracy;
    private Integer correctChars;
    private Integer incorrectChars;
    private Integer totalChars;
    private Integer backspaces;
    private String speedHistoryJson;
    private LocalDateTime completionTimestamp;
    private LocalDateTime expirationTimestamp;
    private boolean isPersonalBest;
    private Integer rankIn24h;
    private List<KeyErrorDto> keyErrors = new ArrayList<>();

    public TypingResultResponseDto() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Double getRawWpm() {
        return rawWpm;
    }

    public void setRawWpm(Double rawWpm) {
        this.rawWpm = rawWpm;
    }

    public Double getNetWpm() {
        return netWpm;
    }

    public void setNetWpm(Double netWpm) {
        this.netWpm = netWpm;
    }

    public Double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Double accuracy) {
        this.accuracy = accuracy;
    }

    public Integer getCorrectChars() {
        return correctChars;
    }

    public void setCorrectChars(Integer correctChars) {
        this.correctChars = correctChars;
    }

    public Integer getIncorrectChars() {
        return incorrectChars;
    }

    public void setIncorrectChars(Integer incorrectChars) {
        this.incorrectChars = incorrectChars;
    }

    public Integer getTotalChars() {
        return totalChars;
    }

    public void setTotalChars(Integer totalChars) {
        this.totalChars = totalChars;
    }

    public Integer getBackspaces() {
        return backspaces;
    }

    public void setBackspaces(Integer backspaces) {
        this.backspaces = backspaces;
    }

    public String getSpeedHistoryJson() {
        return speedHistoryJson;
    }

    public void setSpeedHistoryJson(String speedHistoryJson) {
        this.speedHistoryJson = speedHistoryJson;
    }

    public LocalDateTime getCompletionTimestamp() {
        return completionTimestamp;
    }

    public void setCompletionTimestamp(LocalDateTime completionTimestamp) {
        this.completionTimestamp = completionTimestamp;
    }

    public LocalDateTime getExpirationTimestamp() {
        return expirationTimestamp;
    }

    public void setExpirationTimestamp(LocalDateTime expirationTimestamp) {
        this.expirationTimestamp = expirationTimestamp;
    }

    public boolean isPersonalBest() {
        return isPersonalBest;
    }

    public void setPersonalBest(boolean personalBest) {
        isPersonalBest = personalBest;
    }

    public Integer getRankIn24h() {
        return rankIn24h;
    }

    public void setRankIn24h(Integer rankIn24h) {
        this.rankIn24h = rankIn24h;
    }

    public List<KeyErrorDto> getKeyErrors() {
        return keyErrors;
    }

    public void setKeyErrors(List<KeyErrorDto> keyErrors) {
        this.keyErrors = keyErrors;
    }
}
