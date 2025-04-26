package com.project.webBuilder.modify.controller;


import com.project.webBuilder.global.exeption.custom.CustomException;
import com.project.webBuilder.global.exeption.errorcode.ErrorCode;
import com.project.webBuilder.global.response.ApiResponse;
import com.project.webBuilder.modify.service.ModifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ModifyController {
    private final ModifyService modifyService;


    @PostMapping("/modify")
    public ResponseEntity<ApiResponse<String>> modifyFile(@RequestBody Map<String, Object> body) {
        try {
            // 요청 처리
            String result = modifyService.handleModifyRequest(body);

            // 성공 응답
            ApiResponse<String> response = new ApiResponse<>(result);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            // 잘못된 요청에 대해서는 400 Bad Request 처리
            throw new CustomException(ErrorCode.INVALID_REQUEST, e.getMessage());

        } catch (Exception ex) {
            // 예기치 않은 서버 오류에 대해서는 500 Internal Server Error 처리
            throw new RuntimeException(ex);
        }
    }
}
