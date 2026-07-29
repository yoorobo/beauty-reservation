"""[WO-0729-17] 8회차 — 콘솔 진입점.

Spring Report API 4종을 호출해 요약을 출력하고 Excel 보고서 4개를 생성한다.

실행:  python day08_report.py
사전:  ① Spring 서버 기동(환경변수 API_KEY 설정)  ② .env 의 API_KEY 를 같은 값으로

C5: API_KEY 원문은 출력하지 않는다. 고객 데이터도 통째로 덤프하지 않고
    건수 요약 + 상위 몇 건 미리보기(이메일 마스킹)만 보여준다.
"""

import sys

import pandas as pd

import api_service
import report_service

# 미리보기로 보여줄 최대 행 수 — 전체 덤프 방지
PREVIEW_ROWS = 5

# 미리보기에서 마스킹할 컬럼(개인 식별 정보)
EMAIL_COLUMNS = ("memberEmail", "email")


def mask_email(value):
    """abc@naver.com → a**@naver.com (로컬파트 첫 글자만 노출)."""
    if not isinstance(value, str) or "@" not in value:
        return value
    local, _, domain = value.partition("@")
    if not local:
        return value
    return f"{local[0]}{'*' * max(len(local) - 1, 1)}@{domain}"


def print_preview(title: str, df: pd.DataFrame) -> None:
    """건수 + 상위 N행 미리보기 출력(이메일 마스킹)."""
    print(f"\n{title} — 총 {len(df)}건")
    if df.empty:
        print("(데이터 없음)")
        return

    preview = df.head(PREVIEW_ROWS).copy()
    for column in EMAIL_COLUMNS:
        if column in preview.columns:
            preview[column] = preview[column].map(mask_email)
    print(preview.to_string(index=False))
    if len(df) > PREVIEW_ROWS:
        print(f"... 외 {len(df) - PREVIEW_ROWS}건 (전체는 엑셀 파일에서 확인)")


def main() -> int:
    print("Spring API에 연결 중입니다.")
    print(f"대상 서버: {api_service.API_BASE_URL}")  # 주소만 출력, 키는 출력하지 않는다.

    try:
        today = api_service.get_today()
        tomorrow = api_service.get_tomorrow()
        inactive = api_service.get_inactive(days=60)
        monthly = api_service.get_monthly()
    except api_service.ApiError as e:
        print(f"\n[실패] {e}", file=sys.stderr)
        return 1

    frames, paths = report_service.build_all_reports(today, tomorrow, inactive, monthly)

    print_preview("오늘 예약 고객", frames["today"])
    print_preview("내일 예약 고객", frames["tomorrow"])
    print_preview("미방문 고객", frames["inactive"])
    print_preview("월간 예약 현황", frames["monthly"])

    print("\nExcel 보고서 생성 완료")
    for path in paths:
        print(f"- reports/{path.name}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
