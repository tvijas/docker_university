package com.example.kuby.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfiguration implements WebMvcConfigurer {
    @Value("${frontend.url}")
    public String frontendUrl;

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        org.springframework.web.cors.CorsConfiguration config = new org.springframework.web.cors.CorsConfiguration();

        // Разрешаем запросы из любого источника
        config.addAllowedOriginPattern("*");
        config.setAllowCredentials(true);

        // Разрешаем использование следующих методов HTTP
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("PATCH");
        config.addAllowedMethod("OPTIONS");

        // Разрешаем использование следующих заголовков
        config.addAllowedHeader("Origin");
        config.addAllowedHeader("Content-Type");
        config.addAllowedHeader("Accept");
        config.addAllowedHeader("Authorization");
        config.addAllowedHeader("X-Refresh-Token");
        config.addAllowedHeader("Access-Control-Allow-Origin");
        config.addAllowedHeader("Access-Control-Allow-Headers");
        config.addAllowedHeader("*");

        config.addExposedHeader("Authorization");
        config.addExposedHeader("X-Refresh-Token");

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    //    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        org.springframework.web.cors.CorsConfiguration config = new org.springframework.web.cors.CorsConfiguration();
//        config.setAllowedOrigins(List.of("http://localhost:3000")); // Укажите здесь адрес вашего фронтенда
//        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT","PATCH", "DELETE", "OPTIONS"));
//        config.setAllowedHeaders(Arrays.asList("Origin", "Authorization", "Cache-Control", "Content-Type"));
//        config.setAllowCredentials(true); // Это позволяет отправлять куки
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", config);
//        return source;
//    }
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .maxAge(3600L)
                .allowedMethods("*")
                .allowedHeaders("*")
                .exposedHeaders("Upgrade")
                .allowCredentials(true);
    }
}
