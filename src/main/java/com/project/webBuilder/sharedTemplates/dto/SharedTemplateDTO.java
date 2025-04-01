package com.project.webBuilder.sharedTemplates.dto;

import com.project.webBuilder.sharedTemplates.entities.SharedTemplateEntity;
import com.project.webBuilder.user.dto.UserDTO;
import lombok.Builder;
import lombok.Getter;

@Getter
public class SharedTemplateDTO {
    private Long id;
    private Long dashboardKey;
    private String templateName;
    private String userName;
    private String email;
    private String profileImageUrl;
    private String category;
    private String templatePath;
    private String imagePath;
    private String description;


    @Builder
    public SharedTemplateDTO(Long id, Long dashboardKey, String templateName, String userName,
                             String email, String profileImageUrl, String category, String templatePath,
                             String imagePath, String description) {
        this.id = id;
        this.dashboardKey = dashboardKey;
        this.templateName = templateName;
        this.userName = userName;
        this.email = email;
        this.profileImageUrl = profileImageUrl;
        this.category = category;
        this.templatePath = templatePath;
        this.imagePath = imagePath;
        this.description = description;
    }

    public static SharedTemplateDTO fromEntity(SharedTemplateEntity sharedTemplatesEntity, UserDTO userDTO) {
        return new SharedTemplateDTO(
                sharedTemplatesEntity.getId(),
                sharedTemplatesEntity.getDashboardKey(), // DashboardEntity에서 dashboardKey를 가져옴
                sharedTemplatesEntity.getTemplateName(),
                userDTO.getName(),
                userDTO.getEmail(), // UserEntity에서 email을 가져옴
                userDTO.getPicture(), // UserEntity에서 profileImageUrl을 가져옴
                sharedTemplatesEntity.getCategory(),
                sharedTemplatesEntity.getTemplatePath(),
                sharedTemplatesEntity.getImagePath(),
                sharedTemplatesEntity.getDescription()
        );
    }


}
