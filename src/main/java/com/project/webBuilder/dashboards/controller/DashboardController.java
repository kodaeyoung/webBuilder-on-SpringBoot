package com.project.webBuilder.dashboards.controller;

import com.project.webBuilder.dashboards.dto.DashboardDTO;
import com.project.webBuilder.dashboards.service.DashboardService;
import com.project.webBuilder.global.exeption.custom.CustomException;
import com.project.webBuilder.global.exeption.errorcode.ErrorCode;
import com.project.webBuilder.global.response.ApiResponse;
import com.project.webBuilder.sharedTemplates.dto.SharedTemplateDTO;
import com.project.webBuilder.user.dto.UserDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
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
    public ResponseEntity<ApiResponse<List<DashboardDTO>>> myDashboard(Authentication authentication) {
        UserDTO userDTO = (UserDTO) authentication.getPrincipal();
        if (userDTO == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
        String email = userDTO.getEmail();

        List<DashboardDTO> dashboardDTO = dashboardService.getUserDashboard(email);
        ApiResponse<List<DashboardDTO>> response = new ApiResponse<>("Dashboard fetched successfully", dashboardDTO);
        return ResponseEntity.ok(response);

    }

    //프로젝트 이름 변경
    @PatchMapping("/{id}/update-name")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateProjectName(@RequestParam String newName, @PathVariable Long id) {
        dashboardService.updateProjectName(id, newName);
        Map<String, Object> responseData = Map.of("id", id, "projectName", newName);
        ApiResponse<Map<String, Object>> response = new ApiResponse<>("Project name updated successfully", responseData);
        return ResponseEntity.ok(response);
    }

    //프로젝트 공유
    @PostMapping("/share")
    public ResponseEntity<ApiResponse<Map<String, Object>>> projectShare(@RequestBody Map<String, Object> body, Authentication authentication) throws IOException {
        UserDTO userDTO = (UserDTO) authentication.getPrincipal();
        if (userDTO == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
        Long id = Long.parseLong(String.valueOf(body.get("selectedProjectId")));
        String templateName = (String) body.get("templateName");
        String category = (String) body.get("category");

        SharedTemplateDTO sharedTemplateDTO = dashboardService.projectShare(id, templateName, category, userDTO);
        Map<String, Object> responseData = Map.of(
                "success", "Project shared successfully",
                "sharedTemplate", sharedTemplateDTO
        );
        ApiResponse<Map<String, Object>> response = new ApiResponse<>("Project shared successfully", responseData);
        return ResponseEntity.ok(response);
    }

    // 대시보드 삭제
    @DeleteMapping("/{id}/remove")
    public ResponseEntity<ApiResponse<Map<String, Object>>> removeProject(@PathVariable Long id) throws IOException {
        dashboardService.removeProject(id);
        Map<String, Object> responseData = Map.of("id", id, "message", "Project removed successfully");
        ApiResponse<Map<String, Object>> response = new ApiResponse<>("Project removed successfully", responseData);
        return ResponseEntity.ok(response);
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


