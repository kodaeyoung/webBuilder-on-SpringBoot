package com.project.webBuilder.user.controller;


import com.project.webBuilder.global.exeption.custom.CustomException;
import com.project.webBuilder.global.exeption.errorcode.ErrorCode;
import com.project.webBuilder.global.response.ApiResponse;
import com.project.webBuilder.user.dto.UserDTO;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        // Authentication 객체에서 UserDTO를 가져옴
        UserDTO userDTO = (UserDTO) authentication.getPrincipal();

        // userDTO가 null이면, CustomException을 던져서 처리
        if (userDTO == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        // ApiResponse 사용하여 성공적인 응답을 반환
        ApiResponse<UserDTO> response = new ApiResponse<>(userDTO);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
