package com.example.kuby.security.util.parsers;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.Claim;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class InstantClaim implements Claim {
    private final Instant instant;
    private final boolean isMissing;

    public InstantClaim(Instant instant) {
        this.instant = instant;
        this.isMissing = (instant == null);
    }

    @Override
    public boolean isNull() {
        return instant == null;
    }

    @Override
    public boolean isMissing() {
        return isMissing;
    }

    @Override
    public Instant asInstant() {
        return instant;
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
        return instant != null ? instant.getEpochSecond() : null;
    }

    @Override
    public Double asDouble() {
        return null;
    }

    @Override
    public String asString() {
        return instant != null ? instant.toString() : null;
    }

    @Override
    public Date asDate() {
        return instant != null ? Date.from(instant) : null;
    }

    @Override
    public <T> T[] asArray(Class<T> clazz) throws JWTDecodeException {
        return null;
    }

    @Override
    public <T> List<T> asList(Class<T> clazz) throws JWTDecodeException {
        return null;
    }

    @Override
    public Map<String, Object> asMap() throws JWTDecodeException {
        return null;
    }

    @Override
    public <T> T as(Class<T> clazz) throws JWTDecodeException {
        if (clazz == Instant.class) {
            return clazz.cast(instant);
        }
        throw new JWTDecodeException("Cannot convert to " + clazz.getName());
    }
}