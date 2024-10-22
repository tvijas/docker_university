package com.example.kuby.security.controller;

import com.example.kuby.exceptions.BasicException;
import com.example.kuby.foruser.UserEntity;
import com.example.kuby.foruser.UserRepo;
import com.example.kuby.security.models.enums.EmailCodeType;
import com.example.kuby.security.models.enums.Provider;
import com.example.kuby.security.models.request.ChangePasswordRequest;
import com.example.kuby.security.models.request.LoginRequest;
import com.example.kuby.security.models.request.OauthVerificationRequest;
import com.example.kuby.security.models.request.SignUpRequest;
import com.example.kuby.security.ratelimiter.WithRateLimitProtection;
import com.example.kuby.security.service.EmailSubmitCodeService;
import com.example.kuby.security.service.UserAuthenticationProvider;
import com.example.kuby.security.service.UserService;
import com.example.kuby.security.util.annotations.email.EmailExists;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.example.kuby.security.util.parsers.AuthHeaderParser.recoverToken;
import static com.example.kuby.security.util.parsers.ProviderEnumParser.getProviderFromString;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserAuthController {
    private final UserService userService;
    private final UserRepo userRepo;
    private final EmailSubmitCodeService emailSubmitCodeService;
    private final AuthenticationManager authenticationManager;
    private final UserAuthenticationProvider authenticationProvider;

    @PostMapping("/register")
    @WithRateLimitProtection
    @Transactional
    public ResponseEntity<?> register(@RequestBody @Valid SignUpRequest request) {
        userService.createUser(request.getEmail(), request.getLogin(), request.getPassword());

        emailSubmitCodeService.sendCodeToEmail(request.getEmail(), EmailCodeType.SUBMIT_EMAIL, Provider.LOCAL);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/verify/local")
    @WithRateLimitProtection
    public ResponseEntity<Void> verifyEmail(@RequestParam String code,
                                            @RequestParam String email) {
        emailSubmitCodeService.verifySubmissionEmailCode(code, email);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify/oauth")
    @WithRateLimitProtection
    public ResponseEntity<Void> verifyEmail(@Valid @RequestBody OauthVerificationRequest body) {
        emailSubmitCodeService.verifyOauthAccount(
                body.getCode(),
                body.getEmail(),
                getProviderFromString(body.getProvider()),
                body.getLogin()
        );

        return ResponseEntity.ok().build();
    }

    @PostMapping("/resend-submission-url")
    @WithRateLimitProtection(rateLimit = 3, rateDuration = 180_000)
    public ResponseEntity<Void> submitEmail(@RequestParam @Valid @Email @EmailExists String email) {
        emailSubmitCodeService.sendCodeToEmail(email, EmailCodeType.SUBMIT_EMAIL, Provider.LOCAL);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/login")
    @WithRateLimitProtection
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest request) {
        UserEntity userEntity = userRepo.findByLogin(request.getLogin())
                .orElseThrow(() -> new BasicException(Map.of("login_or_password", "Email or password isn't correct"), HttpStatus.NOT_FOUND));

        if (!userEntity.getProvider().equals(Provider.LOCAL))
            throw new BasicException(Map.of("login_or_password", "Email or password isn't correct"), HttpStatus.NOT_FOUND);

        if (!userEntity.isEmailSubmitted())
            throw new BasicException(Map.of("email", "Email is not verified"), HttpStatus.BAD_REQUEST);

        UsernamePasswordAuthenticationToken usernamePassword = new UsernamePasswordAuthenticationToken(request.getLogin(), request.getPassword());
        Authentication authUser = authenticationManager.authenticate(usernamePassword);

        if (!authUser.isAuthenticated())
            throw new BasicException(Map.of("login_or_password", "Email or password isn't correct"), HttpStatus.NOT_FOUND);

        String[] accessAndRefreshToken = authenticationProvider.generateTokens(userEntity);
        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + accessAndRefreshToken[0])
                .header("X-Refresh-Token", accessAndRefreshToken[1])
                .build();

    }

    @PostMapping("/token/refresh")
    @WithRateLimitProtection
    public ResponseEntity<?> refreshTokens(@RequestHeader("X-Refresh-Token") String refreshToken,
                                           @RequestHeader("Authorization") String accessToken) {
        String[] accessAndRefreshToken = authenticationProvider
                .refreshTokens(recoverToken(accessToken), refreshToken);

        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + accessAndRefreshToken[0])
                .header("X-Refresh-Token", accessAndRefreshToken[1])
                .build();
    }

    @PostMapping("/change-password")
    @WithRateLimitProtection
    public ResponseEntity<?> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        emailSubmitCodeService.sendCodeToEmail(request.getEmail(), EmailCodeType.CHANGE_PASSWORD, Provider.LOCAL);
        emailSubmitCodeService.cacheEmailAndPassword(request.getEmail(), request.getPassword());
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @PostMapping("/summit-password-change")
    @WithRateLimitProtection
    public ResponseEntity<?> submitPasswordChange(@RequestParam String code, @RequestParam String email) {
        String newPassword = emailSubmitCodeService.verifyChangePasswordSubmissionEmailCode(code, email);
        userService.changePassword(email, newPassword);
        return ResponseEntity.ok().build();
    }
}
