# 뷰티샵 예약 시스템 — SA 다이어그램 소스 (Mermaid)

- 근거: SR 확정본 v3.0 · VR-0720-02 판정 1
- 형식: Mermaid diagram-as-code. GitHub에서 자동 렌더링됨.
- 동반 산출물: 04_component_diagram.pdf (UML 2.5 정식 표기, 클로드 디자인 산출)

---

## 1. ERD (SA-DB)

```mermaid
erDiagram
    MEMBERS ||--o{ RESERVATION : "makes (1:N)"
    BEAUTY_SERVICE ||--o{ RESERVATION : "selected (1:N)"
    DESIGNER ||--o{ RESERVATION : "assigned (1:N)"

    MEMBERS {
        bigint id PK
        varchar email UK "LOCAL=실이메일 KAKAO=대체가능"
        varchar password "KAKAO는 NULL"
        varchar name
        varchar role "USER ADMIN"
        varchar provider "LOCAL KAKAO - 복합UK1"
        varchar provider_id "KAKAO id LOCAL=NULL - 복합UK1"
    }
    RESERVATION_CONSTRAINTS {
        varchar UK_provider "UNIQUE(provider, provider_id)"
        varchar UK_slot "UNIQUE(designer_id, reservation_date, reservation_time)"
        varchar FK_policy "시술·디자이너 FK는 ON DELETE RESTRICT"
    }
    BEAUTY_SERVICE {
        bigint id PK
        varchar service_name
        int price
        int duration "분 단위 - 겹침계산 사용"
        varchar description
    }
    DESIGNER {
        bigint id PK
        varchar name
        varchar specialty
        varchar introduction
    }
    RESERVATION {
        bigint id PK
        bigint member_id FK
        bigint beauty_service_id FK
        bigint designer_id FK
        date reservation_date
        time reservation_time
        varchar status "5상태 Enum"
        varchar request_memo
    }
```

**설계 판단 [v2 확정]**: ①회원 식별 = 복합 UNIQUE(provider, provider_id), provider_id 단독 UK 없음 [SR-02]. ②이중예약 = 앱 계층 구간겹침 판정 + DB 보조 UNIQUE(designer_id, reservation_date, reservation_time) 이중방어, DDL에 둘 다 명시 [SR-07]. ③예약이 참조하는 시술·디자이너 FK는 ON DELETE RESTRICT — Service 검사 + DB 정책 이중방어 [SR-11].

---

## 2. 컴포넌트 다이어그램 (SA-ARCH)

```mermaid
flowchart LR
    subgraph Browser["브라우저"]
        V["Thymeleaf View"]
    end
    subgraph App["Spring Boot :8080"]
        SEC["Spring Security\n인증필터 + ADMIN권한 [SR-03]"]
        subgraph P["Presentation"]
            HC[HomeController]
            MC[MemberController]
            RC[ReservationController]
            AC["AdminController [SR-10~13]"]
            API["RestApiController [SR-14]"]
        end
        subgraph B["Business"]
            MS["MemberService [SR-01,02]"]
            RS["ReservationService [SR-06~09]"]
            CS["CatalogService [SR-04,05,11]"]
        end
        subgraph R["Persistence (JPA)"]
            MR[MemberRepository]
            RR[ReservationRepository]
            SR2[ServiceRepository]
            DR[DesignerRepository]
        end
    end
    DB[("MySQL\nbeauty_service")]
    KAKAO["Kakao OAuth2"]
    subgraph Auto["자동화 D8~9"]
        PY["report.py [SR-15]\nreport_bot 세션쿠키"]
        ST["Streamlit [SR-16]"]
    end
    V --> SEC --> P
    MC --> MS
    RC --> RS
    AC --> RS
    AC --> CS
    AC --> MS
    API --> RS
    MS --> MR
    RS --> RR
    CS --> SR2
    CS --> DR
    MR & RR & SR2 & DR --> DB
    MS -."인가코드→토큰→사용자정보".-> KAKAO
    PY --> API
    ST --> API
```

**설계 근거 [v2]**: 자동화 계층은 API만 소비. AdminController→MemberService = 고객 목록·상세·이메일검색 [SR-12]. RestApiController→ReservationService = 관리자 예약 API 전용 조회 [SR-14]. 인증 로직은 MemberService 격리 — Phase 2 대비.

---

## 3. 시퀀스 다이어그램 — 예약 신청 (UR-06 / SR-06,07)

```mermaid
sequenceDiagram
    actor U as 고객
    participant C as ReservationController
    participant S as Spring Security
    participant SV as ReservationService
    participant R as ReservationRepository
    participant DB as MySQL

    U->>S: GET /reservations/new
    Note over S: Security Filter가 Controller보다 선행 [SR-03]
    alt 미로그인
        S-->>U: redirect /login
    else 로그인
        S->>C: 요청 전달
        C-->>U: 예약 폼(시술·디자이너·날짜·시간·메모)
        U->>S: POST /reservations
        S->>C: 인증 통과
        C->>SV: createReservation(form, loginMember)
        Note over SV: @Transactional 경계 시작 [v2]<br/>동시성 제어(잠금 전략)는 D5 구현계획에서 확정
        SV->>R: 같은 디자이너·날짜의 REQUESTED·CONFIRMED 예약을<br/>BeautyService.duration과 함께 조회(fetch join) [v2]
        R->>DB: SELECT (예약 + duration 단일 쿼리)
        DB-->>SV: 기존 예약 목록
        Note over SV: 겹침 판정 [SR-07 v3확정]<br/>기존시작 < 신규종료 AND 신규시작 < 기존종료<br/>(종료 = 시작 + duration)
        alt 겹침 있음
            SV-->>C: 예외(이미 예약된 시간)
            C-->>U: 폼 재표시 + 오류
        else 겹침 없음
            SV->>SV: status = REQUESTED [SR-06]
            SV->>R: save()
            R->>DB: INSERT
            SV-->>C: 완료
            C-->>U: redirect /reservations/my [SR-08]
        end
    end
```

**[v2] 트랜잭션·동시성**: createReservation은 @Transactional로 실행. D5에서는 동일 디자이너·날짜 예약 생성에 비관적 잠금 등 구간 중복 경쟁조건을 방어할 전략을 확정한다. DB 슬롯 UNIQUE는 동일 시작시각 충돌의 보조 방어로만 사용한다.

**[v2] Repository 책임**: 겹침 조회는 같은 디자이너·날짜·활성상태(REQUESTED·CONFIRMED) 예약과 각 BeautyService.duration을 fetch join으로 함께 취득한다 (N+1 방지, NFR-02 연계).

---

## 4. 상태 전이 다이어그램 (SR-10 전이표)

```mermaid
stateDiagram-v2
    [*] --> REQUESTED : 예약 생성 [SR-06]
    REQUESTED --> CONFIRMED : ADMIN 확정
    REQUESTED --> CANCELED : 본인/ADMIN 취소
    CONFIRMED --> COMPLETED : ADMIN 완료
    CONFIRMED --> CANCELED : 본인/ADMIN 취소
    CONFIRMED --> NO_SHOW : 시작시각 경과 후 ADMIN 수동 [v3확정]
    COMPLETED --> [*]
    CANCELED --> [*]
    NO_SHOW --> [*]
```

이외 모든 전이는 거부된다. 검증: 허용 5경로 성공 + 금지 전이 거부.
```
