package com.example.springapp.security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// D3(SR-03): URL 접근 정책 + 폼 로그인/로그아웃 — 화이트리스트(deny-by-default) 방식
// D4(SR-03): 카카오 oauth2Login() 공존 추가
// [WO-0729-17] 이중 SecurityFilterChain — /api/**는 API Key(무상태), 그 외 화면은 기존 세션 인증 그대로.
//   두 체인은 securityMatcher로 완전히 분리되므로 API 정책이 화면 정책에 영향을 주지 않는다.
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(ApiKeyProperties.class) // [C5] app.api.key 바인딩·검증 활성화(미설정 시 기동 실패)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final ApiKeyVerifier apiKeyVerifier;

    // ─────────────────────────────────────────────────────────────
    // (1) API 체인 — /api/** 전용. 리포트 봇(파이썬)이 소비한다.
    // ─────────────────────────────────────────────────────────────
    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http
                // 이 체인이 담당할 경로를 /api/**로 한정 — 나머지는 아래 화면 체인이 처리한다.
                .securityMatcher("/api/**")
                // [C4] 서버 간 호출이라 브라우저 세션·CSRF 토큰이 없다. API 체인에만 적용.
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // [C3] D7이 제공하는 조회 전용 GET 4개만 허용하고, 그 외 /api/** 는 전부 거부한다.
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/reservations/today").hasRole("REPORT_BOT")
                        .requestMatchers(HttpMethod.GET, "/api/reservations/tomorrow").hasRole("REPORT_BOT")
                        .requestMatchers(HttpMethod.GET, "/api/customers/inactive").hasRole("REPORT_BOT")
                        .requestMatchers(HttpMethod.GET, "/api/reports/monthly").hasRole("REPORT_BOT")
                        .anyRequest().denyAll()
                )
                // [C1] 인증 판정을 표준 인증 필터보다 먼저 수행해 SecurityContext를 채운다.
                //   @Component를 쓰지 않고 여기서 직접 생성한다 — Filter 빈은 Boot가 전역 등록해
                //   화면 요청까지 이 필터를 타게 만들기 때문이다.
                .addFilterBefore(new ApiKeyAuthFilter(apiKeyVerifier), UsernamePasswordAuthenticationFilter.class)
                // [C2] 어떤 경우에도 로그인 페이지로 리다이렉트하지 않고 JSON으로 끝낸다.
                //   401: 인증 없음 / 403: 인증됐으나 권한 부족(denyAll 경로 포함)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                ApiErrorWriter.write(response, HttpServletResponse.SC_UNAUTHORIZED, "unauthorized"))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                ApiErrorWriter.write(response, HttpServletResponse.SC_FORBIDDEN, "forbidden"))
                );
        // formLogin·oauth2Login·logout은 이 체인에 포함하지 않는다(화면 전용 기능).
        return http.build();
    }

    // ─────────────────────────────────────────────────────────────
    // (2) 화면 체인 — securityMatcher 없음(포괄). 기존 정책을 그대로 유지한다.
    //   카카오 경로(/oauth2/authorization/kakao, /login/oauth2/code/kakao)는 /api/** 가 아니므로
    //   이 체인이 처리한다. csrf 설정을 두지 않아 기본 활성 상태도 그대로다. [C4]
    // ─────────────────────────────────────────────────────────────
    @Bean
    @Order(2)
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
