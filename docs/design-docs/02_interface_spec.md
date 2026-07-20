# 뷰티샵 예약 시스템 — IS (인터페이스 명세서)

- 날짜: 2026-07-20 | 상위: SA 확정본(VR-0720-02) · UR/SR 확정본 v3.0
- 목적: 계층 간 계약(웹 엔드포인트 + REST API + Service 시그니처)을 구현 착수 가능한
  수준으로 명세한다. 각 항목은 SR 역참조와 검증 기준을 갖는다.
- 표기: 웹 화면(Thymeleaf) 엔드포인트 / REST(JSON) 엔드포인트 / Service 인터페이스 분리

---

## 1. 웹 엔드포인트 명세 (Thymeleaf)

| ID | Method | Path | 권한 | 요청 | 응답(View/redirect) | SR | 차시 |
|---|---|---|---|---|---|---|---|
| W-01 | GET | `/` | ALL | - | home | UR-06진입 | D1✅ |
| W-02 | GET | `/members/join` | ALL | - | member/join | SR-01 | D3 |
| W-03 | POST | `/members/join` | ALL | MemberJoinForm | redirect:/login | SR-01 | D3 |
| W-04 | GET | `/login` | ALL | - | login | SR-01 | D3 |
| W-05 | POST | `/login` | ALL | loginId, password (formLogin) | redirect:/ | SR-01 | D3 |
| W-06 | GET | `/oauth/kakao/callback` | ALL | code(param) | redirect:/ | SR-02 | D4 |
| W-07 | POST | `/logout` | USER | - | redirect:/ | SR-01 | D3 |
| W-08 | GET | `/services` | ALL | - | service/list | SR-04 | D2 |
| W-09 | GET | `/designers` | ALL | - | designer/list | SR-05 | D2 |
| W-10 | GET | `/reservations/new` | USER | - | reservation/form | SR-06 | D5 |
| W-11 | POST | `/reservations` | USER | ReservationForm | redirect:/reservations/my | SR-06,07 | D5 |
| W-12 | GET | `/reservations/my` | USER | - | reservation/my | SR-08 | D5 |
| W-13 | POST | `/reservations/{id}/cancel` | USER | id(path) | redirect:/reservations/my | SR-09 | D5 |
| W-14 | GET | `/admin/reservations` | ADMIN | keyword, status, page(param) | admin/reservations | SR-13 | D6 |
| W-15 | POST | `/admin/reservations/{id}/status` | ADMIN | id(path), status | redirect:/admin/reservations | SR-10 | D6 |
| W-16 | GET/POST | `/admin/services/**` | ADMIN | ServiceForm | admin/services | SR-11 | D7 |
| W-17 | GET/POST | `/admin/designers/**` | ADMIN | DesignerForm | admin/designers | SR-11 | D7 |
| W-18 | GET | `/admin/members` | ADMIN | keyword(email 검색) | admin/members | SR-12 | D7 |

---

## 2. REST API 명세 (JSON)

### API-01: 관리자 예약 조회 [SR-14]

- **Method / Path**: `GET /api/reservations`
- **권한**: ADMIN (서비스 계정 report_bot 세션 쿠키 인증)
- **Query**: `from`(date, optional), `to`(date, optional)
- **Response 200** (application/json):

```json
[
  {
    "id": 1,
    "memberName": "홍길동",
    "serviceName": "커트",
    "designerName": "김디자이너",
    "reservationDate": "2026-07-25",
    "reservationTime": "14:00",
    "status": "CONFIRMED"
  }
]
```

- **Error**: 401(미인증 쿠키 없음) / 403(USER 권한) / 500
- **검증**: 쿠키 없이 호출 → 401/403, report_bot 로그인 후 → 200 + 필드 7종

---

## 3. Service 인터페이스 명세

### MemberService [SR-01, SR-02]

```
Member join(MemberJoinForm form)
  - LOCAL 가입: provider=LOCAL, provider_id=null, password=bcrypt
  - email UNIQUE 위반 시 예외
  - @kakao.local 도메인 이메일 입력 시 거부 [SR-02 v3]

Member kakaoLogin(String code)
  - 인가코드→토큰→사용자정보 3단 교환
  - (provider=KAKAO, provider_id)로 식별, 없으면 자동 가입
  - 이메일 미제공/충돌 시 kakao_{정규화 provider_id}@kakao.local 저장

UserDetails loadUserByUsername(String loginId)  // Spring Security 계약
```

### ReservationService [SR-06, 07, 08, 09]

```
Long createReservation(ReservationForm form, Member loginMember)  // @Transactional
  - 같은 디자이너·날짜의 REQUESTED·CONFIRMED 예약 + duration을 fetch join 조회
  - 겹침 판정: 기존시작 < 신규종료 AND 신규시작 < 기존종료 (종료=시작+duration)
  - 겹침 시 예외, 아니면 status=REQUESTED로 save
  - 동시성: 비관적 잠금 전략은 D5 구현계획 확정 (DB 슬롯 UNIQUE는 보조)

List<Reservation> findMyReservations(Member loginMember)  // 본인 것만 [SR-08]

void cancelReservation(Long id, Member loginMember)  // [SR-09]
  - 소유자 검증 (member_id == loginMember.id), 아니면 예외
  - REQUESTED·CONFIRMED에서만 CANCELED로 전환

void changeStatus(Long id, ReservationStatus next)  // ADMIN [SR-10]
  - 전이표 검증: 허용 5경로만, 종료 상태에서 전이 시 예외
```

### CatalogService [SR-04, 05, 11]

```
List<BeautyService> findAllServices()      // [SR-04]
List<Designer> findAllDesigners()          // [SR-05]
void saveService / deleteService(Long id)  // ADMIN [SR-11]
  - delete 시 예약 참조 존재하면 예외 (FK RESTRICT + Service 검사 이중)
void saveDesigner / deleteDesigner(Long id)  // 동일 정책
```

### AdminQueryService [SR-12, 13]

```
Page<Reservation> searchReservations(String memberName, String designerName,
                                     ReservationStatus status, Pageable pageable)  // [SR-13]
  - 검색 조건 3개, 상태 필터 5종, 10건/페이지

List<Member> searchMembersByEmail(String keyword)  // [SR-12] 목록·상세·이메일 검색
```

---

## 4. DTO (Form) 명세

| DTO | 필드 | 검증 애노테이션 | 관련 |
|---|---|---|---|
| MemberJoinForm | loginId, email, password, name | @NotBlank, @Email | SR-01 |
| ReservationForm | serviceId, designerId, reservationDate, reservationTime, requestMemo | @NotNull(id/date/time) | SR-06 |
| ServiceForm | serviceName, price, duration, description | @NotBlank, @Positive | SR-11 |
| DesignerForm | name, specialty, introduction | @NotBlank(name) | SR-11 |

---

## 5. 에러 응답 규약

| 상황 | 웹 | REST |
|---|---|---|
| 검증 실패 | 폼 재표시 + 필드 오류 | 400 |
| 미인증 | redirect:/login | 401 |
| 권한 없음 | 403 페이지 | 403 |
| 리소스 없음 | 404 페이지 | 404 |
| 서버 오류 | 5xx 페이지 | 500 |

(웹 4xx/5xx 커스텀 페이지는 qna-board 패턴 재사용)

---

## 6. IS ↔ SR 추적 요약

W-01~18 + API-01 + Service 4종이 SR v3.0의 기능 SR 16건을 전건 커버한다.
미배치 SR 없음. 각 엔드포인트의 "검증"은 SR의 pass/fail 기준과 1:1 연결된다.
