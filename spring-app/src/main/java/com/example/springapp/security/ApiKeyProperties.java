package com.example.springapp.security;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

// [WO-0729-17] Report API 인증 키 바인딩 — 키 로딩·검증을 한 곳에 모아 필터가 이를 사용(이식 시 교체 지점).
//   C5: 기본값을 두지 않는다. application.yaml의 app.api.key=${API_KEY}는
//       (1) 환경변수 미설정 → 플레이스홀더 해석 실패로 기동 중단
//       (2) 빈 문자열     → @NotBlank 위반으로 기동 중단
//       두 경우 모두 fail-fast 시킨다. 키 원문은 어디에도 로깅하지 않는다.
@Validated
@ConfigurationProperties(prefix = "app.api")
public record ApiKeyProperties(

        @NotBlank(message = "환경변수 API_KEY(app.api.key)가 설정되지 않았습니다. 애플리케이션을 시작할 수 없습니다.")
        String key
) {
}
