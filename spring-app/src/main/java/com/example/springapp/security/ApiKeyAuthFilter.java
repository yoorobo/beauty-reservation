package com.example.springapp.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// [WO-0729-17] /api/** 전용 API Key 인증 필터.
//
//   ※ 의도적으로 @Component를 붙이지 않는다.
//     Filter 타입 빈은 Spring Boot가 전역 서블릿 필터로 자동 등록하므로, 그러면 화면 요청(홈·로그인·카카오)까지
//     이 필터를 타서 401이 된다. SecurityConfig에서 new로 생성해 API 체인에만 부착한다.
//
//   C1: 키가 일치하면 ROLE_REPORT_BOT 권한의 "인증 완료" Authentication을 SecurityContext에 넣고 통과시킨다.
//       (키 비교만 하고 doFilter로 넘기면 뒤의 authorizeHttpRequests에서 익명으로 판정돼 통과하지 못한다.)
//   C2: 키가 없거나 틀리면 필터가 401 JSON으로 직접 응답을 끝낸다. doFilter를 호출하지 않으므로
//       인증 엔트리포인트가 개입할 여지가 없고, 로그인 페이지 리다이렉트도 발생하지 않는다.
//   C5: 키 원문은 헤더 값이든 설정 값이든 로그에 남기지 않는다(대조는 ApiKeyVerifier가 전담).
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    // 클라이언트(파이썬 리포트 봇)가 키를 실어 보내는 헤더 이름
    public static final String API_KEY_HEADER = "X-API-KEY";

    // 기존 Role enum(USER/ADMIN)은 건드리지 않고, 이 필터에서만 쓰는 권한을 직접 부여한다.
    private static final String REPORT_BOT_AUTHORITY = "ROLE_REPORT_BOT";
    private static final String REPORT_BOT_PRINCIPAL = "report-bot";

    private final ApiKeyVerifier apiKeyVerifier;

    public ApiKeyAuthFilter(ApiKeyVerifier apiKeyVerifier) {
        this.apiKeyVerifier = apiKeyVerifier;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String presentedKey = request.getHeader(API_KEY_HEADER);

        // [C2] 누락·불일치를 구분하지 않고 동일하게 401로 끝낸다(키 존재 여부를 응답으로 흘리지 않기 위함).
        if (!apiKeyVerifier.matches(presentedKey)) {
            ApiErrorWriter.write(response, HttpServletResponse.SC_UNAUTHORIZED, "unauthorized");
            return; // doFilter 호출하지 않음 — 여기서 요청 종료
        }

        // [C1] 인증 완료 토큰 생성 → SecurityContext에 설정 → 이후 hasRole("REPORT_BOT") 통과
        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
                REPORT_BOT_PRINCIPAL,
                null, // credentials는 보관하지 않는다(키 원문 잔류 방지)
                List.of(new SimpleGrantedAuthority(REPORT_BOT_AUTHORITY))
        );
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        try {
            filterChain.doFilter(request, response);
        } finally {
            // STATELESS 체인이므로 요청 종료 시 컨텍스트를 반드시 비운다(스레드 재사용 시 권한 잔류 방지).
            SecurityContextHolder.clearContext();
        }
    }
}
