# beauty-reservation

뷰티샵 예약 서비스 — Spring Boot 기반 구현 저장소. **HARNESS 거버넌스**로 운영되며,
모든 작업은 WO(Work Order) 단위로 수행되고 SR(시스템 요구사항)을 정본으로 삼는다.

> ⚠️ 본 README는 임시본이다. D10 마일스톤에서 재작성된다.

## 문서 구조 (docs/)

```
docs/
├─ requirements/
│  └─ 01_system_requirements.md   # SR 정본 (UR·SR v3 확정)
├─ design-docs/
│  ├─ 02_interface_spec.md        # IS 인터페이스 명세
│  ├─ 03_impl_tdd_plan.md         # 구현 · TDD 검증 계획
│  └─ 04_component_diagram.pdf    # UML 컴포넌트 다이어그램
└─ exec-plans/                    # WO 실행 계획 (작업 단위)
```

- `scripts/` — 운영·리포트 스크립트
- `reports/` — 생성 리포트 산출물 (버전관리 제외)
- 거버넌스·역할·사이클: `CLAUDE.md`, `AGENTS.md`

## 애플리케이션

- `spring-app/` — Spring Boot 애플리케이션 (D1: 프로젝트 골격 + 메인 화면)

## 보류 항목

- SA(소프트웨어 아키텍처) 다이어그램 Mermaid 소스(05) — 추후 WO에서 반영 예정.
