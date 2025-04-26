package com.project.webBuilder.user.entities;

import com.project.webBuilder.global.entities.BaseTimeEntity;
import com.project.webBuilder.user.enums.Role;
import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // JPA에서 기본 생성자 필요
@Entity
@Table(name="users")
public class UserEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // id Auto Increase
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column
    private String picture;

    @Column
    private String provider;

    @Column
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // updateUser 메소드 유지
    public UserEntity updateUser(String name , String picture){
        this.name = name;
        this.picture = picture;
        return this;
    }

    // 생성자에 Builder패턴 명시
    @Builder
    public UserEntity(String email, String name, String picture, Role role, String provider, String providerId) {
        this.email = email;
        this.name = name;
        this.picture = picture;
        this.role = role;
        this.provider=provider;
        this.providerId=providerId;
    }
}
