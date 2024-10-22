package com.example.kuby.security.util.parsers;

import com.example.kuby.exceptions.BasicException;
import com.example.kuby.security.models.enums.Provider;
import org.springframework.http.HttpStatus;

import java.util.Map;

public final class ProviderEnumParser {
    private ProviderEnumParser() {
    }

    public static Provider getProviderFromString(String stringedProvider) {
        switch (stringedProvider.toUpperCase()) {
            case "LOCAL" -> {
                return Provider.LOCAL;
            }
            case "GOOGLE" -> {
                return Provider.GOOGLE;
            }
            default -> throw new BasicException(Map.of("provider", "Incorrect provider type"), HttpStatus.BAD_REQUEST);
        }
    }
}
