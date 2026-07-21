package com.example.springapp.service;

import com.example.springapp.domain.Member;
import com.example.springapp.domain.Provider;
import com.example.springapp.domain.Role;
import com.example.springapp.dto.MemberForm;
import com.example.springapp.dto.MemberUpdateForm;
import com.example.springapp.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    // 일반 회원가입 (LOCAL)
    @Transactional
    public Long join(MemberForm form) {
        // 이메일 중복 확인 (별도 메서드로 분리)
        validateDuplicateEmail(form.getEmail());

        Member member = new Member();
        member.setName(form.getName());
        member.setEmail(form.getEmail());
        member.setPassword(passwordEncoder.encode(form.getPassword()));
        member.setRole(Role.USER);
        member.setProvider(Provider.LOCAL);

        return memberRepository.save(member).getId();
    }

    // 이메일 중복 검증
    private void validateDuplicateEmail(String email) {
        if (memberRepository.findByEmail(email).isPresent()) {
            throw new IllegalStateException("이미 가입된 이메일입니다.");
        }
    }

    // 전체 회원 조회 — 관리자용
    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    // 회원 한 명 조회 — 관리자용
    public Member findById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다. id=" + id));
    }

    // 이메일로 회원 조회 — 로그인 회원 조회용
    public Member findByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다. email=" + email));
    }

    // 이메일 존재 여부
    public boolean existsByEmail(String email) {
        return memberRepository.findByEmail(email).isPresent();
    }

    // 일반 회원이 자신의 정보 수정 (currentEmail = 세션의 로그인 이메일, 변경 전 값)
    @Transactional
    public Member updateMyInfo(String currentEmail, MemberUpdateForm form) {
        Member member = findByEmail(currentEmail);
        updateMember(member, form);
        return member;
    }

    // 관리자가 회원 정보 수정 (id 기준)
    @Transactional
    public Member updateMemberInfo(Long id, MemberUpdateForm form) {
        Member member = findById(id);
        updateMember(member, form);
        return member;
    }

    // 회원 수정 공통 처리 — 트랜잭션 내 변경 감지(더티 체킹)로 자동 UPDATE, save() 불필요
    private void updateMember(Member member, MemberUpdateForm form) {
        String newEmail = form.getEmail().trim();
        String newName = form.getName().trim();

        // 이메일을 변경한 경우에만 중복 검사
        if (!newEmail.equals(member.getEmail())) {
            validateDuplicateEmail(newEmail);
        }

        member.setName(newName);
        member.setEmail(newEmail);
        // 비밀번호를 입력한 경우에만 변경 (비우면 기존 유지)
        if (form.getPassword() != null && !form.getPassword().isBlank()) {
            member.setPassword(passwordEncoder.encode(form.getPassword()));
        }
    }

    // 회원 삭제 — 관리자용
    @Transactional
    public void delete(Long id) {
        Member member = findById(id);   // 존재 확인 (없으면 명확한 예외)
        memberRepository.delete(member);
    }

    // 카카오 로그인 시 확인 후 저장 (기존이면 이름 갱신, 없으면 신규 가입)
    @Transactional
    public Member joinOrUpdateKakaoMember(String kakaoId, String nickname) {
        return memberRepository.findByProviderAndProviderId(Provider.KAKAO, kakaoId)
                .map(member -> {
                    member.setName(nickname);   // 더티 체킹으로 자동 UPDATE
                    return member;
                })
                .orElseGet(() -> {
                    Member member = new Member();
                    member.setEmail("kakao_" + kakaoId + "@kakao.local");
                    member.setName(nickname);
                    // 소셜 회원은 비밀번호 미사용 — nullable=false 충족용 랜덤 더미
                    member.setPassword(UUID.randomUUID().toString());
                    member.setRole(Role.USER);
                    member.setProvider(Provider.KAKAO);
                    member.setProviderId(kakaoId);
                    return memberRepository.save(member);
                });
    }
}