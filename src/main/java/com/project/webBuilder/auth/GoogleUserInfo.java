package com.project.webBuilder.auth;

import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
public class GoogleUserInfo implements OAuth2UserInfo{
    private Map<String,Object> attribute;

    @Override
    public String getEmail(){
        return (String)attribute.get("email");
    }

    @Override
    public String getName(){
        return (String)attribute.get("name");
    }

    public Optional<String> getPicture(){
        return Optional.ofNullable((String)attribute.get("picture"));
    }
}
