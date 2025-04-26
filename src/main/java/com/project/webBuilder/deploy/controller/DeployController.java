package com.project.webBuilder.deploy.controller;

import com.project.webBuilder.deploy.service.DeployService;
import com.project.webBuilder.global.exeption.custom.CustomException;
import com.project.webBuilder.global.exeption.errorcode.ErrorCode;
import com.project.webBuilder.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class DeployController {
    private final DeployService deployService;

    // 프로젝트 배포하기
    @PostMapping("/deploy")
    public ResponseEntity<?> deployProject(@RequestBody Map<String, Object> body) {
        Long id = Long.parseLong(String.valueOf(body.get("id")));
        String deployName = String.valueOf(body.get("deployName"));

        try {
            boolean deploy = deployService.deployProject(id, deployName);
            if (deploy) {
                return ResponseEntity.ok(new ApiResponse<>("Project deploy successfully", null));
            } else {
                throw new CustomException(ErrorCode.TEMPLATE_NOT_FOUND); // Project not found 에러를 CustomException으로 처리
            }
        }catch (Exception ex) {
            // 예상치 못한 예외는 RuntimeException으로 던짐
            throw new RuntimeException(ex);
        }
    }

    // 프로젝트 배포 중지
    @PostMapping("/undeploy")
    public ResponseEntity<?> undeployProject(@RequestBody Map<String, Object> body) {
        Long id = Long.parseLong(String.valueOf(body.get("id")));
        try {
            boolean undeploy = deployService.undeployProject(id);
            if (undeploy) {
                return ResponseEntity.ok(new ApiResponse<>("Project undeploy successfully", null));
            } else {
                throw new CustomException(ErrorCode.PROJECT_NOT_FOUND); // Deploy project not found 에러를 CustomException으로 처리
            }
        }catch (Exception ex) {
            // 예상치 못한 예외는 RuntimeException으로 던짐
            throw new RuntimeException(ex);
        }
    }

    // 프로젝트 배포 업데이트
    @PostMapping("update-deploy")
    public ResponseEntity<?> updateDeploy(@RequestBody Map<String, Object> body) {
        Long id = Long.parseLong(String.valueOf(body.get("id")));

        try {
            boolean update = deployService.updateDeploy(id);
            if (update) {
                return ResponseEntity.ok(new ApiResponse<>("Deploy update successfully", null));
            } else {
                throw new CustomException(ErrorCode.PROJECT_NOT_FOUND); // Deploy project not found 에러를 CustomException으로 처리
            }
        } catch (Exception e) {
            // 예상치 못한 예외는 RuntimeException으로 던짐
            throw new RuntimeException("An error occurred: " + e.getMessage(), e);
        }
    }

    @GetMapping("/deploy/{deployDir}")
    public ResponseEntity<Void> forwardDeploy(@PathVariable String deployDir) {
        // 경로 인코딩
        String encodedDir = URLEncoder.encode(deployDir, StandardCharsets.UTF_8);

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, "/deploy/" + encodedDir + "/index.html")
                .build();
    }

}
