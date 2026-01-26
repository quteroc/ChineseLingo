package com.chineselingo.user.dto;

import com.chineselingo.persistence.PersistedDto;
import com.chineselingo.user.UserState;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class UserStateDto extends PersistedDto {
    @JsonProperty ("knownIds")
    private List<Integer> knownChars;
    @JsonProperty("history")
    private Map<Integer, UserState.ReviewHistory> reviewHistory;

    public List<Integer> getKnownChars() {
        return knownChars;
    }

    public void setKnownChars(List<Integer> knownChars) {
        this.knownChars = knownChars;
    }

    public Map<Integer, UserState.ReviewHistory> getReviewHistory() {
        return reviewHistory;
    }

    public void setReviewHistory(Map<Integer, UserState.ReviewHistory> reviewHistory) {
        this.reviewHistory = reviewHistory;
    }
}


