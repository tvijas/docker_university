package com.example.kuby.security.service;

import com.example.kuby.exceptions.BasicException;
import com.example.kuby.security.models.enums.EmailCodeType;
import com.example.kuby.security.models.enums.Provider;
import com.example.kuby.foruser.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;


@Service
@RequiredArgsConstructor
public class EmailSubmitCodeService {
    private final JavaMailSender javaMailSender;
    private final UserRepo userRepo;
    @Value("${spring.mail.username}")
    private String emailAdmin;
    @Value("${frontend.url}")
    private String frontEndUrl;
    private final SubmissionCodeService submissionCodeService;
    @Value("${server.base-url}")
    private String backendUrl;


    @Transactional
    public void sendCodeToEmail(String toEmail, EmailCodeType codeType, Provider provider) {
        userRepo.findByEmailAndProvider(toEmail, Provider.LOCAL)
                .orElseThrow(() -> new BasicException(Map.of("email", "User with email " + toEmail + " not found."), HttpStatus.NOT_FOUND));

        String code = null;
        String base = null;

        if (codeType.equals(EmailCodeType.SUBMIT_EMAIL)) {
            code = submissionCodeService.createEmailSubmissionCodeWithExpiration(toEmail, provider);
            base = "verify/local";
        } else if (codeType.equals(EmailCodeType.CHANGE_PASSWORD)) {
            code = submissionCodeService.createChangePasswordSubmissionCodeWithExpiration(toEmail);
            base = "summit-password-change";
        } else {
            throw new RuntimeException("invalid code type");
        }

        String submissionUrl = frontEndUrl + "/user/" + base + "?code=" + code + "&email=" + toEmail;

        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(emailAdmin);
        simpleMailMessage.setTo(toEmail);
        simpleMailMessage.setSubject("Kuby - Email verification");
        simpleMailMessage.setText("To verify your action forward next link: \n\n" + submissionUrl);
        try {
            javaMailSender.send(simpleMailMessage);
        } catch (MailException ex) {
            throw new BasicException(Map.of("email", "Error occurred during email sending"), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public void verifySubmissionEmailCode(String code, String email) {
        if (!submissionCodeService.isEmailSubmissionCodeExists(code, email, Provider.LOCAL))
            throw new BasicException(Map.of("code", "Code is expired or not exists "), HttpStatus.NOT_FOUND);

        if (userRepo.updateIsEmailSubmittedByEmailAndProvider(email, Provider.LOCAL) != 1)
            throw new BasicException(Map.of("email", "User with such email not found"), HttpStatus.NOT_FOUND);
    }

    @Transactional
    public void verifyOauthAccount(String code, String email, Provider provider, String login) {
        if (!submissionCodeService.isEmailSubmissionCodeExists(code, email, provider))
            throw new BasicException(Map.of("code", "Code is expired or not exists "), HttpStatus.NOT_FOUND);

        if (userRepo.existsByLogin(login))
            throw new BasicException(Map.of("login", "User with such login already exists"), HttpStatus.NOT_FOUND);

        if (userRepo.updateLoginAndSetEmailSubmittedByEmailAndProvider(login, email,provider) != 1)
            throw new BasicException(Map.of("email", "User with such email not found"), HttpStatus.NOT_FOUND);
    }

    public String verifyChangePasswordSubmissionEmailCode(String code, String email) {
        if (!submissionCodeService.isChangePasswordSubmissionCodeExists(code, email))
            throw new BasicException(Map.of("code", "Code not found"), HttpStatus.NOT_FOUND);

        return submissionCodeService.popPasswordByEmail(email);
    }

    public void cacheEmailAndPassword(String email, String password) {
        submissionCodeService.cacheEmailAndNewPasswordUntilSubmission(email, password);
    }

    @Transactional
    public String generateOauthSubmissionUrl(String email, Provider provider) {
        if (provider.equals(Provider.LOCAL))
            throw new BasicException(Map.of("provider",
                    "Local provider can not be used for generating of the OAuth submission url"),
                    HttpStatus.BAD_REQUEST);
        String code = submissionCodeService.createEmailSubmissionCodeWithExpiration(email, provider);
        return frontEndUrl + "/api/user/verify/oauth?code=" + code + "&email=" + email + "&provider=" + provider.toString().toLowerCase();
        //frontEndUrl + base + link
    }

    //    @Transactional
//    public boolean isCodeExpired(String code) {
//        EmailSubmitCode byCode = emailSubmitCodeRepo.findByCode(code)
//                .orElseThrow(() -> new BasicException(Map.of("code", "Code doesn't exist"),HttpStatus.NOT_FOUND));
//
//        LocalDateTime expirationTime = byCode.getCreatedAt().plusMinutes(1).plusSeconds(30);
//
//        return LocalDateTime.now().isAfter(expirationTime);
//    }

}
