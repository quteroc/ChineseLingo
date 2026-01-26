package com.chineselingo.persistence.json.mixin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class ReviewHistoryMixin {

    @JsonCreator
    public ReviewHistoryMixin(
            @JsonProperty("views") int views,
            @JsonProperty("successes") int successes,
            @JsonProperty("last") long lastReviewedEpochSeconds
    ) {}
    @JsonProperty("last")
    abstract long getLastReviewedEpochSeconds();
}
