package com.example.springapp.security;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

// [WO-0729-17] API Key 대조 전담 컴포넌트 — 필터는 판정 결과만 사용한다(검증 책임 분리).
//   · 원문 대신 SHA-256 다이제스트를 상수시간 비교(MessageDigest.isEqual)한다.
//     다이제스트는 길이가 항상 32바이트로 고정되므로 키 길이 차이조차 타이밍으로 새지 않는다.
//   · 키 원문은 필드로 보관하지 않으며, 어떤 로그·예외 메시지에도 담지 않는다. [C5]
@Component
public class ApiKeyVerifier {

    private static final String ALGORITHM = "SHA-256";

    private final byte[] expectedDigest;

    public ApiKeyVerifier(ApiKeyProperties properties) {
        // 이중 안전장치: @Validated 바인딩을 우회한 경로로 생성되더라도 빈 키로는 기동하지 못한다. [C5]
        if (!StringUtils.hasText(properties.key())) {
            throw new IllegalStateException(
                    "환경변수 API_KEY(app.api.key)가 비어 있습니다. API Key 인증을 초기화할 수 없습니다.");
        }
        this.expectedDigest = digest(properties.key());
    }

    // 제시된 키가 설정된 키와 일치하는지 판정한다. null·빈 문자열은 항상 불일치.
    public boolean matches(String presentedKey) {
        if (!StringUtils.hasText(presentedKey)) {
            return false;
        }
        return MessageDigest.isEqual(expectedDigest, digest(presentedKey));
    }

    private static byte[] digest(String value) {
        try {
            return MessageDigest.getInstance(ALGORITHM)
                    .digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            // SHA-256은 JDK 표준 필수 알고리즘이라 실제로는 도달하지 않는다.
            throw new IllegalStateException(ALGORITHM + " 알고리즘을 사용할 수 없습니다.", e);
        }
    }
}
