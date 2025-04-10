package com.project.webBuilder.generate.repository;

import com.project.webBuilder.generate.dto.BasicTemplateDTO;
import com.project.webBuilder.generate.entities.BasicTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BasicTemplateRepository extends JpaRepository<BasicTemplateEntity,Long> {
    @Query("SELECT new com.project.webBuilder.generate.dto.BasicTemplateDTO(t.id, t.websiteType, t.feature, t.mood) " +
            "FROM BasicTemplateEntity t")
    List<BasicTemplateDTO> findAllBasicTemplates();
}
