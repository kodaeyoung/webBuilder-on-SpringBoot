package com.project.webBuilder.modify.service;

import com.project.webBuilder.common.gpt.GptApi;
import com.project.webBuilder.common.util.Extraction;
import com.project.webBuilder.dashboards.entities.DashboardEntity;
import com.project.webBuilder.dashboards.repository.DashboardRepository;
import com.project.webBuilder.dashboards.service.DashboardService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ModifyService {
    private final DashboardRepository dashboardRepository;


    public String handleModifyRequest(Map<String, Object> body) throws IOException {
        String path = (String) body.get("path");
        String prompt = (String) body.get("prompt");

        if (path == null || prompt == null || path.isEmpty() || prompt.isEmpty()) {
            throw new IllegalArgumentException("File path and prompt are required.");
        }

        String[] promptParts = prompt.split("\\n\\n");
        String domElement = promptParts[0].replace("# DOM Element", "").trim();
        String modificationRequest = promptParts[1].replace("# Prompt", "").trim();

        domElement = domElement.replaceAll("\\sclass=\"\"", "");

        String gptGeneratedElement;

        if (domElement.equals("선택한 요소가 없습니다.")) {
           modifyEntirePage(path, modificationRequest);
        } else if (domElement.startsWith("<img")) {
            domElement = modifyDomElement(path, domElement);
            gptGeneratedElement = modifyImageElement(domElement, modificationRequest);
            updateHtmlFile(path, domElement, gptGeneratedElement);
        } else {
            gptGeneratedElement = modifyElement(domElement, modificationRequest);
            updateHtmlFile(path, domElement, gptGeneratedElement);
        }
        return "HTML file updated successfully.";
    }


    @Transactional
    private void modifyEntirePage(String filePath, String modificationRequest) throws IOException {
        try {
            // 1. HTML 파일 실제 경로 계산
            Path htmlFilePath = Paths.get(System.getProperty("user.dir")).resolve(filePath);

            // 2. store/dashboard/dir1/index.html 형태에서 store/dashboard/dir1 을 추출
            String[] parts = filePath.split(Pattern.quote(File.separator));
            if (parts.length < 4) {
                throw new IllegalArgumentException("파일 경로가 올바르지 않습니다: " + filePath);
            }
            String directoryPath = File.separator + parts[0] + File.separator + parts[1] + File.separator + parts[2]; // 예:  store/dashboard/dir1

            // 3. DB에서 dashboard 정보 조회
            DashboardEntity dashboardEntity = dashboardRepository.findByProjectPath(directoryPath)
                    .orElseThrow(()-> new RuntimeException("Dashboard not found for path: " + directoryPath));

            // 4. 기존 HTML 내용 읽기
            String htmlContent = Files.readString(htmlFilePath, StandardCharsets.UTF_8);

            // 5. GPT 프롬프트 구성
            String prompt = String.format("""
                Modify the HTML content below to match the user's requirements
                Current HTML:
                %s
                Modification Requirements:
                %s
                """, htmlContent, modificationRequest);

            // 6. GPT API 호출
            String gptResponse = GptApi.gpt(prompt,"You are a helpful assistant.",16384);  // gpt-4o-mini 호출로 구현

            // 7. <!DOCTYPE html>부터 </html>까지 추출
            Extraction.extractValidHtml(gptResponse);

            // 8. 수정된 HTML을 파일에 덮어쓰기
            Files.writeString(htmlFilePath, gptResponse, StandardCharsets.UTF_8);
/*
            // 9. 캡처 경로 생성
            Path screenshotPath = Paths.get(System.getProperty("user.dir")).resolve(dashboardEntity.getImagePath());

            // 10. 로컬 서버 주소
            String localServerUrl = "http://localhost:8080" + directoryPath + "/index.html";

            // 11. 캡처 (나중에 구현)
            // captureScreenshot(localServerUrl, screenshotPath.toString());
*/
            // 12. Dashboard 업데이트
            dashboardEntity.updateModified(true);
            dashboardRepository.save(dashboardEntity);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error processing prompt with GPT: " + e.getMessage());
        }
    }

    private String modifyDomElement(String path, String domElement) {
    }

    private String modifyImageElement(String domElement, String modificationRequest) {
    }

    private void updateHtmlFile(String path, String domElement, String gptGeneratedElement) {
    }
    //gpt를 호출하여 요구사항에 맞는 코드 생성
    private String modifyElement(String domElement, String modificationRequest) {
        return GptApi.gpt("You are an assistant that helps modify HTML DOM elements.",String.format("""
                Modify the following DOM element as requested and return only the modified element without including any additional characters or symbols.
                DOM Element: %s
                
                Modification: %s
                """,domElement,modificationRequest),1000);
    }


}
