package com.typeflow.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "typing_results", indexes = {
    @Index(name = "idx_tr_user_id", columnList = "user_id"),
    @Index(name = "idx_tr_completion_timestamp", columnList = "completion_timestamp"),
    @Index(name = "idx_tr_duration_netwpm", columnList = "duration, net_wpm")
})
public class TypingResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private Integer duration; // 30, 60, 120 seconds

    @Column(name = "raw_wpm", nullable = false)
    private Double rawWpm;

    @Column(name = "net_wpm", nullable = false)
    private Double netWpm;

    @Column(nullable = false)
    private Double accuracy;

    @Column(name = "correct_chars", nullable = false)
    private Integer correctChars;

    @Column(name = "incorrect_chars", nullable = false)
    private Integer incorrectChars;

    @Column(name = "total_chars", nullable = false)
    private Integer totalChars;

    @Column(nullable = false)
    private Integer backspaces = 0;

    @Column(name = "speed_history_json", columnDefinition = "TEXT")
    private String speedHistoryJson;

    @Column(name = "completion_timestamp", nullable = false)
    private LocalDateTime completionTimestamp;

    @Column(name = "expiration_timestamp", nullable = false)
    private LocalDateTime expirationTimestamp;

    @OneToMany(mappedBy = "typingResult", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<KeyError> keyErrors = new ArrayList<>();

    public TypingResult() {
        this.completionTimestamp = LocalDateTime.now();
        this.expirationTimestamp = this.completionTimestamp.plusHours(24);
    }

    public void addKeyError(KeyError error) {
        keyErrors.add(error);
        error.setTypingResult(this);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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
        if (completionTimestamp != null) {
            this.expirationTimestamp = completionTimestamp.plusHours(24);
        }
    }

    public LocalDateTime getExpirationTimestamp() {
        return expirationTimestamp;
    }

    public void setExpirationTimestamp(LocalDateTime expirationTimestamp) {
        this.expirationTimestamp = expirationTimestamp;
    }

    public List<KeyError> getKeyErrors() {
        return keyErrors;
    }

    public void setKeyErrors(List<KeyError> keyErrors) {
        this.keyErrors = keyErrors;
    }
}
