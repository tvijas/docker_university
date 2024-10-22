package com.example.kuby.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;

public final class JsonPrettyPrinter {
    private JsonPrettyPrinter() {
    }

    private final static ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public static void print(String json) throws IOException {
        Object jsonObject = objectMapper.readValue(json, Object.class);
        System.out.println(objectMapper.writeValueAsString(jsonObject));
    }
}
