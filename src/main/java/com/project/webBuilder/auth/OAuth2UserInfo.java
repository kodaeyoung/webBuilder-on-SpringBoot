package com.project.webBuilder.auth;

import java.util.Optional;

public interface OAuth2UserInfo {
    String getEmail();
    String getName();
    Optional<String> getPicture();
}
