package com.chineselingo.persistence.json;

import com.chineselingo.persistence.json.mixin.ReviewHistoryMixin;
import com.chineselingo.user.UserState;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class ReviewHistoryJacksonModule extends SimpleModule {

    public ReviewHistoryJacksonModule() {
        setMixInAnnotation(UserState.ReviewHistory.class, ReviewHistoryMixin.class);
    }
}