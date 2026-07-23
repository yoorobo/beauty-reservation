package com.example.springapp.security;

import com.example.springapp.domain.Member;
import com.example.springapp.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

// D3(SR-03): 이메일(=username)로 회원 조회 → Spring Security UserDetails 변환, 권한은 ROLE_ 접두
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("가입되지 않은 이메일입니다: " + email));

        return User.builder()
                .username(member.getEmail())
                .password(member.getPassword() == null ? "" : member.getPassword())
                .roles(member.getRole().name())
                .build();
    }
}
