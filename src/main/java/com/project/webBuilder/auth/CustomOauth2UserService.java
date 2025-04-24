package com.project.webBuilder.auth;

import com.project.webBuilder.user.dto.UserDTO;
import com.project.webBuilder.user.entities.UserEntity;
import com.project.webBuilder.user.repositories.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@RequiredArgsConstructor // final 또는 @NonNull 이 붙은 필드만을 포함한 생성자 생성
@Service
public class CustomOauth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;
    private final HttpSession httpSession;


    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException{

        //userRequest를 통해 OAuth2User 로드
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);
        System.out.println(oAuth2User);

        //userRequest가 google인지 naver인지 받아옴
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        //고유 식별자 google(sub), naver,kakao(id)
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

        UserEntity user = saveOrUpdate(attributes);

        //security에 넘겨주기 위한 객체
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(
                        user.getRole().toString())),
                oAuth2User.getAttributes(),
                userNameAttributeName
        );
    }

    private UserEntity saveOrUpdate(OAuthAttributes attributes) {
        UserEntity user = userRepository.findByEmail(attributes.getEmail())
                .map(entity -> entity.updateUser(attributes.getName(), attributes.getPicture()))
                .orElse(attributes.toEntity());

        return userRepository.save(user);
    }
}