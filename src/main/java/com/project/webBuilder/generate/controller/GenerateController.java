package com.project.webBuilder.generate.controller;

import com.project.webBuilder.generate.service.GenerateService;
import com.project.webBuilder.user.dto.UserDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<?> generatePage(@RequestBody Map<String,Object> body, HttpSession session) throws IOException {
        UserDTO userDTO = (UserDTO) session.getAttribute("userDTO");
        String email =userDTO.getEmail();

        try{
            boolean generate = generateService.generate(body,email);
            if(generate){
                return ResponseEntity.ok("generate project successfully");
            }else{
                return ResponseEntity.status(404).body("appropriate template not found");
            }
        } catch (Exception e){
            return ResponseEntity.status(500).body("An error occurred: " + e.getMessage());
        }
    }
}
