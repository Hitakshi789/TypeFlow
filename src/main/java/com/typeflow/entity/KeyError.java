package com.typeflow.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "key_errors", indexes = {
    @Index(name = "idx_ke_result_id", columnList = "typing_result_id"),
    @Index(name = "idx_ke_expected_key", columnList = "expected_key"),
    @Index(name = "idx_ke_actual_key", columnList = "actual_key")
})
public class KeyError {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "typing_result_id", nullable = false)
    private TypingResult typingResult;

    @Column(name = "expected_key", nullable = false, length = 20)
    private String expectedKey;

    @Column(name = "actual_key", nullable = false, length = 20)
    private String actualKey;

    @Column(name = "error_type", nullable = false, length = 40)
    private String errorType; // CASE_A_SUBSTITUTION, CASE_B_OMISSION, CASE_B_INSERTION, CORRECTION

    @Column(name = "error_count", nullable = false)
    private Integer errorCount = 1;

    public KeyError() {}

    public KeyError(String expectedKey, String actualKey, String errorType, Integer errorCount) {
        this.expectedKey = expectedKey;
        this.actualKey = actualKey;
        this.errorType = errorType;
        this.errorCount = errorCount != null ? errorCount : 1;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TypingResult getTypingResult() {
        return typingResult;
    }

    public void setTypingResult(TypingResult typingResult) {
        this.typingResult = typingResult;
    }

    public String getExpectedKey() {
        return expectedKey;
    }

    public void setExpectedKey(String expectedKey) {
        this.expectedKey = expectedKey;
    }

    public String getActualKey() {
        return actualKey;
    }

    public void setActualKey(String actualKey) {
        this.actualKey = actualKey;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public Integer getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(Integer errorCount) {
        this.errorCount = errorCount;
    }
}
