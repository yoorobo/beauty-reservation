package com.example.springapp.config;

import com.example.springapp.domain.Member;
import com.example.springapp.domain.Provider;
import com.example.springapp.domain.Role;
import com.example.springapp.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// D3(SR-03): 최초 기동 시 관리자 계정 시드 (기존재 시 스킵)
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String ADMIN_EMAIL = "admin@beauty.com";
    // 개발용 시드 비밀번호 — 코드 내 시드 값으로만 사용하며 BCrypt encode 후 저장
    private static final String ADMIN_RAW_PASSWORD = "12345678";

    @Override
    public void run(ApplicationArguments args) {
        if (memberRepository.findByEmail(ADMIN_EMAIL).isPresent()) {
            return; // 이미 존재하면 아무것도 하지 않음
        }
        Member admin = new Member();
        admin.setEmail(ADMIN_EMAIL);
        admin.setName("관리자");
        admin.setPassword(passwordEncoder.encode(ADMIN_RAW_PASSWORD));
        admin.setRole(Role.ADMIN);
        admin.setProvider(Provider.LOCAL);
        memberRepository.save(admin);
    }
}
