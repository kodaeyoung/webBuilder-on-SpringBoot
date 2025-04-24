package com.project.webBuilder.config;


import com.project.webBuilder.auth.JwtAuthFilter;
import com.project.webBuilder.auth.CustomOauth2UserService;
import com.project.webBuilder.auth.OAuth2LoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomOauth2UserService customOauth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers("/",
                                "/deploy/**",
                                "/sharedTemplate/get-all",
                                "/**/index.html",
                                "/**/*.css",
                                "/**/*.scss",
                                "/**/*.js",
                                "/**/*.png",
                                "/**/**/*.png",
                                "**/assets/**",
                                "**/fonts/**",
                                "/**/images/**",
                                "/login").permitAll() // 모든 사용자가 접근 가능
                        .anyRequest().authenticated() // 나머지 모든 요청은 인증된 사용자만 접근 가능
                )// 요청 URL에 따른 권한을 설정
                .logout(logout -> logout
                        .logoutUrl("/logout") // 로그아웃 경로
                        .logoutSuccessUrl("http://localhost:4000") // 로그아웃 성공 후 이동 경로
                        .invalidateHttpSession(true) // 세션 무효화
                        //.deleteCookies("JSESSIONID") // 쿠키 삭제
                )//로그아웃 시 리다이렉트될 url

                .oauth2Login(oauth2Login -> oauth2Login
                        //.defaultSuccessUrl("http://localhost:4000",true)// OAuth 2 로그인 설정 진입점
                        .successHandler(oAuth2LoginSuccessHandler)
                        .userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint
                                .userService(customOauth2UserService) // OAuth 2 로그인 성공 이후 사용자 정보를 가져올 때의 설정
                        )
                )
                .cors(withDefaults()) // CORS 설정 활성화
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                .exceptionHandling(exceptionHandling ->
                        exceptionHandling
                                .authenticationEntryPoint((request, response, authException) -> {
                                    // 인증되지 않은 사용자가 접근했을 때 리다이렉트할 URL 설정
                                    response.sendRedirect("http://localhost:4000/login");  // 인증되지 않은 사용자는 /login으로 리다이렉트
                                })
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4000"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS","PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
