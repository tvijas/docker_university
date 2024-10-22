package com.example.kuby.security.filter;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class PermitAllUrlConfig {

    private final List<RequestMatcher> permitAllMatchers = new ArrayList<>();

    public PermitAllUrlConfig addPermitAllMatcher(HttpMethod httpMethod, String pattern) {
        permitAllMatchers.add(new AntPathRequestMatcher(pattern,httpMethod.name()));
        return this;
    }
    public PermitAllUrlConfig addPermitAllMatcher( String pattern) {
        permitAllMatchers.add(new AntPathRequestMatcher(pattern));
        return this;
    }

    public boolean isPermitAllRequest(HttpServletRequest request) {
        return permitAllMatchers.stream()
                .anyMatch(matcher -> matcher.matches(request));
    }
}