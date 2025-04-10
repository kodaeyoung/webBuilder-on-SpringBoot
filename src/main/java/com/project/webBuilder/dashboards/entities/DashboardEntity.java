package com.project.webBuilder.dashboards.entities;


import com.project.webBuilder.common.entities.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
@Entity
@Table(name="dashboards")
public class DashboardEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 기본키

    @Column(nullable = false)
    private String projectName;

    @Column(nullable = false,unique = true)
    private String projectPath;

    @Column
    private Boolean modified;

    @Column
    private String imagePath;

    @Column
    private String email;

    @Column
    private String deployDomain;

    @Column
    private Boolean publish;


    @Builder
    public DashboardEntity(String projectName, String projectPath, Boolean modified, String imagePath,
                           String email, String deployDomain, Boolean publish) {
        this.projectName = projectName;
        this.projectPath = projectPath;
        this.modified = modified;
        this.imagePath = imagePath;
        this.email = email;
        this.deployDomain = deployDomain;
        this.publish = publish;
    }

    public DashboardEntity updateDeployDomain(String deployDomain){
        this.deployDomain=deployDomain;
        return this;
    }

    public DashboardEntity updateProjectName(String projectName){
        this.projectName=projectName;
        return this;
    }

    public DashboardEntity updateModified(Boolean bool){
        this.modified = bool;
        return this;
    }

    public DashboardEntity updatePublish(Boolean bool){
        this.publish = bool;
        return this;
    }

    public DashboardEntity updateImagePath(String imagePath){
        this.imagePath=imagePath;
        return this;
    }
}
