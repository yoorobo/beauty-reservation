package com.example.springapp.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

// D3(SR-03): URL 접근 정책 + 폼 로그인/로그아웃
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // 관리자 전용: 시술·디자이너·회원 관리 (D2 TODO(SR-03) 해소 지점)
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // 로그인 필요: 내 정보 조회·수정
                .requestMatchers("/members/my", "/members/my/edit").authenticated()
                // 그 외(홈·회원가입·로그인·정적 리소스)는 공개
                .anyRequest().permitAll()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("email")   // 이메일을 username으로 사용
                .passwordParameter("password")
                .defaultSuccessUrl("/")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );
        return http.build();
    }
}
