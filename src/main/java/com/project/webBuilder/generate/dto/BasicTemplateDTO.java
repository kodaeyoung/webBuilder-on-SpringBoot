package com.project.webBuilder.generate.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class BasicTemplateDTO {
    private Long id;
    private String websiteType;
    private String mood;

    @Builder
    public BasicTemplateDTO(Long id, String websiteType, String mood) {
        this.id = id;
        this.websiteType = websiteType;
        this.mood = mood;
    }
}
