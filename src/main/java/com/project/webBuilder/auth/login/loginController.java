package com.project.webBuilder.auth.login;

import com.project.webBuilder.auth.jwt.JwtUtil;
import com.project.webBuilder.auth.jwt.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RequiredArgsConstructor
@RestController
public class loginController {
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    @GetMapping("/login")
    public void handleLogin(HttpServletResponse response) throws IOException {
        response.sendRedirect("/oauth2/authorization/google");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String token = jwtUtil.resolveToken(request);

        if (token != null && jwtUtil.validateToken(token)) {
            tokenBlacklistService.blacklist(token); // Redis 또는 DB에 저장
        }
        return ResponseEntity.ok("Logged out"); // 리디렉션 안함
    }



}
