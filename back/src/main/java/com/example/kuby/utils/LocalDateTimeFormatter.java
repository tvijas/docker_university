package com.example.kuby.utils;

import com.example.kuby.exceptions.BasicException;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public final class LocalDateTimeFormatter {
    private LocalDateTimeFormatter (){}

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String convertToString(LocalDateTime localDateTime){
        try{
            return localDateTime.format(formatter);
        }catch (Exception ex){
            ex.printStackTrace();
            throw new BasicException(Map.of("dateTime","Can not convert date & time object to string"), HttpStatus.BAD_REQUEST);
        }
    }
}
