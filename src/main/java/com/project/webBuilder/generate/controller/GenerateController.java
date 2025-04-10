package com.project.webBuilder.generate.controller;

import com.project.webBuilder.generate.service.GenerateService;
import com.project.webBuilder.user.dto.UserDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class GenerateController {

    private final GenerateService generateService;
    @PostMapping("generate")
    public ResponseEntity<?> generatePage(@RequestBody Map<String,Object> body, HttpSession session){
        UserDTO userDTO = (UserDTO) session.getAttribute("userDTO");
        String email =userDTO.getEmail();

        boolean generate = generateService.generate(body,email);
    }
}
