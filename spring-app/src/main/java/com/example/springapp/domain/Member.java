package com.example.springapp.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "members")
@Getter
@Setter
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    // ORDINAL(숫자) 저장 방지 — 반드시 STRING
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role=Role.USER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Provider provider = Provider.LOCAL;

    // 소셜 로그인 시 외부 서비스가 준 회원 식별자 (LOCAL이면 null)
    @Column(nullable = true)
    private String providerId;
}