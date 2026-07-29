package com.example.springapp.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

// [WO-0729-17] /api/** 전용 오류 응답 작성기 — 항상 JSON 본문으로 끝낸다.
//   C2: 로그인 페이지로의 리다이렉트를 원천 차단하기 위해, 필터·엔트리포인트·접근거부 핸들러가
//       모두 이 한 곳을 거쳐 상태코드와 본문을 직접 기록한다.
final class ApiErrorWriter {

    private ApiErrorWriter() {
    }

    static void write(HttpServletResponse response, int status, String error) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        // 고정 문자열만 조합한다(사용자 입력·키 원문 미반영).
        response.getWriter().write("{\"error\":\"" + error + "\"}");
        response.getWriter().flush();
    }
}
