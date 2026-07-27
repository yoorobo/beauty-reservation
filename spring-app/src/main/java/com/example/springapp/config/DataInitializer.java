package com.example.springapp.config;

import com.example.springapp.domain.Member;
import com.example.springapp.domain.Provider;
import com.example.springapp.domain.Role;
import com.example.springapp.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

// D3(SR-03): 최초 기동 시 관리자 계정 시드 (기존재 시 스킵)
// [WO-0727-09] 초기 비밀번호 하드코딩 제거 → 환경변수(app.admin.initial-password) 주입
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String ADMIN_EMAIL = "admin@beauty.com";

    // 관리자 초기 비밀번호: 소스에 하드코딩하지 않고 환경변수 ADMIN_INITIAL_PASSWORD로 주입 [WO-0727-09]
    @Value("${app.admin.initial-password:}")
    private String adminInitialPassword;

    @Override
    public void run(ApplicationArguments args) {
        if (memberRepository.findByEmail(ADMIN_EMAIL).isPresent()) {
            return; // 이미 존재하면 아무것도 하지 않음 (기존 계정 비밀번호 덮어쓰기 없음)
        }
        // 안전 처리: 초기 비밀번호 미설정 시 빈 비밀번호 계정 생성을 방지하고 시드를 건너뜀 [WO-0727-09]
        if (!StringUtils.hasText(adminInitialPassword)) {
            log.warn("관리자 초기 비밀번호(app.admin.initial-password / 환경변수 ADMIN_INITIAL_PASSWORD)가 "
                    + "설정되지 않아 관리자 계정 시드를 건너뜁니다. 환경변수 설정 후 재기동하세요.");
            return;
        }
        Member admin = new Member();
        admin.setEmail(ADMIN_EMAIL);
        admin.setName("관리자");
        admin.setPassword(passwordEncoder.encode(adminInitialPassword));
        admin.setRole(Role.ADMIN);
        admin.setProvider(Provider.LOCAL);
        memberRepository.save(admin);
        log.info("관리자 계정을 시드했습니다: {}", ADMIN_EMAIL);
    }
}
