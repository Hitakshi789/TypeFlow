package com.typeflow.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

public class TypingSubmissionDto {

    @NotNull(message = "Duration is required")
    @Min(value = 10, message = "Duration must be at least 10 seconds")
    @Max(value = 300, message = "Duration must not exceed 300 seconds")
    private Integer duration;

    @NotNull(message = "Elapsed seconds is required")
    private Double elapsedSeconds;

    @NotNull(message = "Total characters is required")
    @Min(0)
    private Integer totalChars;

    @NotNull(message = "Correct characters is required")
    @Min(0)
    private Integer correctChars;

    @NotNull(message = "Incorrect characters is required")
    @Min(0)
    private Integer incorrectChars;

    private Integer backspaces = 0;

    private Double clientRawWpm;
    private Double clientNetWpm;
    private Double clientAccuracy;

    private String speedHistoryJson;
    private List<KeyErrorDto> keyErrors = new ArrayList<>();

    public TypingSubmissionDto() {}

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Double getElapsedSeconds() {
        return elapsedSeconds;
    }

    public void setElapsedSeconds(Double elapsedSeconds) {
        this.elapsedSeconds = elapsedSeconds;
    }

    public Integer getTotalChars() {
        return totalChars;
    }

    public void setTotalChars(Integer totalChars) {
        this.totalChars = totalChars;
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

    public Integer getBackspaces() {
        return backspaces;
    }

    public void setBackspaces(Integer backspaces) {
        this.backspaces = backspaces;
    }

    public Double getClientRawWpm() {
        return clientRawWpm;
    }

    public void setClientRawWpm(Double clientRawWpm) {
        this.clientRawWpm = clientRawWpm;
    }

    public Double getClientNetWpm() {
        return clientNetWpm;
    }

    public void setClientNetWpm(Double clientNetWpm) {
        this.clientNetWpm = clientNetWpm;
    }

    public Double getClientAccuracy() {
        return clientAccuracy;
    }

    public void setClientAccuracy(Double clientAccuracy) {
        this.clientAccuracy = clientAccuracy;
    }

    public String getSpeedHistoryJson() {
        return speedHistoryJson;
    }

    public void setSpeedHistoryJson(String speedHistoryJson) {
        this.speedHistoryJson = speedHistoryJson;
    }

    public List<KeyErrorDto> getKeyErrors() {
        return keyErrors;
    }

    public void setKeyErrors(List<KeyErrorDto> keyErrors) {
        this.keyErrors = keyErrors;
    }
}
