package com.example.kuby.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
public class BasicException extends RuntimeException {
    private final HttpStatus httpStatus;
    private final Map<String, String> errors;

    public BasicException(Map<String, String> errors, HttpStatus httpStatus) {
        super();
        this.httpStatus = httpStatus;
        this.errors = errors;
    }
}
