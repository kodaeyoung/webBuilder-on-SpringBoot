package com.project.webBuilder.sharedTemplates.entities;


import com.project.webBuilder.dashboards.entities.DashboardEntity;
import com.project.webBuilder.entities.BaseTimeEntity;

import com.project.webBuilder.user.dto.UserDTO;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name="sharedtemplates")
public class SharedTemplateEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; //기본키

    @Column(nullable = false)
    private String templateName; //템플릿 제목

    @Column(nullable = false)
    private String email;   //공유한 사람의 email로, User테이블의 참조키

    @Column(nullable = false)
    private String category; //카테고리 별 필터를 위한 속성

    @Column(nullable = false, unique = true)
    private String templatePath; //해당 템플릿의 디렉터리가 포함된 상대 경로를 저장

    @Column(nullable = false, unique = true)
    private String imagePath; // 미리보기를 위한 해당 템플릿의 이미지가 저장된 상대 경로 저장

    @Builder
    public SharedTemplateEntity(String templateName, UserDTO userDTO,
                                String category, String templatePath, String imagePath){
        this.templateName = templateName;
        this.email = userDTO.getEmail();
        this.category = category;
        this.templatePath = templatePath;
        this.imagePath = imagePath;
    }
}
