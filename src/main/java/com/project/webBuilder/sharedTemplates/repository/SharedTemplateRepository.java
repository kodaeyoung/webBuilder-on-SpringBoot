package com.project.webBuilder.sharedTemplates.repository;

import com.project.webBuilder.sharedTemplates.entities.SharedTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SharedTemplateRepository extends JpaRepository<SharedTemplateEntity,Long> {
}
