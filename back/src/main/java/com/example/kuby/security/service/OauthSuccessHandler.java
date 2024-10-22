package com.example.kuby.security.service;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
@Component
@RequiredArgsConstructor
public class OauthSuccessHandler implements AuthenticationSuccessHandler {
    private final EmailSubmitCodeService emailSubmitCodeService;
    private final UserAuthenticationProvider userAuthenticationProvider;
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        CustomOAuth2User oauthUser = (CustomOAuth2User) authentication.getPrincipal();
        if (!oauthUser.getUser().isEmailSubmitted()) {
            String emailSubmissionUrl = emailSubmitCodeService
                    .generateOauthSubmissionUrl(
                            oauthUser.getUser().getEmail(),
                            oauthUser.getUser().getProvider());
            response.sendRedirect(emailSubmissionUrl);
            return;
        }
        String[] accessAndRefreshToken = userAuthenticationProvider.generateTokens(oauthUser.getUser());
        response.addHeader("Authorization", "Bearer " + accessAndRefreshToken[0]);
        response.addHeader("X-Refresh-Token", accessAndRefreshToken[1]);
        response.setStatus(HttpStatus.OK.value());
//                            response.sendRedirect(frontendUrl); ??? TODO maybe not needed
    }
}
