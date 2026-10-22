package com.typeflow.dto;

import java.time.LocalDateTime;

public class UserProfileDto {
    private Long id;
    private String username;
    private String email;
    private String avatar;
    private LocalDateTime createdAt;
    private long totalTestsCompleted;
    private Double personalBestNetWpm;
    private Double averageNetWpm;
    private Double bestAccuracy;
    private Integer leaderboardRank;

    public UserProfileDto() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public long getTotalTestsCompleted() {
        return totalTestsCompleted;
    }

    public void setTotalTestsCompleted(long totalTestsCompleted) {
        this.totalTestsCompleted = totalTestsCompleted;
    }

    public Double getPersonalBestNetWpm() {
        return personalBestNetWpm;
    }

    public void setPersonalBestNetWpm(Double personalBestNetWpm) {
        this.personalBestNetWpm = personalBestNetWpm;
    }

    public Double getAverageNetWpm() {
        return averageNetWpm;
    }

    public void setAverageNetWpm(Double averageNetWpm) {
        this.averageNetWpm = averageNetWpm;
    }

    public Double getBestAccuracy() {
        return bestAccuracy;
    }

    public void setBestAccuracy(Double bestAccuracy) {
        this.bestAccuracy = bestAccuracy;
    }

    public Integer getLeaderboardRank() {
        return leaderboardRank;
    }

    public void setLeaderboardRank(Integer leaderboardRank) {
        this.leaderboardRank = leaderboardRank;
    }
}
