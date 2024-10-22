package com.example.kuby.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach((error) -> {
            String fieldName = error.getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        Map<String, Map<String, String>> response = new HashMap<>();
        response.put("errors", errors);
        return ResponseEntity.badRequest().body(response);
    }
    @ExceptionHandler(BasicException.class)
    public ResponseEntity<Map<String, Map<String, String>>> handleValidationExceptions(BasicException ex){
        Map<String, String> errors = ex.getErrors();
        Map<String, Map<String, String>> response = new HashMap<>();
        response.put("errors", errors);
        return new ResponseEntity<>(response,ex.getHttpStatus());
    }
}
