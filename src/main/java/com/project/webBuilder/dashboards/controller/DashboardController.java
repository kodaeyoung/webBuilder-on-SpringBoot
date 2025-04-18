package com.project.webBuilder.dashboards.controller;

import com.project.webBuilder.dashboards.dto.DashboardDTO;
import com.project.webBuilder.dashboards.service.DashboardService;
import com.project.webBuilder.sharedTemplates.dto.SharedTemplateDTO;
import com.project.webBuilder.user.dto.UserDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    //사용자 대시보드 호출
    @GetMapping("/my-dashboard")
    public ResponseEntity<List<DashboardDTO>> myDashboard(HttpSession session){
        UserDTO userDTO = (UserDTO) session.getAttribute("userDTO");
        String email = userDTO.getEmail();
        List<DashboardDTO> dashboardDTO = dashboardService.getUserDashboard(email);

        if(dashboardDTO.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(dashboardDTO,HttpStatus.OK);
    }

    //프로젝트 이름 변경
    @PatchMapping("/{id}/update-name")
    public ResponseEntity<?> updateProjectName(@RequestParam String newName, @PathVariable Long id){
        try {
            // 사용자 이름 업데이트 서비스 호출
            boolean updated = dashboardService.updateProjectName(id,newName);

            if (updated) {
                return ResponseEntity.ok(Map.of("id", id, "projectName", newName));
            } else {
                return ResponseEntity.status(404).body("Project not found");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An error occurred: " + e.getMessage());
        }
    }

    //프로젝트 공유
    @PostMapping("/share")
    public ResponseEntity<?> projectShare(@RequestBody Map<String,Object> body , HttpSession session){
        UserDTO userDTO = (UserDTO) session.getAttribute("userDTO");
        Long id = Long.parseLong(String.valueOf(body.get("selectedProjectId")));
        String templateName = (String) body.get("templateName");
        String category = (String) body.get("category");

        try{
            SharedTemplateDTO sharedTemplateDTO = dashboardService.projectShare(id,templateName,category,userDTO);

            if(sharedTemplateDTO!=null){
                return ResponseEntity.ok(Map.of("success","project shared successfully","sharedTemplate",sharedTemplateDTO));
            }else{
                return ResponseEntity.status(404).body("project not found");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An error occurred: " + e.getMessage());
        }
    }

    //대시보드 삭제
    @DeleteMapping("/{id}/remove")
    public ResponseEntity<?> removePoject(@PathVariable Long id){
        try{
            boolean remove = dashboardService.removeProject(id);
            if(remove){
                return ResponseEntity.ok("Project remove successfully");
            }else{
                return ResponseEntity.status(404).body("Project not found");
            }
        } catch (Exception e){
            return ResponseEntity.status(500).body("An error occurred: " + e.getMessage());
        }
    }


    @GetMapping("/{dashboardDir}")
    public ResponseEntity<Void> forwardSharedTemplate(@PathVariable String dashboardDir) {
        // sharedTemplate/{dashboarDir}/index.html로 포워드
        String encodedDir = URLEncoder.encode(dashboardDir, StandardCharsets.UTF_8);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, "/sharedTemplate/" + encodedDir + "/index.html")
                .build();
    }
}


