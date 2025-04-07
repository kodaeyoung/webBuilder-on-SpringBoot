package com.project.webBuilder.deploy.repository;

import com.project.webBuilder.deploy.entities.DeployEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeployRepository extends JpaRepository<DeployEntity,Long> {
    boolean existsByDeployName(String deployName);
    Optional<DeployEntity> findByOriginalProjectPath(String originalProjectPath);
}
