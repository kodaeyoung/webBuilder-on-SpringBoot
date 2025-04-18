package com.project.webBuilder.common.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/store")
public class storeController {
    @GetMapping("/dashboard/**")
    public ResponseEntity<Void> handleDashboard(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String newUri = uri.replace("/store", "");  // '/store' 부분을 제거
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, newUri)
                .build();
    }

    @GetMapping("/sharedTemplate/**")
    public ResponseEntity<Void> handleTemplate(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String newUri = uri.replace("/store", "");  // '/store' 부분을 제거
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, newUri)
                .build();
    }

    @GetMapping("/deploy/**")
    public ResponseEntity<Void> handleDeploy(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String newUri = uri.replace("/store", "");  // '/store' 부분을 제거
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, newUri)
                .build();
    }

    @GetMapping("/sharedTemplateImage/**")
    public ResponseEntity<Void> handleSharedTemplateImage(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String newUri = uri.replace("/store", "");  // '/store' 부분을 제거
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, newUri)
                .build();
    }

    @GetMapping("/dashboardImage/**")
    public ResponseEntity<Void> handleDashboardImage(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String newUri = uri.replace("/store", "");  // '/store' 부분을 제거
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, newUri)
                .build();
    }
}
