package com.project.webBuilder.deploy.entities;

import com.project.webBuilder.common.entities.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.web.header.writers.StaticHeadersWriter;

import java.lang.reflect.Type;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name="deploy")
public class DeployEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,unique = true)
    private String originalProjectPath;

    @Column(nullable = false,unique = true)
    private String deployPath;

    @Column(unique = true)
    private String deployName;

    @Builder
    public DeployEntity(String originalProjectPath, String deployPath, String deployName){
        this.originalProjectPath=originalProjectPath;
        this.deployName=deployName;
        this.deployPath=deployPath;
    }
}
