package com.example.springapp.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

// D3(SR-03): URL 접근 정책 + 폼 로그인/로그아웃 — 화이트리스트(deny-by-default) 방식
// D4(SR-03): 카카오 oauth2Login() 공존 추가
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // 정적 리소스·오류 페이지 공개
                        .requestMatchers("/css/**", "/js/**", "/images/**",
                                "/favicon.ico", "/error").permitAll()
                        // 공개 페이지: 로그인·회원가입 (홈 포함 그 외는 로그인 필요)
                        .requestMatchers("/login", "/members/join").permitAll()
                        // 관리자 전용: 시술·디자이너·회원 관리 (D2 TODO(SR-03) 해소 지점)
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // 그 외 전부 로그인 필요 (홈·내 정보 포함)
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("email")   // 이메일을 username으로 사용
                        .passwordParameter("password")
                        .defaultSuccessUrl("/")
                        .permitAll()
                )
                // D4(SR-03): 카카오 OAuth2 로그인 — formLogin과 동일 loginPage 공유, 로그인 성공 시 홈으로
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .defaultSuccessUrl("/")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );
        return http.build();
    }
}