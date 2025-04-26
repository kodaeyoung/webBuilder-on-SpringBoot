package com.project.webBuilder.deploy.service;

import com.project.webBuilder.dir.service.DirectoryService;
import com.project.webBuilder.dashboards.entities.DashboardEntity;
import com.project.webBuilder.dashboards.repository.DashboardRepository;
import com.project.webBuilder.deploy.entities.DeployEntity;
import com.project.webBuilder.deploy.repository.DeployRepository;
import com.project.webBuilder.global.exeption.custom.CustomException;
import com.project.webBuilder.global.exeption.errorcode.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class DeployService {

    private final DeployRepository deployRepository;
    private final DashboardRepository dashboardRepository;

    @Transactional
    public void deployProject(Long id, String deployName) throws IOException {
        // 1. Dashboard 조회
        DashboardEntity dashboardEntity = dashboardRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND,"Dashboard with id " + id + " not found."));

        // 2. 이미 배포된 경우 예외
        if (Boolean.TRUE.equals(dashboardEntity.getPublish())) {
            throw new CustomException(ErrorCode.INVALID_REQUEST,"Already in deployment.");
        }

        // 3. 중복되는 deployName 확인
        if (deployRepository.existsByDeployName(deployName)) {
            throw new CustomException(ErrorCode.INVALID_REQUEST,"Deploy name " + deployName + " already exists.");
        }


        Path rootDirPath = Paths.get(System.getProperty("user.dir"));  //루트경로
        // 선택된 대시보드의 상대 경로
        String projectRelativePath = dashboardEntity.getProjectPath();
        // 선택된 대시보드의 절대 경로
        Path projectAbsolutePath = rootDirPath.resolve(projectRelativePath);

        //새로운 배포파일 경로
        Path newDeployPath = rootDirPath.resolve("store/deploy").resolve(deployName); //절대 경로

        // 디렉토리가 존재하지 않으면 생성
        if (Files.notExists(newDeployPath)) {
            Files.createDirectories(newDeployPath);
        }

        DirectoryService.copyDirectory(projectAbsolutePath, newDeployPath);

        //DB에 저장하기 위해서 상대경로로 변환
        Path newDeployRelativePath=rootDirPath.relativize(newDeployPath);

        DeployEntity deployEntity = DeployEntity.builder()
                .originalProjectPath(projectRelativePath)
                .deployName(deployName)
                .deployPath(newDeployRelativePath.toString().replace("\\", "/"))
                .build();
        deployRepository.save(deployEntity);

        dashboardEntity.updateModified(false);
        dashboardEntity.updatePublish(true);
        dashboardEntity.updateDeployDomain(newDeployRelativePath.toString().replace("\\", "/"));
        dashboardRepository.save(dashboardEntity);
    }

    @Transactional
    public void undeployProject(Long id) throws IOException {
        // 1. Dashboard 조회
        DashboardEntity dashboardEntity = dashboardRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND,"Dashboard with id " + id + " not found."));

        // 2. 배포 여부 확인
        if (!Boolean.TRUE.equals(dashboardEntity.getPublish())) {
            throw new CustomException(ErrorCode.INVALID_REQUEST,"This dashboard is not currently deployed.");
        }

        String projectRelativePath = dashboardEntity.getProjectPath();
        // deploy 엔티티 조회
        DeployEntity deployEntity= deployRepository.findByOriginalProjectPath(projectRelativePath)
                .orElseThrow(() -> new CustomException(ErrorCode.DEPLOY_NOT_FOUND,"Can not find deployEntity with original project path"));


        // 실제 배포 디렉터리 삭제
        Path rootDirPath = Paths.get(System.getProperty("user.dir"));
        Path deployAbsolutePath = rootDirPath.resolve(dashboardEntity.getDeployDomain());

        try {
            if (Files.exists(deployAbsolutePath)) {
                DirectoryService.deleteDirectory(deployAbsolutePath);
            }
        } catch (IOException e) {
            throw new IOException("Failed to delete deployed files.", e);
        }

        // deploy 엔티티 삭제
        deployRepository.delete(deployEntity);

        // dashboard 엔티티 수정

        dashboardEntity.updateDeployDomain(null);
        dashboardEntity.updatePublish(false);
        dashboardEntity.updateModified(false);

        dashboardRepository.save(dashboardEntity);
    }

    @Transactional
    public void updateDeploy(Long id) throws IOException {
        // 1. Dashboard 조회
        DashboardEntity dashboardEntity = dashboardRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND,"Dashboard with id " + id + " not found."));

        // 2. Deploy 엔티티 조회 (projectPath 기준)
        String projectRelativePath = dashboardEntity.getProjectPath();
        DeployEntity deployEntity = deployRepository.findByOriginalProjectPath(projectRelativePath)
                .orElseThrow(() -> new CustomException(ErrorCode.DEPLOY_NOT_FOUND,"Deploy not found for project path: " + projectRelativePath));


        // 3. 절대 경로 생성
        Path rootDirPath = Paths.get(System.getProperty("user.dir"));
        Path projectAbsolutePath = rootDirPath.resolve(projectRelativePath);
        Path deployAbsolutePath = rootDirPath.resolve(deployEntity.getDeployPath());

        // 4. 경로 유효성 확인
        if (!Files.exists(projectAbsolutePath)) {
            throw new RuntimeException("Template path does not exist: " + projectAbsolutePath);
        }

        if (!Files.exists(deployAbsolutePath)) {
            throw new RuntimeException("Deployment path does not exist: " + deployAbsolutePath);
        }

        // 5. 변경 여부 비교
        boolean isSame = DirectoryService.isSameDirectory(projectAbsolutePath, deployAbsolutePath);
        if (isSame) {
            throw new RuntimeException("No changes detected. Deployment not needed.");
        }

        // 6. 덮어쓰기
        DirectoryService.copyDirectory(projectAbsolutePath, deployAbsolutePath);

        // 7. Dashboard 상태 업데이트
        dashboardEntity.updateModified(false);
        dashboardRepository.save(dashboardEntity);
    }
}
