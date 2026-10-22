package com.typeflow.dto;

public class KeyErrorDto {
    private String expectedKey;
    private String actualKey;
    private String errorType; // CASE_A_SUBSTITUTION, CASE_B_OMISSION, CASE_B_INSERTION, CORRECTION
    private Integer errorCount;

    public KeyErrorDto() {}

    public KeyErrorDto(String expectedKey, String actualKey, String errorType, Integer errorCount) {
        this.expectedKey = expectedKey;
        this.actualKey = actualKey;
        this.errorType = errorType;
        this.errorCount = errorCount != null ? errorCount : 1;
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
