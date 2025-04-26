package com.project.webBuilder.generate.service;

import com.project.webBuilder.global.gpt.GptApi;
import com.project.webBuilder.global.util.Extraction;
import com.project.webBuilder.global.util.Screenshot;
import com.project.webBuilder.dir.service.DirectoryService;
import com.project.webBuilder.dashboards.entities.DashboardEntity;
import com.project.webBuilder.dashboards.repository.DashboardRepository;
import com.project.webBuilder.generate.dto.BasicTemplateDTO;
import com.project.webBuilder.generate.entities.BasicTemplateEntity;
import com.project.webBuilder.generate.repository.BasicTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class GenerateService {



    private final BasicTemplateRepository basicTemplateRepository;
    private final DashboardRepository dashboardRepository;



    public boolean generate(Map<String,Object> body, String email) throws IOException {

        //모든 basicTemplateDTO 호출
        List<BasicTemplateDTO> basicTemplateDTOS =getAllBasicTemplate();

        //적절한 basicTemplate선택
        String websiteType = (String) body.get("websiteType");
        String mood = (String)body.get("mood");
        Long id = selectTemplate(websiteType,mood,basicTemplateDTOS);

        //해당 basicTemplate 복사하여 프로젝트 생성
        String projectName = (String)body.get("projectName");
        String projectRelativePath = copy(email,projectName,id);

        //복사된 프로젝트를 요구사항에 맞게 수정
        String features = (String)body.get("features");
        String content =(String) body.get("content");
        modify(projectRelativePath,features,content);

        return true;
    }

    //모든 basicTemplateDTO 호출
    private List<BasicTemplateDTO> getAllBasicTemplate(){
        return basicTemplateRepository.findAllBasicTemplates();
    }

    //적절한 basicTemplate선택
    private Long selectTemplate(String websiteType, String mood, List<BasicTemplateDTO> basicTemplateDTOS) {

        StringBuilder userContent = new StringBuilder();
        userContent.append("The user provided the following requirements for a website:\n")
                .append("- Type of website: ").append(websiteType).append("\n")
                .append("- Mood: ").append(mood).append("\n\n")
                .append("Based on these requirements, choose the most suitable template from the following list:\n");

        for (int i = 0; i < basicTemplateDTOS.size(); i++) {
            BasicTemplateDTO b = basicTemplateDTOS.get(i);
            userContent.append("Template ").append(b.getId()).append(": ")
                    .append(b.getWebsiteType()).append(" - ")
                    .append(b.getMood()).append("\n");
        }

        userContent.append("\nIf there is a suitable template, respond with \"template:number\".\n")
                .append("If no appropriate template is found, choose the closest match and respond with \"template:number\".");

        String content = GptApi.gpt("You are a helpful assistant.",userContent.toString(),150);

        return Extraction.extractTemplateId(content.trim());
    }

    //해당 basicTemplate 복사하여 프로젝트 생성
    private String copy(String email, String projectName, Long id) throws IOException {
        BasicTemplateEntity basicTemplateEntity = basicTemplateRepository.findById(id)
                .orElseThrow(()->new RuntimeException("BasicTemplate with id"+id+"not found."));

        Path rootDirPath = Paths.get(System.getProperty("user.dir"));

        //선택된 basicTemplate의 절대경로
        Path selectBasicTemplateAsolutePath = rootDirPath.resolve(basicTemplateEntity.getPath());

        //사용자의 새 프로젝트 생성
        String newDirName = projectName + "_" +email + "_" + System.currentTimeMillis();
        Path newProjectPath = rootDirPath.resolve("store/dashboard").resolve(newDirName);

        if(Files.notExists(newProjectPath)){
            Files.createDirectories(newProjectPath);
        }

        // 템플릿 파일 복사
        DirectoryService.copyDirectory(selectBasicTemplateAsolutePath, newProjectPath);

        //상대 경로 변환
        Path newProjectRelativePath = rootDirPath.relativize(newProjectPath);

        DashboardEntity newDashboard = DashboardEntity.builder()
                .projectName((projectName!=null)?projectName:"default")
                .projectPath(newProjectRelativePath.toString().replace("\\", "/"))
                .modified(false)
                .email(email)
                .publish(false)
                .build();
        dashboardRepository.save(newDashboard);

        return newProjectRelativePath.toString();
    }

    //복사된 프로젝트를 요구사항에 맞게 수정
    private void modify(String projectRelativePath, String features, String content){

        DashboardEntity dashboardEntity = dashboardRepository.findByProjectPath(projectRelativePath)
                .orElseThrow(()->new RuntimeException("Dashboard with " + projectRelativePath + " not found."));

        //프로젝트 절대경로 변경
        Path rootDirPath = Paths.get(System.getProperty("user.dir"));
        Path projectAbsolutePath = rootDirPath.resolve(projectRelativePath);

        //프로젝트 디렉터리의 html 파일 배열에 추가
        File dir = new File(projectAbsolutePath.toString());
        File[] htmlFiles = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".html"));

        //html 파일이 없으면 예외처리
        if (htmlFiles == null || htmlFiles.length == 0) {
            throw new RuntimeException("No HTML files found in project path: " + projectAbsolutePath);
        }

        //하나 씩 순회하면서 처리
        for (File htmlFile : htmlFiles) {
            try {
                String html = Files.readString(htmlFile.toPath());
                String gptResult = GptApi.gpt("You are a helpful assistant.",
                        String.format("Modify the HTML to match the following features and content:\nFeatures: %s\nContent: %s\n\n%s",
                        features, content, html),4000);
                String modifiedHtml = Extraction.extractValidHtml(gptResult);

                if (modifiedHtml != null) {
                    Files.writeString(htmlFile.toPath(), modifiedHtml, StandardCharsets.UTF_8);
                    if (htmlFile.getName().equalsIgnoreCase("index.html")) {
                        String screenshotName = dashboardEntity.getProjectName()+"_"+ dashboardEntity.getEmail()+"_"+System.currentTimeMillis()+".png";
                        Path newImageAbsolutePath = rootDirPath.resolve("store/dashboardImage").resolve(screenshotName);
                        // Optional: 캡쳐 로직 삽입 가능
                        String url = String.format("http://localhost:8080/%s/index.html",dashboardEntity.getProjectPath());
                        Screenshot.takeScreenshot(url, String.valueOf(newImageAbsolutePath));
                        System.out.println("index.html 수정됨 - 스크린샷 로직 실행 위치");

                        Path newImageRelatvePath = rootDirPath.relativize(newImageAbsolutePath);
                        dashboardEntity.updateImagePath(newImageRelatvePath.toString().replace("\\", "/"));
                        dashboardRepository.save(dashboardEntity);
                    }
                } else {
                    System.out.println("GPT 응답에 유효한 HTML이 없습니다: " + htmlFile.getName());
                }

            } catch (IOException e) {
                throw new RuntimeException("파일 처리 중 오류 발생: " + htmlFile.getName(), e);
            }
        }
    }


    /* <------------------------------- util 메서드 ------------------------------------->*/


}
