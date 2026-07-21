package com.example.springapp.repository;

import com.example.springapp.domain.Member;
import com.example.springapp.domain.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    // 로그인 시 CustomUserDetailsService가 이메일로 회원 조회
    Optional<Member> findByEmail(String email);

    // 소셜 로그인 회원 찾기: provider와 providerId가 모두 일치하는 Member
    Optional<Member> findByProviderAndProviderId(Provider provider, String providerId);
}