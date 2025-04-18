package com.project.webBuilder.deploy.controller;

import com.project.webBuilder.deploy.service.DeployService;
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
    public ResponseEntity<?> deployProject(@RequestBody Map<String,Object> body){
        Long id = Long.parseLong(String.valueOf(body.get("id")));
        String deployName = String.valueOf(body.get("deployName"));

        try {
            boolean deploy = deployService.deployProject(id,deployName);
            if(deploy){
                return ResponseEntity.ok("Project deploy successfully");
            }else{
                return ResponseEntity.status(404).body("Project not found");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An error occurred: "+e.getMessage());
        }
    }

    //프로젝트 배포 중지
    @PostMapping("/undeploy")
    public ResponseEntity<?> undeployProject(@RequestBody Map<String,Object> body){
        Long id = Long.parseLong(String.valueOf(body.get("id")));
        try {
            boolean undeploy = deployService.undeployProject(id);
            if(undeploy){
                return ResponseEntity.ok("Project undeploy successfully");
            }else{
                return ResponseEntity.status(404).body("Deploy project not found");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An error occurred: "+e.getMessage());
        }
    }

    // 프로젝트 배포 업데이트
    @PostMapping("update-deploy")
    public ResponseEntity<?> updateDeploy(@RequestBody Map<String,Object> body){
        Long id = Long.parseLong(String.valueOf(body.get("id")));
        try {
            boolean update = deployService.updateDeploy(id);
            if(update){
                return ResponseEntity.ok("Deploy update successfully");
            }else{
                return ResponseEntity.status(404).body("Deploy project not found");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An error occurred: "+e.getMessage());
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
