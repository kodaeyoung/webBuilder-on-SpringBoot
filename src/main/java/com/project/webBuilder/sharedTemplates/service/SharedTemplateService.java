package com.project.webBuilder.sharedTemplates.service;


import com.project.webBuilder.dir.service.DirectoryService;
import com.project.webBuilder.dashboards.dto.DashboardDTO;
import com.project.webBuilder.dashboards.entities.DashboardEntity;
import com.project.webBuilder.dashboards.repository.DashboardRepository;
import com.project.webBuilder.global.exeption.custom.CustomException;
import com.project.webBuilder.global.exeption.errorcode.ErrorCode;
import com.project.webBuilder.sharedTemplates.dto.SharedTemplateDTO;
import com.project.webBuilder.sharedTemplates.entities.SharedTemplateEntity;
import com.project.webBuilder.sharedTemplates.repository.SharedTemplateRepository;
import com.project.webBuilder.user.dto.UserDTO;
import com.project.webBuilder.user.entities.UserEntity;
import com.project.webBuilder.user.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.User;
import org.springframework.stereotype.Service;

import javax.swing.text.html.parser.Entity;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SharedTemplateService {
    private final SharedTemplateRepository sharedTemplateRepository;
    private final UserRepository userRepository;
    private final DashboardRepository dashboardRepository;

    public List<SharedTemplateDTO> getAllSharedTemplateDTO() {
        return sharedTemplateRepository.findAll().stream()
                .map(sharedTemplateEntity -> {
                    UserDTO userDTO = userRepository.findByEmail(sharedTemplateEntity.getEmail())
                            .map(UserDTO::fromEntity) // UserEntity -> UserDTO 변환
                            .orElse(new UserDTO("Anonymous", "Anonymous", "")); // 기본값 설정

                    return SharedTemplateDTO.fromEntity(sharedTemplateEntity, userDTO);
                })
                .collect(Collectors.toList()); // List<SharedTemplateDTO> 반환
    }


    @Transactional
    public DashboardDTO useSharedTemplate(Long id, String projectName, String email) throws IOException {
        Optional<SharedTemplateEntity> optionalSharedTemplateEntity = sharedTemplateRepository.findById(id);

        if (optionalSharedTemplateEntity.isEmpty()) {
            throw new CustomException(ErrorCode.TEMPLATE_NOT_FOUND);
        }

        SharedTemplateEntity sharedTemplateEntity = optionalSharedTemplateEntity.get();

        if (projectName == null || projectName.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "프로젝트 이름이 비어 있습니다.");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "이메일이 비어 있습니다.");
        }

        Path rootDirPath = Paths.get(System.getProperty("user.dir")); // 애플리케이션의 루트 디렉터리

        // 선택된 템플릿의 절대 경로
        Path selectTemplateAbsolutePath =rootDirPath.resolve(sharedTemplateEntity.getTemplatePath());

        // 새로운 디렉토리 생성
        String newDirName = projectName + "_" + email + "_" + System.currentTimeMillis();
        Path newProjectPath = rootDirPath.resolve("store/dashboard").resolve(newDirName); //절대 경로

        try {
            // 디렉토리가 존재하지 않으면 생성
            if (Files.notExists(newProjectPath)) {
                Files.createDirectories(newProjectPath);
            }
            // 템플릿 파일 복사
            DirectoryService.copyDirectory(selectTemplateAbsolutePath, newProjectPath);
        } catch (IOException e) {
            throw new IOException("템플릿 파일 복사 중 오류 발생",e);
        }


        // 선택된 템플릿 이미지의 절대경로
        Path selectTemplateImageAbsolutePath = rootDirPath.resolve(sharedTemplateEntity.getImagePath()); //절대 경로
        // 새로운 이미지를 저장할 경로
        Path newImagePath = rootDirPath.resolve("store/dashboardImage").resolve(newDirName+ ".png"); //절대경로

        try {
            // 이미지 파일 복사
            Files.copy(selectTemplateImageAbsolutePath, newImagePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IOException("이미지 파일 복사 중 오류 발생",e);
        }

        // 상대 경로로 변환

        Path newProjectRelativePath = rootDirPath.relativize(newProjectPath);
        Path newImageRelativePath = rootDirPath.relativize(newImagePath);


        // dashboard 테이블에 새 데이터 저장
        try {
            DashboardEntity newDashboard = DashboardEntity.builder()
                    .projectName((projectName != null) ? projectName : "default")
                    .projectPath(newProjectRelativePath.toString().replace("\\", "/"))
                    .imagePath(newImageRelativePath.toString().replace("\\", "/"))
                    .modified(false)
                    .email(email)
                    .publish(false)
                    .build();

            DashboardEntity savedDashboard = dashboardRepository.save(newDashboard);
            return DashboardDTO.fromEntity(newDashboard);
        } catch (Exception e) {
            throw new RuntimeException("데이터베이스 저장 오류", e);
        }
    }

    //로그인한 사용자가 공유한 템플릿 호출
    public List<SharedTemplateDTO> getMySharedTemplates(String email) {
        return sharedTemplateRepository.findAllByEmail(email)
                .stream()
                .map(sharedTemplateEntity -> {
                    UserDTO userDTO = userRepository.findByEmail(sharedTemplateEntity.getEmail())
                            .map(UserDTO::fromEntity)
                            .orElse(new UserDTO("Anonymous", "Anonymous", ""));
                    return SharedTemplateDTO.fromEntity(sharedTemplateEntity,userDTO);
                })
                .collect(Collectors.toList());
    }

    // 공유된 템플릿 삭제
    public void removeSharedTemplate(Long id) throws IOException {

        SharedTemplateEntity sharedTemplateEntity = sharedTemplateRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.TEMPLATE_NOT_FOUND, "공유된 템플릿을 찾을 수 없습니다."));

        Path rootDirPath = Paths.get(System.getProperty("user.dir"));
        Path sharedTemplateAbsolutePath = rootDirPath.resolve(sharedTemplateEntity.getTemplatePath());
        Path imageAbsolutePath = rootDirPath.resolve(sharedTemplateEntity.getImagePath());

        try {
            DirectoryService.deleteDirectory(sharedTemplateAbsolutePath);
            DirectoryService.deleteDirectory(imageAbsolutePath);
        } catch (IOException e) {
            throw new IOException("템플릿 파일 삭제 중 오류 발생", e);
        }
        sharedTemplateRepository.delete(sharedTemplateEntity);
    }
}
