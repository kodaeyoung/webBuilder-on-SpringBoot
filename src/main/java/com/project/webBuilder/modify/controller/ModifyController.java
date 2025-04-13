package com.project.webBuilder.modify.controller;


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
    public ResponseEntity<String> modifyFile(@RequestBody Map<String,Object> body) {
        try {
            String result = modifyService.handleModifyRequest(body);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating HTML file: " + e.getMessage());
        }
    }
}
