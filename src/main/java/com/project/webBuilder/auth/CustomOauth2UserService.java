package com.project.webBuilder.auth;

import com.project.webBuilder.user.entities.User;
import com.project.webBuilder.user.repositories.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
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
        OAuth2User oAuth2User = super.loadUser(userRequest);

        //userRequest가 google인지 naver인지 받아옴
        String provider = userRequest.getClientRegistration().getRegistrationId();

        //고유 식별자 google(sub), naver,kakao(id)
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        OAuth2UserInfo oAuth2UserInfo =null;

        if(provider.equals("google")){
            oAuth2UserInfo = new GoogleUserInfo(oAuth2User.getAttributes());
        }

        String email = oAuth2UserInfo.getEmail();
        String name = oAuth2UserInfo.getName();
        String picture = oAuth2UserInfo.getPicture().orElse("default-profile.png");

        User user;
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if(optionalUser.isPresent()){
            user=optionalUser.get();
        }else{
            user = new User();
            user.setEmail(email);
            user.setName(name);
            user.setPicture(picture);
            userRepository.save(user);
        }

        httpSession.setAttribute("user",user);

        //security에 넘겨주기 위한 객체
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(
                        user.getRole().toString())),
                oAuth2User.getAttributes(),
                userNameAttributeName
        );
    }
}
