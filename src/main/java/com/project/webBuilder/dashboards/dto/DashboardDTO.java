package com.project.webBuilder.dashboards.dto;

import com.project.webBuilder.dashboards.entities.DashboardEntity;
import lombok.Getter;

@Getter
public class DashboardDTO {

    private Long id;
    private String projectName;
    private String projectPath;
    private Boolean modified;
    private String imagePath;
    private Boolean shared;
    private String email;
    private String deployDomain;
    private Boolean publish;

    public DashboardDTO(Long id, String projectName, String projectPath, Boolean modified,
                        String imagePath, Boolean shared, String email, String deployDomain,
                        Boolean publish) {
        this.id = id;
        this.projectName = projectName;
        this.projectPath = projectPath;
        this.modified = modified;
        this.imagePath = imagePath;
        this.shared = shared;
        this.email = email;
        this.deployDomain = deployDomain;
        this.publish = publish;
    }

    public static DashboardDTO fromEntity(DashboardEntity dashboardEntity) {
        return new DashboardDTO(
                dashboardEntity.getId(),
                dashboardEntity.getProjectName(),
                dashboardEntity.getProjectPath(),
                dashboardEntity.getModified(),
                dashboardEntity.getImagePath(),
                dashboardEntity.getShared(),
                dashboardEntity.getEmail(),
                dashboardEntity.getDeployDomain(),
                dashboardEntity.getPublish()
        );
    }
}
