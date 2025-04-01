package com.project.webBuilder.sharedTemplates.controller;


import com.project.webBuilder.dashboards.dto.DashboardDTO;
import com.project.webBuilder.sharedTemplates.dto.SharedTemplateDTO;
import com.project.webBuilder.sharedTemplates.service.SharedTemplateService;
import com.project.webBuilder.user.dto.UserDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("sharedTemplate")
public class SharedTemplateController {
    private final SharedTemplateService sharedTemplateService;

    // 모든 공유된 템플릿 호출
    @GetMapping("/getAll")
    public ResponseEntity<List<SharedTemplateDTO>> getAllSharedTemplates(){

        List<SharedTemplateDTO> sharedTemplateDTO = sharedTemplateService.getAllSharedTemplateDTO();

        if (sharedTemplateDTO.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);  // 템플릿이 없으면 204 No Content 반환4
        }
        return new ResponseEntity<>(sharedTemplateDTO, HttpStatus.OK);
    }

    // 공유된 템플릿 사용
    @PostMapping("/use")
    public ResponseEntity<DashboardDTO> useSharedTemplates(@RequestBody Map<String,Object> body, HttpSession session) throws IOException {
        Long id = Long.parseLong((String)body.get("id"));
        String projectName = (String)body.get("projectName");
        UserDTO userDTO = (UserDTO) session.getAttribute("userDTO");
        String email =userDTO.getEmail();
        System.out.println("projectName:"+projectName);
        DashboardDTO dashboardDTO = sharedTemplateService.useSharedTemplate(id,projectName,email);

        return new ResponseEntity<>(dashboardDTO, HttpStatus.OK);
    }

    @GetMapping("/{templateDir}")
    public ResponseEntity<Void> forwardSharedTemplate(@PathVariable String templateDir) {
        // sharedTemplate/{templateDir}/index.html로 포워드
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, "/sharedTemplate/" + templateDir + "/index.html")
                .build();
    }

}
