package com.example.springapp.security;

import com.example.springapp.domain.Member;
import com.example.springapp.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

// D4(SR-03): 카카오 사용자정보 로드 → find-or-create → OAuth2User 반환 (oauth2Login 자동 방식)
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberService memberService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 카카오가 준 원본 사용자정보 로드
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 원본 attributes 복사 (id, connected_at, properties, kakao_account 등)
        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());

        // 카카오 회원 식별자(id) — user-name-attribute와 동일
        String kakaoId = String.valueOf(attributes.get("id"));
        // 닉네임 추출 (폴백 체인)
        String nickname = extractNickname(attributes);

        // 확인 후 저장 (기존이면 이름 갱신, 없으면 신규 가입)
        Member member = memberService.joinOrUpdateKakaoMember(kakaoId, nickname);

        // 뷰/후속 처리를 위해 로그인 이메일·닉네임을 attributes에 추가
        attributes.put("loginEmail", member.getEmail());
        attributes.put("nickname", member.getName());

        // ROLE_ 접두 권한 부여, user-name-attribute="id" 기준
        return new DefaultOAuth2User(
                java.util.List.of(new SimpleGrantedAuthority("ROLE_" + member.getRole().name())),
                attributes,
                "id"
        );
    }

    // 닉네임 폴백: properties.nickname → kakao_account.profile.nickname → "카카오회원"
    @SuppressWarnings("unchecked")
    private String extractNickname(Map<String, Object> attributes) {
        Object propertiesObj = attributes.get("properties");
        if (propertiesObj instanceof Map<?, ?> properties) {
            Object nickname = ((Map<String, Object>) properties).get("nickname");
            if (nickname != null) {
                return nickname.toString();
            }
        }
        Object accountObj = attributes.get("kakao_account");
        if (accountObj instanceof Map<?, ?> account) {
            Object profileObj = ((Map<String, Object>) account).get("profile");
            if (profileObj instanceof Map<?, ?> profile) {
                Object nickname = ((Map<String, Object>) profile).get("nickname");
                if (nickname != null) {
                    return nickname.toString();
                }
            }
        }
        return "카카오회원";
    }
}
