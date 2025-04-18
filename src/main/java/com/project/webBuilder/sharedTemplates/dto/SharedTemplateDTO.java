package com.project.webBuilder.sharedTemplates.dto;

import com.project.webBuilder.sharedTemplates.entities.SharedTemplateEntity;
import com.project.webBuilder.user.dto.UserDTO;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class SharedTemplateDTO {
    private Long id;
    private String templateName;
    private String userName;
    private String email;
    private String profileImageUrl;
    private String category;
    private String templatePath;
    private String imagePath;
    private LocalDateTime createdAt;

    @Builder
    public SharedTemplateDTO(Long id, String templateName, String userName,
                             String email, String profileImageUrl, String category, String templatePath,
                             String imagePath, LocalDateTime createdAt) {
        this.id = id;
        this.templateName = templateName;
        this.userName = userName;
        this.email = email;
        this.profileImageUrl = profileImageUrl;
        this.category = category;
        this.templatePath = templatePath;
        this.imagePath = imagePath;
        this.createdAt = createdAt;
    }

    public static SharedTemplateDTO fromEntity(SharedTemplateEntity sharedTemplatesEntity, UserDTO userDTO) {
        return new SharedTemplateDTO(
                sharedTemplatesEntity.getId(),
                sharedTemplatesEntity.getTemplateName(),
                userDTO.getName(),
                userDTO.getEmail(), // UserEntity에서 email을 가져옴
                userDTO.getPicture(), // UserEntity에서 profileImageUrl을 가져옴
                sharedTemplatesEntity.getCategory(),
                sharedTemplatesEntity.getTemplatePath(),
                sharedTemplatesEntity.getImagePath(),
                sharedTemplatesEntity.getCreatedAt()
        );
    }


}
