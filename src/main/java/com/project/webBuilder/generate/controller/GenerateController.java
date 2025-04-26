package com.project.webBuilder.generate.controller;

import com.project.webBuilder.generate.service.GenerateService;
import com.project.webBuilder.global.exeption.custom.CustomException;
import com.project.webBuilder.global.exeption.errorcode.ErrorCode;
import com.project.webBuilder.global.response.ApiResponse;
import com.project.webBuilder.user.dto.UserDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class GenerateController {

    private final GenerateService generateService;
    @PostMapping("generate")
    public ResponseEntity<?> generatePage(@RequestBody Map<String,Object> body, Authentication authentication) throws IOException {

        UserDTO userDTO = (UserDTO) authentication.getPrincipal();
        if (userDTO == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
        String email = userDTO.getEmail();
        generateService.generate(body, email);
        return ResponseEntity.ok(new ApiResponse<>("Generate project successfully", null));
    }
}
