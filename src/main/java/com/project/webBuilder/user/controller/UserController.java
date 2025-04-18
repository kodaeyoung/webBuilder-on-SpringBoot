package com.project.webBuilder.user.controller;


import com.project.webBuilder.user.dto.UserDTO;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(HttpSession session){
        UserDTO userDTO = (UserDTO) session.getAttribute("userDTO");

        if(userDTO==null){
            return new ResponseEntity<>("Can not find user", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(userDTO, HttpStatus.OK);
    }
}
