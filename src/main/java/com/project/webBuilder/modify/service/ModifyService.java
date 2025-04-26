package com.project.webBuilder.modify.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.webBuilder.global.exeption.custom.CustomException;
import com.project.webBuilder.global.exeption.errorcode.ErrorCode;
import com.project.webBuilder.global.gpt.GptApi;
import com.project.webBuilder.global.util.Extraction;
import com.project.webBuilder.global.util.Screenshot;
import com.project.webBuilder.dashboards.entities.DashboardEntity;
import com.project.webBuilder.dashboards.repository.DashboardRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ModifyService {
    private final DashboardRepository dashboardRepository;
    @Value("${google.pse.id}")
    private String pseId;

    @Value("${google.api.key}")
    private String cseApiKey;


    public String handleModifyRequest(Map<String, Object> body) throws IOException {
        // 요청 본문에서 path와 prompt 값을 추출
        String path = (String) body.get("path");
        String prompt = (String) body.get("prompt");

        // 필수 값 검증
        if (path == null || prompt == null || path.isEmpty() || prompt.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST,"File path and prompt are required.");
        }

        // 프롬프트를 두 부분으로 분리: DOM 요소 + 수정 요청
        String[] promptParts = prompt.split("\\n\\n");
        if (promptParts.length < 2) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "Prompt format is invalid. Must contain DOM Element and Prompt parts.");
        }

        String domElement = promptParts[0].replace("# DOM Element", "").trim();         // DOM Element 부분만 추출
        String modificationRequest = promptParts[1].replace("# Prompt", "").trim();     // Prompt 부분만 추출

        // class=""와 같이 빈 클래스 속성이 있을 경우 제거
        domElement = domElement.replaceAll("\\sclass=\"\"", "");

        String gptGeneratedElement;

        // 1. 선택한 요소가 없으면 전체 페이지 수정 로직 실행
        if (domElement.equals("선택한 요소가 없습니다.")) {
            modifyEntirePage(path, modificationRequest);

            // 2. 이미지 태그인 경우: src 경로 변환 후, GPT로 이미지 검색 → HTML 수정
        } else if (domElement.startsWith("<img")) {
            domElement = convertDomElementSrcPath(path, domElement); // 상대 경로로 변환
            gptGeneratedElement = modifyImageElementFromPrompt(domElement, modificationRequest); // 이미지 수정
            updateHtmlFile(path, domElement, gptGeneratedElement); // HTML 파일 업데이트

            // 3. 일반 DOM 요소 수정: GPT로 수정 후 파일 업데이트
        } else {
            gptGeneratedElement = modifyElement(domElement, modificationRequest); // GPT로 DOM 수정
            updateHtmlFile(path, domElement, gptGeneratedElement); // HTML 파일 업데이트
        }

        return "HTML file updated successfully.";
    }



    private void modifyEntirePage(String filePath, String modificationRequest) throws IOException {

        // 1. HTML 파일 실제 경로 계산
        Path htmlFilePath = Paths.get(System.getProperty("user.dir")).resolve(filePath);

        // 2. store/dashboard/dir1/index.html 형태에서 store/dashboard/dir1 을 추출
        String[] parts = filePath.split(Pattern.quote(File.separator));
        if (parts.length < 4) {
            throw new CustomException(ErrorCode.INVALID_REQUEST,"파일 경로가 올바르지 않습니다: " + filePath);
        }
        String directoryPath = File.separator + parts[0] + File.separator + parts[1] + File.separator + parts[2]; // 예:  store/dashboard/dir1

        // 3. DB에서 dashboard 정보 조회
        DashboardEntity dashboardEntity = dashboardRepository.findByProjectPath(directoryPath)
                .orElseThrow(()-> new CustomException(ErrorCode.PROJECT_NOT_FOUND,"Dashboard not found for path: " + directoryPath));

        try {
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
            String gptResponse = GptApi.gpt(prompt, "You are a helpful assistant.", 16384);  // gpt-4o-mini 호출로 구현

            // 7. <!DOCTYPE html>부터 </html>까지 추출
            Extraction.extractValidHtml(gptResponse);

            // 8. 수정된 HTML을 파일에 덮어쓰기
            Files.writeString(htmlFilePath, gptResponse, StandardCharsets.UTF_8);

            // 9. 캡처 경로 생성
            Path screenshotPath = Paths.get(System.getProperty("user.dir")).resolve(dashboardEntity.getImagePath());

            // 10. 로컬 서버 주소
            String localServerUrl = "http://localhost:8080/" + directoryPath + "/index.html";

            // 11. 캡처 (나중에 구현)
            Screenshot.takeScreenshot(localServerUrl, screenshotPath.toString());

            // 12. Dashboard 업데이트
            dashboardEntity.updateModified(true);
            dashboardRepository.save(dashboardEntity);
        } catch (IOException | RuntimeException e) {
            throw new RuntimeException("Error processing prompt with GPT: " + e.getMessage(), e);
        }
    }

    private String convertDomElementSrcPath(String pagePath, String domElement) {
        // src="..." 패턴 추출
        // ex) <img src="http://localhost:8080/store/dashboard/template1/img/about/about.png"> 에서
        // http://localhost:8080/store/dashboard/template1/img/about/about.png를 추출
        Pattern pattern = Pattern.compile("src=\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(domElement);

        if (matcher.find()) {
            String fullSrc = matcher.group(1); // 전체 src 값

            if (fullSrc.startsWith("http://localhost:8080")) {
                // 디렉터리 경로만 추출
                // ex) store/dashboard/template1/index.html 에서 store/dashboard/template1 을 추출
                Path directoryPath = Paths.get(pagePath).getParent();
                String fullPath = "http://localhost:8080" + directoryPath.toString().replace("\\", "/") + "/";

                // src에서 경로 부분만 제거해서 상대 경로로 만듦
                // ex) http://localhost:8080/store/dashboard/template1/img/about/about.png 에서 img/about/about.png 추출
                String originalSrcPath = fullSrc.replace(fullPath, "");

                // src 경로 수정
                // ex ) <img src="http://localhost:8080/store/dashboard/template1/img/about/about.png"> 에서
                // <img src="img/about/about.png"> 로 파싱
                // ** 실제 원본 html 파일에는 <img src="img/about/about.png"> 으로 작성 되어있음 **
                return domElement.replace(fullSrc, originalSrcPath);
            }
        }
        // &amp; -> & 처리 (이미 수정된 경우)
        return domElement.replace("&amp;", "&");
    }

    public String modifyImageElementFromPrompt(String domElement, String modificationRequest) throws IOException {

        try {
            // 1. GPT에게 핵심 키워드 추출 요청
            String systemPrompt = "You are an assistant that extracts the main keywords from user modification requests. Your job is to identify the most relevant and concise keywords that best represent the user's intent for an image search. Respond only with the main keywords in a comma-separated format.";

            String userPrompt = String.format("User modification request: \"%s\". Extract the main keywords that would be most suitable for finding an image related to this request.", modificationRequest);

            String extractedKeywords = GptApi.gpt(systemPrompt, userPrompt, 1000);

            // 2. Google Custom Search API로 이미지 검색
            String apiUrl = String.format(
                    "https://www.googleapis.com/customsearch/v1?q=%s&cx=%s&key=%s&searchType=image&num=1",
                    URLEncoder.encode(extractedKeywords, StandardCharsets.UTF_8),
                    pseId,
                    cseApiKey
            );
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(apiUrl, String.class);

            if (response == null) {
                throw new RuntimeException("Google Search API 응답이 없습니다.");
            }


            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(response);
            JsonNode items = json.get("items");
            if (items == null || !items.isArray() || items.size() == 0) {
                throw new RuntimeException("적절한 이미지를 찾을 수 없습니다.");
            }

            String selectedImageUrl = items.get(0).get("link").asText();

            // 3. domElement의 src 속성을 selectedImageUrl로 변경
            String updatedDomElement = domElement.replaceAll("src=\"[^\"]*\"", "src=\"" + selectedImageUrl + "\"");

            return updatedDomElement;

        } catch (IOException e) {
            throw new IOException("JSON 파싱 중 오류가 발생", e);
        } catch (Exception e) {
            throw new RuntimeException("이미지 요소 수정 중 오류가 발생", e);
        }
    }

    private void updateHtmlFile(String filePath, String domElement, String gptGeneratedElement) throws IOException {

        // 예: store/dashboard/template1 형식의 디렉토리 추출
        String[] parts = filePath.split(Pattern.quote(File.separator));
        if (parts.length < 3) {
            throw new CustomException(ErrorCode.INVALID_REQUEST,"파일 경로가 올바르지 않습니다: " + filePath);
        }

        String directoryPath = parts[0] + File.separator + parts[1] + File.separator + parts[2];  // 예: store\dashboard\template1

        // Dashboard 조회
        DashboardEntity dashboardEntity = dashboardRepository.findByProjectPath(directoryPath)
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));

        // 실제 HTML 파일의 경로 구성
        Path htmlFilePath = Paths.get(System.getProperty("user.dir"), filePath);

        // HTML 파일 읽기
        String htmlContent = Files.readString(htmlFilePath, StandardCharsets.UTF_8);

        // 정규식 특수 문자 이스케이프
        String escapedOriginalElement = Pattern.quote(domElement);

        // 요소 교체
        String updatedHtmlContent = htmlContent.replaceAll(escapedOriginalElement, Matcher.quoteReplacement(gptGeneratedElement));

        // 파일 덮어쓰기
        Files.writeString(htmlFilePath, updatedHtmlContent, StandardCharsets.UTF_8);

        // Dashboard 상태 업데이트
        dashboardEntity.updateModified(true);
        dashboardRepository.save(dashboardEntity);
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
