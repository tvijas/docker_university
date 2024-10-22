package com.example.kuby.security.filter;

import com.example.kuby.security.service.CustomOAuth2UserService;
import com.example.kuby.security.service.OauthFailureHandler;
import com.example.kuby.security.service.OauthSuccessHandler;
import com.example.kuby.security.service.UserAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class SecurityFilter {
    private final JwtAuthFilter jwtAuthFilter;
    private final CustomOAuth2UserService oauthUserService;
    private final UserAuthenticationEntryPoint userAuthenticationEntryPoint;
    private final OauthFailureHandler oauthFailureHandler;
    private final OauthSuccessHandler oauthSuccessHandler;
    private final PermitAllUrlConfig permitAllUrlConfig;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(withDefaults())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> {
                            authorize
                                    .requestMatchers(HttpMethod.GET, "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                                    .requestMatchers("/api/user/**").permitAll()
                                    .requestMatchers(HttpMethod.POST, "/api/user/token/refresh").permitAll()

                                    .requestMatchers(HttpMethod.GET, "/login/oauth2/code/google/**").permitAll()
                                    .requestMatchers(HttpMethod.GET, "/oauth2/authorization/google").permitAll()
                                    //                         ^^^^ Call this url to login with google account ^^^^
                                    .anyRequest().authenticated();

                            permitAllUrlConfig
                                    .addPermitAllMatcher("/api/user/**")
                                    .addPermitAllMatcher(HttpMethod.POST, "/api/user/token/refresh")
                                    .addPermitAllMatcher(HttpMethod.GET, "/login/oauth2/code/google/**")
                                    .addPermitAllMatcher(HttpMethod.GET, "/oauth2/authorization/google");
                        }
                )
                .oauth2Login(auth -> {
                    auth.userInfoEndpoint(uiep ->
                            uiep.userService(oauthUserService));
                    auth.successHandler(oauthSuccessHandler);
                    auth.failureHandler(oauthFailureHandler);
                })
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(httpSecurityExceptionHandlingConfigurer ->
                        httpSecurityExceptionHandlingConfigurer.authenticationEntryPoint(userAuthenticationEntryPoint))
                .build();
    }
}

