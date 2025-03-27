package com.project.webBuilder.user.dto;

import com.project.webBuilder.user.entities.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Getter
public class UserDTO implements Serializable {
    private String name;
    private String email;
    private String picture;

    @Builder
    public UserDTO(String name, String email, String picture){
        this.name=name;
        this.email=email;
        this.picture=picture;
    }

    // UserEntity를 받아서 UserDTO를 반환하는 정적 메서드
    public static UserDTO fromEntity(UserEntity userEntity) {
        return new UserDTO(userEntity.getName(), userEntity.getEmail(), userEntity.getPicture());
    }
}
