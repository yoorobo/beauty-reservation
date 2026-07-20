# CLAUDE.md — beauty-reservation HARNESS

- 이 repo는 **HARNESS 거버넌스** 저장소다. 모든 작업은 **WO(Work Order) 단위**로만 수행한다.
- **SR 정본(正本) = `docs/requirements/`**. 구현·설계·커밋은 반드시 관련 **SR ID를 역참조**한다.
- **위험 명령(push · delete · force)** 은 **WO + 차이 게이트 통과 + 정학 승인** 없이는 금지한다.
- 각 STEP은 **증거(실행 명령 + 출력)** 를 첨부해 보고한다.
- 스테이징은 **파일별 명시**로 한다 — `git add .` 금지.
- 역할·보고 규칙·하네스 사이클은 `AGENTS.md` 참조.
