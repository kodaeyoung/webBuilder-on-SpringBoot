package com.project.webBuilder.dashboards.repository;

import com.project.webBuilder.dashboards.entities.DashboardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DashboardRepository extends JpaRepository<DashboardEntity,Long> {
    List<DashboardEntity> findAllByEmail(String email);

    DashboardEntity findByProjectPath(String projectRelativePath);
}
