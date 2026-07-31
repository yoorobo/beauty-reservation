"""[WO-0729-17] 8회차 — API 응답(JSON) → DataFrame → Excel 보고서 생성 계층.

C7: Excel 수식 주입 방어. 사용자가 입력한 문자열(memberName·requestMemo·designerName 등)이
    '=' '+' '-' '@' 로 시작하면 Excel이 이를 수식으로 해석해 실행한다(CSV/XLSX 공통 위험).
    앞에 작은따옴표를 붙여 텍스트로 강제하고, 문자열 컬럼 전체에 일괄 적용한다.
C7: 파일명은 고정값만 사용한다 — 고객 이름·이메일 등 개인정보를 파일명에 넣지 않는다.

DataFrame 컬럼명은 D7 DTO의 JSON 키(camelCase)를 그대로 쓴다.
"""

from io import BytesIO
from pathlib import Path

import pandas as pd

# reports/ 는 이 파일과 같은 폴더 기준 — 어느 경로에서 실행해도 같은 위치에 쌓인다.
REPORTS_DIR = Path(__file__).resolve().parent / "reports"

# D7 DTO의 JSON 키. 데이터가 0건이어도 헤더가 남도록 명시한다.
RESERVATION_COLUMNS = [
    "reservationId", "memberName", "memberEmail", "serviceName", "designerName",
    "reservationDate", "reservationTime", "status", "statusLabel", "requestMemo",
]
INACTIVE_COLUMNS = ["memberId", "name", "email", "lastVisitDate", "inactive"]
MONTHLY_COLUMNS = ["month", "totalReservations", "completed", "canceled"]

# 고정 파일명 4종 (개인정보 미포함)
FILE_TOMORROW = "내일예약고객.xlsx"
FILE_INACTIVE = "미방문고객목록.xlsx"
FILE_MONTHLY = "월간예약현황.xlsx"
FILE_COMBINED = "뷰티샵_업무보고서.xlsx"

# Excel이 수식·명령으로 해석할 수 있는 선두 문자
_FORMULA_PREFIXES = ("=", "+", "-", "@", "\t", "\r")


def ensure_reports_dir() -> Path:
    """reports/ 폴더가 없으면 만든다."""
    REPORTS_DIR.mkdir(parents=True, exist_ok=True)
    return REPORTS_DIR


def sanitize_cell(value):
    """[C7] 수식으로 해석될 수 있는 문자열이면 앞에 '를 붙여 텍스트로 고정한다."""
    if isinstance(value, str) and value.startswith(_FORMULA_PREFIXES):
        return "'" + value
    return value


def sanitize_dataframe(df: pd.DataFrame) -> pd.DataFrame:
    """[C7] 문자열(object) 컬럼 전체에 sanitize_cell 을 일괄 적용한다."""
    if df.empty:
        return df
    safe = df.copy()
    for column in safe.columns:
        if safe[column].dtype == "object":
            safe[column] = safe[column].map(sanitize_cell)
    return safe


def to_dataframe(records, columns: list) -> pd.DataFrame:
    """JSON(list[dict] 또는 dict) → 정규화·수식방어까지 끝낸 DataFrame."""
    if isinstance(records, dict):      # 월간 통계는 단일 객체로 온다.
        records = [records]
    if not records:                    # 0건이어도 헤더는 유지한다.
        return pd.DataFrame(columns=columns)
    df = pd.DataFrame(records)
    # 서버가 준 키 순서와 무관하게 컬럼 순서를 고정한다(없는 키는 건너뛴다).
    ordered = [c for c in columns if c in df.columns]
    remaining = [c for c in df.columns if c not in ordered]
    df = df[ordered + remaining]
    return sanitize_dataframe(df)


def build_dataframes(today, tomorrow, inactive, monthly) -> dict:
    """4종 응답을 한 번에 DataFrame으로 변환한다."""
    return {
        "today": to_dataframe(today, RESERVATION_COLUMNS),
        "tomorrow": to_dataframe(tomorrow, RESERVATION_COLUMNS),
        "inactive": to_dataframe(inactive, INACTIVE_COLUMNS),
        "monthly": to_dataframe(monthly, MONTHLY_COLUMNS),
    }


def _save_single(df: pd.DataFrame, filename: str, sheet_name: str) -> Path:
    path = ensure_reports_dir() / filename
    df.to_excel(path, index=False, sheet_name=sheet_name, engine="openpyxl")
    return path


def to_excel_bytes(df: pd.DataFrame, sheet_name: str) -> bytes:
    """검증·정제된 DataFrame을 디스크에 저장하지 않고 XLSX bytes로 반환한다."""
    safe_df = sanitize_dataframe(df)
    output = BytesIO()
    with pd.ExcelWriter(output, engine="openpyxl") as writer:
        safe_df.to_excel(writer, index=False, sheet_name=sheet_name)
    return output.getvalue()


def save_combined(frames: dict) -> Path:
    """4종을 시트로 묶은 통합 업무보고서."""
    path = ensure_reports_dir() / FILE_COMBINED
    with pd.ExcelWriter(path, engine="openpyxl") as writer:
        frames["today"].to_excel(writer, index=False, sheet_name="오늘예약")
        frames["tomorrow"].to_excel(writer, index=False, sheet_name="내일예약")
        frames["inactive"].to_excel(writer, index=False, sheet_name="미방문고객")
        frames["monthly"].to_excel(writer, index=False, sheet_name="월간현황")
    return path


def build_all_reports(today, tomorrow, inactive, monthly) -> tuple:
    """엑셀 4종을 생성하고 (프레임 dict, 생성 경로 목록)을 돌려준다."""
    frames = build_dataframes(today, tomorrow, inactive, monthly)
    paths = [
        _save_single(frames["tomorrow"], FILE_TOMORROW, "내일예약"),
        _save_single(frames["inactive"], FILE_INACTIVE, "미방문고객"),
        _save_single(frames["monthly"], FILE_MONTHLY, "월간현황"),
        save_combined(frames),
    ]
    return frames, paths
