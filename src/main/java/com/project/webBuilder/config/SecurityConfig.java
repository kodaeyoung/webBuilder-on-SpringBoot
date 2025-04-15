package com.project.webBuilder.config;


import com.project.webBuilder.auth.CustomOauth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomOauth2UserService customOauth2UserService;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers("/", "/sharedTemplateStore/get-all"
                                ,"/**/index.html","/**/*.css","/**/*.scss","/**/*.js","/**/*.png").permitAll() // 모든 사용자가 접근 가능
                        .anyRequest().authenticated() // 나머지 모든 요청은 인증된 사용자만 접근 가능
                )// 요청 URL에 따른 권한을 설정

                .logout(logout -> logout.logoutSuccessUrl("/")) //로그아웃 시 리다이렉트될 url
                .oauth2Login(oauth2Login -> oauth2Login
                        .defaultSuccessUrl("/sharedTemplates/getAll")// OAuth 2 로그인 설정 진입점
                        .userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint
                                .userService(customOauth2UserService) // OAuth 2 로그인 성공 이후 사용자 정보를 가져올 때의 설정
                        )
                )

/*
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling
                                .authenticationEntryPoint((request, response, authException) -> {
                                    // 인증되지 않은 사용자가 접근했을 때 리다이렉트할 URL 설정
                                    response.sendRedirect("http://localhost:3000/login");  // 인증되지 않은 사용자는 /login으로 리다이렉트
                                })


                )*/;

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
