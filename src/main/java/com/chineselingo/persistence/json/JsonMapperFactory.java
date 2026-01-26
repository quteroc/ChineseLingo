package com.chineselingo.persistence.json;

import com.chineselingo.persistence.json.mixin.ReviewHistoryMixin;
import com.chineselingo.user.UserState;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public final class JsonMapperFactory {

    private static final ObjectMapper MAPPER = create();

    private JsonMapperFactory() {}

    private static ObjectMapper create() {
        ObjectMapper mapper = new ObjectMapper();

        // pretty print
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        mapper.findAndRegisterModules();

        return mapper;
    }

    public static ObjectMapper get() {
        return MAPPER;
    }
}