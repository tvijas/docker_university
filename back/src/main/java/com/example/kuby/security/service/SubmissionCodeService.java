package com.example.kuby.security.service;

import com.example.kuby.security.models.enums.Provider;
import com.example.kuby.security.util.generate.GenerateCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubmissionCodeService {
    private final StringRedisTemplate redisTemplate;
    private final GenerateCode generateCode;
    private final String EMAIL_SUBMISSION_CODE_PREFIX = "submission_code:";
    private final String PASSWORD_CHANGE_SUBMISSION_CODE_PREFIX = "change_password_submission_code:";
    private final String NEW_PASSWORD = "new_password:";

    public String createEmailSubmissionCodeWithExpiration(String email, Provider provider) {

        String code = generateCode.generateCode();

        redisTemplate.opsForValue().set(EMAIL_SUBMISSION_CODE_PREFIX + code + ":" + provider.toString().toUpperCase(), email);

        redisTemplate.expire(EMAIL_SUBMISSION_CODE_PREFIX + code, Duration.ofMinutes(5));

        return code;
    }

    public boolean isEmailSubmissionCodeExists(String code, String email, Provider provider) {
        String value = redisTemplate.opsForValue().get(EMAIL_SUBMISSION_CODE_PREFIX + code + ":" + provider.toString().toUpperCase());
        return value != null && value.equals(email);
    }

    public String createChangePasswordSubmissionCodeWithExpiration(String email) {
        String code = generateCode.generateCode();

        redisTemplate.opsForValue().set(PASSWORD_CHANGE_SUBMISSION_CODE_PREFIX + code, email);
        redisTemplate.expire(PASSWORD_CHANGE_SUBMISSION_CODE_PREFIX + code, Duration.ofMinutes(5));

        return code;
    }

    public void cacheEmailAndNewPasswordUntilSubmission(String email, String password) {
        redisTemplate.opsForValue().set(NEW_PASSWORD + email, password);
        redisTemplate.opsForValue().getAndExpire(NEW_PASSWORD + email, Duration.ofMinutes(5));
    }

    public Boolean isChangePasswordSubmissionCodeExists(String code, String email) {
        String value = redisTemplate.opsForValue().getAndDelete(PASSWORD_CHANGE_SUBMISSION_CODE_PREFIX + code);
        return value != null && value.equals(email);
    }

    public String popPasswordByEmail(String email) {
        return redisTemplate.opsForValue().getAndDelete(NEW_PASSWORD + email);
    }
}