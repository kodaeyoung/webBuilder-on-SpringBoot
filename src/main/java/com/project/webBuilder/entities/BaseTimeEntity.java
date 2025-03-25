package com.project.webBuilder.entities;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.cglib.core.Local;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass // 이 추상 클래스를 상속하는 Entity 클래스에서 아래 필드를 자동으로 Column으로 등록
@EntityListeners(AuditingEntityListener.class) //JPA Auditing 기능을 활성화하여, 엔티티의 생성 시간과 수정 시간을 자동으로 관리
public abstract class BaseTimeEntity {
    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime modifiedAt;
}
