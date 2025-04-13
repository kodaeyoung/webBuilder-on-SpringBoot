package com.project.webBuilder.dashboards.service;

import com.project.webBuilder.dir.service.DirectoryService;
import com.project.webBuilder.dashboards.dto.DashboardDTO;
import com.project.webBuilder.dashboards.entities.DashboardEntity;
import com.project.webBuilder.dashboards.repository.DashboardRepository;
import com.project.webBuilder.sharedTemplates.dto.SharedTemplateDTO;
import com.project.webBuilder.sharedTemplates.entities.SharedTemplateEntity;
import com.project.webBuilder.sharedTemplates.repository.SharedTemplateRepository;
import com.project.webBuilder.user.dto.UserDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class DashboardService {
    private final DashboardRepository dashboardRepository;
    private final SharedTemplateRepository sharedTemplateRepository;

    //사용자 email별 대시보드 리턴
    public List<DashboardDTO> getUserDashboard(String email) {
        return dashboardRepository.findAllByEmail(email)
                .stream()
                .map(DashboardDTO::fromEntity) // Entity → DTO 변환
                .collect(Collectors.toList());
    }

    //대시보드 이름 수정
    public boolean updateProjectName(Long id, String newName) {
        return dashboardRepository.findById(id).map(dashboardEntity -> {
            dashboardEntity.updateProjectName(newName);
            dashboardRepository.save(dashboardEntity);
            return true;
        }).orElse(false);
    }

    //대시보드 공유
    public boolean projectShare(Long id, String templateName, String category, UserDTO userDTO) throws IOException {

        Optional<DashboardEntity> optionalDashboardEntity = dashboardRepository.findById(id);

        if (optionalDashboardEntity.isPresent()) {
            DashboardEntity dashboardEntity = optionalDashboardEntity.get();

            Path rootDirPath = Paths.get(System.getProperty("user.dir")); // 애플리케이션의 루트 디렉터리

            // 선택된 대시보드의 절대 경로
            Path selectProjectPathAbsolute = rootDirPath.resolve(dashboardEntity.getProjectPath());

            // 새로운 디렉토리 생성
            String newDirName = templateName + "_" + userDTO.getEmail() + "_" + System.currentTimeMillis();
            Path newSharedTemplateAbsolutePath = rootDirPath.resolve("store/sharedTemplate").resolve(newDirName); //절대 경로

            // 디렉토리가 존재하지 않으면 생성
            if (Files.notExists(newSharedTemplateAbsolutePath)) {
                Files.createDirectories(newSharedTemplateAbsolutePath);
            }

            // 템플릿 파일 복사
            DirectoryService.copyDirectory(selectProjectPathAbsolute, newSharedTemplateAbsolutePath);


            // 선택된 대시보드 이미지의 절대경로
            Path selectProjectImagePathAbsolute = rootDirPath.resolve(dashboardEntity.getImagePath());
            // 새로운 이미지를 저장할 경로
            Path newImagePath = rootDirPath.resolve("store/sharedTemplateImage").resolve(newDirName + ".png"); //절대경로

            // 이미지 파일 복사
            Files.copy(selectProjectImagePathAbsolute, newImagePath, StandardCopyOption.REPLACE_EXISTING);

            // 상대 경로로 변환

            Path newSharedTemplateRelativePath = rootDirPath.relativize(newSharedTemplateAbsolutePath);
            Path newSharedImageRelativePath = rootDirPath.relativize(newImagePath);


            // dashboard 테이블에 새 데이터 저장
            SharedTemplateEntity newSharedTemplate = SharedTemplateEntity.builder()
                    .templateName((templateName != null) ? templateName : "default")
                    .templatePath(newSharedTemplateRelativePath.toString().replace("\\", "/"))
                    .imagePath(newSharedImageRelativePath.toString().replace("\\", "/"))
                    .category(category)
                    .userDTO(userDTO)
                    .build();

            sharedTemplateRepository.save(newSharedTemplate);

            return true;
        } else {
            throw new IllegalArgumentException("Template not found");
        }
    }


    //대시보드 삭제
    @Transactional
    public boolean removeProject(Long id) {
        Optional<DashboardEntity> optionalDashboardEntity = dashboardRepository.findById(id);
        if (optionalDashboardEntity.isPresent()) {
            DashboardEntity dashboardEntity = optionalDashboardEntity.get();

            Path rootDirPath = Paths.get(System.getProperty("user.dir"));
            Path projectAbsolutePath = rootDirPath.resolve(dashboardEntity.getProjectPath());
            Path imageAbsolutePath = rootDirPath.resolve(dashboardEntity.getImagePath());

            try {
                DirectoryService.deleteDirectory(projectAbsolutePath);
                DirectoryService.deleteDirectory(imageAbsolutePath);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            dashboardRepository.delete(dashboardEntity);
            return true;
        }
        return false;
    }
}
