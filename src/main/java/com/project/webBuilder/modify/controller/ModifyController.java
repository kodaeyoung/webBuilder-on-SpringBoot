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

import java.io.IOException;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ModifyController {
    private final ModifyService modifyService;


    @PostMapping("/modify")
    public ResponseEntity<ApiResponse<String>> modifyFile(@RequestBody Map<String, Object> body) throws IOException {
        String result = modifyService.handleModifyRequest(body);
        ApiResponse<String> response = new ApiResponse<>(result);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
