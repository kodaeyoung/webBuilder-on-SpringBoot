package com.project.webBuilder.sharedTemplates.service;

import com.project.webBuilder.dashboards.dto.DashboardDTO;
import com.project.webBuilder.dashboards.entities.DashboardEntity;
import com.project.webBuilder.dashboards.repository.DashboardRepository;
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

@Service
@RequiredArgsConstructor
public class SharedTemplateService {
    private final SharedTemplateRepository sharedTemplateRepository;
    private final UserRepository userRepository;
    private final DashboardRepository dashboardRepository;

    public List<SharedTemplateDTO> getAllSharedTemplateDTO(){
        List<SharedTemplateEntity> sharedTemplateEntities = sharedTemplateRepository.findAll();
        List<SharedTemplateDTO> sharedTemplateDTOS = new ArrayList<>();
        for(SharedTemplateEntity sharedTemplateEntity : sharedTemplateEntities){
            Optional<UserEntity> optionalUser = userRepository.findByEmail(sharedTemplateEntity.getEmail());
            if(optionalUser.isPresent()){
                UserEntity userEntity =optionalUser.get();

                // SharedTemplateDTO와 UserDTO를 변환하여 리스트에 추가
                SharedTemplateDTO sharedTemplateDTO = SharedTemplateDTO.fromEntity(sharedTemplateEntity, UserDTO.fromEntity(userEntity));
                sharedTemplateDTOS.add(sharedTemplateDTO);
            }else{
                SharedTemplateDTO sharedTemplateDTO = SharedTemplateDTO.fromEntity(sharedTemplateEntity,new UserDTO("Anonymous","Anonymous",""));
                sharedTemplateDTOS.add(sharedTemplateDTO);
            }
        }
        return sharedTemplateDTOS;
    }


    @Transactional
    public DashboardDTO useSharedTemplate(Long id, String projectName, String email) throws IOException {
        Optional<SharedTemplateEntity> optionalSharedTemplateEntity = sharedTemplateRepository.findById(id);

        if (optionalSharedTemplateEntity.isPresent()) {
            SharedTemplateEntity sharedTemplateEntity = optionalSharedTemplateEntity.get();

            Path rootDirPath = Paths.get(System.getProperty("user.dir")); // 애플리케이션의 루트 디렉터리

            System.out.println(rootDirPath.toString());
            // 선택된 템플릿의 상대 경로
            Path selectTemplatePath = Paths.get(sharedTemplateEntity.getTemplatePath());
            System.out.println(selectTemplatePath.toString());
            // 선택된 템플릿 절대 경로
            Path selectTemplatePathAbsolute =rootDirPath.resolve(selectTemplatePath);

            // 새로운 디렉토리 이름 생성
            String newDirName = projectName + "_" + email + "_" + System.currentTimeMillis();

            //절대 경로
            Path newProjectPath = rootDirPath.resolve("store/dashboard").resolve(newDirName);
            System.out.println(newProjectPath.toString());
            // 디렉토리가 존재하지 않으면 생성
            if (Files.notExists(newProjectPath)) {
                Files.createDirectories(newProjectPath);
            }

            // 템플릿 파일 복사
            copyDirectory(selectTemplatePathAbsolute, newProjectPath);

            // 이미지 경로 설정
            Path selectTemplateImagePath = Paths.get(sharedTemplateEntity.getImagePath()); //상대 경로

            Path selectTemplateImagePathAbsolute = rootDirPath.resolve(selectTemplateImagePath); //절대 경로

            Path newImagePath = rootDirPath.resolve("store/dashboardImage").resolve(newDirName+ ".png"); //절대경로

            // 이미지 파일 복사
            Files.copy(selectTemplateImagePathAbsolute, newImagePath, StandardCopyOption.REPLACE_EXISTING);

            // 상대 경로로 변환

            Path relativeNewProjectPath = rootDirPath.relativize(newProjectPath);
            Path relativeNewImagePath = rootDirPath.relativize(newImagePath);

            System.out.println("pro:"+projectName);

            // dashboard 테이블에 새 데이터 저장
            DashboardEntity newDashboard = DashboardEntity.builder()
                                            .projectName((projectName!=null)?projectName:"default")
                                            .projectPath(relativeNewProjectPath.toString())
                                            .imagePath(relativeNewImagePath.toString())
                                            .modified(false)
                                            .shared(false)
                                            .email(email)
                                            .publish(false)
                                            .build();
            DashboardEntity savedDashboard=dashboardRepository.save(newDashboard);

            System.out.println("Saved Project Name: " + savedDashboard.getProjectName());
            return DashboardDTO.fromEntity(newDashboard);
        } else {
            throw new IllegalArgumentException("Template not found");
        }
    }

    private void copyDirectory(Path source, Path target) throws IOException {
        // 파일 복사를 위해 Files.walkFileTree 사용
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path destination = target.resolve(source.relativize(file));
                Files.copy(file, destination, StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path destination = target.resolve(source.relativize(dir));
                if (Files.notExists(destination)) {
                    Files.createDirectories(destination);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
