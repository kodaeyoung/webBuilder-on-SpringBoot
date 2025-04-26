package com.project.webBuilder.sharedTemplates.controller;


import com.project.webBuilder.dashboards.dto.DashboardDTO;
import com.project.webBuilder.global.exeption.custom.CustomException;
import com.project.webBuilder.global.exeption.errorcode.ErrorCode;
import com.project.webBuilder.global.response.ApiResponse;
import com.project.webBuilder.sharedTemplates.dto.SharedTemplateDTO;
import com.project.webBuilder.sharedTemplates.service.SharedTemplateService;
import com.project.webBuilder.user.dto.UserDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("sharedTemplate")
public class SharedTemplateController {
    private final SharedTemplateService sharedTemplateService;

    // 모든 공유된 템플릿 호출
    @GetMapping("/get-all")
    public ResponseEntity<?> getAllSharedTemplates() {
        try {
            List<SharedTemplateDTO> sharedTemplateDTO = sharedTemplateService.getAllSharedTemplateDTO();

            // 템플릿이 없으면 204 No Content
            if (sharedTemplateDTO.isEmpty()) {
                ApiResponse<List<SharedTemplateDTO>> response = new ApiResponse<>("No shared templates found.", sharedTemplateDTO);
                return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);  // 204 No Content 응답
            }

            // 템플릿이 있으면 성공 응답 처리
            ApiResponse<List<SharedTemplateDTO>> response = new ApiResponse<>(sharedTemplateDTO);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception ex) {
            throw new RuntimeException("서버 오류가 발생했습니다.", ex);
        }
    }

    // 공유된 템플릿 사용
    @PostMapping("/use")
    public ResponseEntity<ApiResponse<DashboardDTO>> useSharedTemplates(@RequestBody Map<String,Object> body, Authentication authentication) throws IOException {
        try {
            // 요청 본문에서 템플릿 ID와 프로젝트 이름을 추출
            Long id = Long.parseLong(String.valueOf(body.get("selectedTemplateId")));
            String projectName = String.valueOf(body.get("projectName"));

            // 인증된 사용자 정보 가져오기
            UserDTO userDTO = (UserDTO) authentication.getPrincipal();
            if (userDTO == null) {
                throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS, "사용자가 인증되지 않았습니다.");
            }
            String email = userDTO.getEmail();

            // 템플릿 사용 서비스 호출
            DashboardDTO dashboardDTO = sharedTemplateService.useSharedTemplate(id, projectName, email);

            // 성공적인 응답 처리
            ApiResponse<DashboardDTO> response = new ApiResponse<>(dashboardDTO);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (NumberFormatException ex) {
            // 요청 데이터에 숫자 형식 오류가 있을 때 처리 (예: ID가 숫자가 아닐 경우)
            throw new CustomException(ErrorCode.INVALID_REQUEST, "잘못된 템플릿 ID 형식입니다.");
        } catch (NullPointerException ex) {
            // 요청 데이터가 누락되었을 때 처리 (예: selectedTemplateId 또는 projectName이 null인 경우)
            throw new CustomException(ErrorCode.INVALID_REQUEST, "필수 데이터가 누락되었습니다.");
        } catch (Exception ex) {
            // 예상치 못한 오류 발생 시 500 Internal Server Error 처리
            throw new RuntimeException("서버 오류가 발생했습니다.", ex);
        }
    }


    //로그인한 사용자가 공유한 템플릿 호출
    @GetMapping("get-mine")
    public ResponseEntity<ApiResponse<List<SharedTemplateDTO>>> getMySharedTemplates(Authentication authentication) {
        try {
            // 인증된 사용자 정보 가져오기
            UserDTO userDTO = (UserDTO) authentication.getPrincipal();
            if (userDTO == null) {
                throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
            }
            String email = userDTO.getEmail();

            // 사용자에 대한 공유된 템플릿 조회
            List<SharedTemplateDTO> sharedTemplateDTO = sharedTemplateService.getMySharedTemplates(email);

            // 템플릿이 없으면 204 No Content 응답
            if (sharedTemplateDTO.isEmpty()) {
                ApiResponse<List<SharedTemplateDTO>> response = new ApiResponse<>("No shared templates found.", sharedTemplateDTO);
                return new ResponseEntity<>(response, HttpStatus.NO_CONTENT); // 204 No Content
            }

            // 템플릿이 있으면 성공 응답
            ApiResponse<List<SharedTemplateDTO>> response = new ApiResponse<>(sharedTemplateDTO);
            return new ResponseEntity<>(response, HttpStatus.OK); // 200 OK

        } catch (NullPointerException ex) {
            // 인증 정보가 없으면 401 Unauthorized 처리
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        } catch (Exception ex) {
            // 예기치 못한 서버 오류 처리
            throw new RuntimeException(ex);
        }
    }

    //공유된 템플릿 삭제
    @DeleteMapping("/{id}/remove")
    public ResponseEntity<ApiResponse<Map<String, Object>>> removeSharedTemplate(@PathVariable Long id,Authentication authentication) {
        try {
            UserDTO userDTO = (UserDTO) authentication.getPrincipal();
            if (userDTO == null) {
                throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
            }

            boolean remove = sharedTemplateService.removeSharedTemplate(id);

            // 삭제된 템플릿이 있을 경우
            if (remove) {
                Map<String, Object> responseData = Map.of(
                        "success", true,
                        "message", "SharedTemplate removed successfully",
                        "id", id
                );
                ApiResponse<Map<String, Object>> response = new ApiResponse<>(responseData);
                return new ResponseEntity<>(response, HttpStatus.OK);  // 200 OK 응답
            } else {
                // 템플릿을 찾지 못한 경우
                throw new CustomException(ErrorCode.TEMPLATE_NOT_FOUND);  // 404 Not Found
            }

        } catch (Exception ex) {
            // 예기치 못한 오류 발생 시 500 Internal Server Error
            throw new RuntimeException(ex);
        }
    }


    @GetMapping("/{templateDir}")
    public ResponseEntity<Void> forwardSharedTemplate(@PathVariable String templateDir) {
        // sharedTemplate/{templateDir}/index.html로 포워드
        String encodedDir = URLEncoder.encode(templateDir, StandardCharsets.UTF_8);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, "/sharedTemplate/" + encodedDir + "/index.html")
                .build();
    }

}
