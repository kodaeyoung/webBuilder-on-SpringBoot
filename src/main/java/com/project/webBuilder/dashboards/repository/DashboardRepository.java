package com.project.webBuilder.dashboards.repository;

import com.project.webBuilder.dashboards.entities.DashboardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DashboardRepository extends JpaRepository<DashboardEntity,Long> {
}
