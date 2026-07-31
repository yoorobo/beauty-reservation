# Beauty Reservation Dashboard

Spring Boot의 조회 전용 Report API를 `X-API-KEY`로 호출하여 예약 현황을 보여 주고
Excel 보고서를 내려받는 Streamlit 대시보드입니다.

## 환경변수

로컬 `.env`에 다음 이름을 설정합니다. 실제 값이나 `.env` 파일은 Git에 추가하지 않습니다.

```dotenv
API_BASE_URL=http://localhost:8080
API_KEY=Spring과 동일한 API Key
```

Spring도 동일한 `API_KEY` 환경변수를 사용해야 합니다. API Key는 화면에서 입력하거나
표시하지 않습니다.

## 실행

Spring 실행 구성에 DB 및 인증 환경변수를 설정한 뒤 다음 명령으로 서버를 실행합니다.

```powershell
cd spring-app
.\gradlew.bat bootRun
```

별도 터미널에서 Python 의존성을 준비한 환경으로 대시보드를 실행합니다.

```powershell
cd streamlit-app
streamlit run app.py
```

## 사용 API

- `GET /api/reservations/today`
- `GET /api/reservations/tomorrow`
- `GET /api/customers/inactive`
- `GET /api/reports/monthly`

모든 요청은 `X-API-KEY` Header가 필요합니다. Key가 없거나 다르면 Spring은 JSON `401`
응답을 반환합니다.

## 개인정보 및 생성 파일

화면에서는 예약 이메일과 요청 메모를 제외하고 미방문 고객 이메일을 마스킹합니다.
다운로드 Excel에는 업무 처리를 위한 개인정보가 포함되므로 안전한 위치에 보관하고
사용 후 삭제해야 합니다.

다음 항목은 Git에 추가하지 않습니다.

- `.env`
- `reports/*.xlsx`
- `__pycache__/`, `*.pyc`
- `.venv/`

## 이번 구현 범위 밖

- 이메일 발송
- 차트와 기간 필터
- 신규 Spring API
- AI 및 BeautyLink 기능
