"""[WO-0729-17] 8회차 — Spring Report API 소비 계층.

D7이 제공하는 조회 전용 GET 4종을 호출한다. 모든 요청에 X-API-KEY 헤더가 필요하며,
키가 없거나 틀리면 서버가 401 JSON으로 끊는다(로그인 페이지 리다이렉트 없음).

C6: 모든 호출에 timeout·raise_for_status·연결오류·JSON파싱오류 처리를 건다.
C5: API_KEY 원문은 어떤 예외 메시지·로그에도 싣지 않는다.
"""

import os

import requests
from dotenv import load_dotenv

load_dotenv()

# 끝 슬래시가 붙어 들어와도 경로가 //로 깨지지 않게 정규화한다.
API_BASE_URL = os.getenv("API_BASE_URL", "http://localhost:8080").rstrip("/")
API_KEY = os.getenv("API_KEY", "")

# (연결 타임아웃, 응답 읽기 타임아웃) — 서버가 죽어 있을 때 무한 대기를 막는다.
TIMEOUT = (5, 15)


class ApiError(RuntimeError):
    """API 호출 실패를 호출자에게 한 종류로 전달하기 위한 예외."""


def _build_headers() -> dict:
    if not API_KEY or API_KEY.startswith("여기에"):
        raise ApiError(
            ".env 의 API_KEY 가 비어 있거나 자리표시자 그대로입니다. "
            "Spring 실행구성의 API_KEY 와 동일한 값을 넣어 주세요."
        )
    return {"X-API-KEY": API_KEY, "Accept": "application/json"}


def _get(path: str, params: dict | None = None):
    """공통 GET 호출 — 네트워크·상태코드·JSON 3단계 오류를 모두 ApiError로 변환한다."""
    url = f"{API_BASE_URL}{path}"

    # 1) 네트워크 계층
    try:
        response = requests.get(url, headers=_build_headers(), params=params, timeout=TIMEOUT)
    except requests.exceptions.ConnectTimeout as e:
        raise ApiError(f"연결 시간 초과({TIMEOUT[0]}초): {url} — Spring 서버가 기동 중인지 확인하세요.") from e
    except requests.exceptions.ReadTimeout as e:
        raise ApiError(f"응답 시간 초과({TIMEOUT[1]}초): {url} — 서버 응답이 지연되고 있습니다.") from e
    except requests.exceptions.ConnectionError as e:
        raise ApiError(f"연결 실패: {url} — 주소·포트를 확인하고 Spring 서버를 먼저 실행하세요.") from e
    except requests.exceptions.RequestException as e:
        raise ApiError(f"요청 실패: {url} — {type(e).__name__}") from e

    # 2) 상태코드 계층 — 401/403은 원인이 분명하므로 별도 안내한다.
    if response.status_code == 401:
        raise ApiError(
            f"401 인증 실패: {url} — X-API-KEY 헤더가 없거나 Spring의 API_KEY 와 다릅니다. "
            "(양쪽 값을 다시 맞춰 주세요. 값 자체는 출력하지 않습니다.)"
        )
    if response.status_code == 403:
        raise ApiError(f"403 권한 없음: {url} — 허용되지 않은 API 경로입니다.")
    try:
        response.raise_for_status()
    except requests.exceptions.HTTPError as e:
        raise ApiError(f"HTTP {response.status_code} 오류: {url}") from e

    # 3) 본문 파싱 계층
    try:
        return response.json()
    except ValueError as e:
        raise ApiError(f"JSON 파싱 실패: {url} — 응답이 JSON 형식이 아닙니다.") from e


def get_today() -> list:
    """오늘 예약 목록."""
    return _get("/api/reservations/today")


def get_tomorrow() -> list:
    """내일 예약 목록."""
    return _get("/api/reservations/tomorrow")


def get_inactive(days: int = 60) -> list:
    """미방문(휴면) 고객 목록 — 기준일수 기본 60일."""
    return _get("/api/customers/inactive", params={"days": days})


def get_monthly(year: int | None = None, month: int | None = None) -> dict:
    """월간 예약 통계 — year·month 생략 시 서버가 이번 달로 처리한다."""
    params = {}
    if year is not None and month is not None:
        params = {"year": year, "month": month}
    return _get("/api/reports/monthly", params=params)
