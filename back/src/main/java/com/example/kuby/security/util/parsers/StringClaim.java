package com.example.kuby.security.util.parsers;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.Claim;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class StringClaim implements Claim {
    private final String value;
    private final boolean isMissing;

    public StringClaim(String value) {
        this.value = value;
        this.isMissing = (value == null);
    }

    @Override
    public boolean isNull() {
        return value == null;
    }

    @Override
    public boolean isMissing() {
        return isMissing;
    }

    @Override
    public Boolean asBoolean() {
        return null;
    }

    @Override
    public Integer asInt() {
        return null;
    }

    @Override
    public Long asLong() {
        return null;
    }

    @Override
    public Double asDouble() {
        return null;
    }

    @Override
    public String asString() {
        return value;
    }

    @Override
    public Date asDate() {
        return null;
    }

    @Override
    public <T> T[] asArray(Class<T> aClass) throws JWTDecodeException {
        return null;
    }

    @Override
    public <T> List<T> asList(Class<T> aClass) throws JWTDecodeException {
        return null;
    }

    @Override
    public Map<String, Object> asMap() throws JWTDecodeException {
        return null;
    }

    @Override
    public <T> T as(Class<T> aClass) throws JWTDecodeException {
        return null;
    }
}
