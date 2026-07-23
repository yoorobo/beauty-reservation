package com.example.springapp.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalTime;

// D5(SR-07): 예약 엔티티 — 회원·시술·디자이너 3연관, 취소는 상태변경(CANCELED)로 이력 보존
@Entity
@Table(name = "reservation")
@Getter
@Setter
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 연관 3개 모두 LAZY + nullable=false (N+1 대비 지연 로딩, FK 필수)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "beauty_service_id", nullable = false)
    private BeautyService beautyService;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "designer_id", nullable = false)
    private Designer designer;

    @Column(nullable = false)
    private LocalDate reservationDate;

    @Column(nullable = false)
    private LocalTime reservationTime;

    // ORDINAL(숫자) 저장 방지 — 반드시 STRING (enum 순서 변경 시 데이터 깨짐)
    // Hibernate 7은 @Enumerated(STRING)을 MySQL 네이티브 enum(...) DDL로 생성하므로,
    // 팀 표준(VARCHAR)을 위해 JdbcTypeCode로 VARCHAR 매핑을 강제 (WO-0723-06 V2 처방 A)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private ReservationStatus status = ReservationStatus.REQUESTED;

    // 요청 메모(선택)
    @Column(length = 500)
    private String requestMemo;
}
