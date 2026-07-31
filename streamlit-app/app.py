"""Spring Report API를 소비하는 Streamlit 업무 대시보드."""

import pandas as pd
import streamlit as st

import api_service
import report_service


RESERVATION_DISPLAY_COLUMNS = [
    "reservationId",
    "memberName",
    "serviceName",
    "designerName",
    "reservationDate",
    "reservationTime",
    "statusLabel",
]
INACTIVE_DISPLAY_COLUMNS = [
    "memberId",
    "name",
    "email",
    "lastVisitDate",
    "inactive",
]


def mask_email(value):
    """이메일 로컬 파트의 첫 글자만 남겨 화면 노출을 줄인다."""
    if not isinstance(value, str) or "@" not in value:
        return value
    local, _, domain = value.partition("@")
    if not local:
        return value
    return f"{local[0]}{'*' * max(len(local) - 1, 1)}@{domain}"


def reservation_display_frame(df: pd.DataFrame) -> pd.DataFrame:
    columns = [column for column in RESERVATION_DISPLAY_COLUMNS if column in df.columns]
    return df.loc[:, columns]


def inactive_display_frame(df: pd.DataFrame) -> pd.DataFrame:
    columns = [column for column in INACTIVE_DISPLAY_COLUMNS if column in df.columns]
    display = df.loc[:, columns].copy()
    if "email" in display.columns:
        display["email"] = display["email"].map(mask_email)
    return display


def safe_error_message(error: api_service.ApiError) -> str:
    """API 오류 원문이나 비밀값을 노출하지 않는 사용자 안내를 반환한다."""
    category = str(error)
    if category.startswith("401"):
        return "API 인증에 실패했습니다. Spring과 Streamlit의 API_KEY 설정을 확인하세요."
    if category.startswith("403"):
        return "이 계정에는 요청한 API를 사용할 권한이 없습니다."
    if category.startswith(("연결 시간 초과", "응답 시간 초과")):
        return "Spring API 응답 시간이 초과되었습니다. 서버 상태를 확인하세요."
    if category.startswith("연결 실패"):
        return "Spring API에 연결할 수 없습니다. 서버 실행 상태와 API_BASE_URL을 확인하세요."
    return "Spring API 요청을 처리하지 못했습니다. 서버 설정과 응답 상태를 확인하세요."


def show_table(title: str, df: pd.DataFrame, empty_message: str) -> None:
    st.subheader(title)
    if df.empty:
        st.info(empty_message)
        return
    st.dataframe(df, use_container_width=True, hide_index=True)


def load_dashboard_data():
    with st.spinner("예약 데이터를 불러오는 중입니다..."):
        today = api_service.get_today()
        tomorrow = api_service.get_tomorrow()
        inactive = api_service.get_inactive()
        monthly = api_service.get_monthly()
    return today, tomorrow, inactive, monthly


def main() -> None:
    st.set_page_config(page_title="Beauty Reservation Dashboard", layout="wide")
    st.title("Beauty Reservation Dashboard")

    if st.button("Refresh"):
        st.rerun()

    try:
        today, tomorrow, inactive, monthly = load_dashboard_data()
    except api_service.ApiError as error:
        st.error(safe_error_message(error))
        st.stop()
    except Exception:
        st.error("API 응답을 대시보드 데이터로 변환하지 못했습니다.")
        st.stop()

    try:
        frames = report_service.build_dataframes(today, tomorrow, inactive, monthly)
        monthly_total = monthly.get("totalReservations", 0)
        excel_downloads = {
            "tomorrow": report_service.to_excel_bytes(
                frames["tomorrow"], "Tomorrow Reservations"
            ),
            "inactive": report_service.to_excel_bytes(
                frames["inactive"], "Inactive Customers"
            ),
            "monthly": report_service.to_excel_bytes(frames["monthly"], "Monthly Report"),
        }
    except Exception:
        st.error("대시보드 또는 Excel 보고서를 준비하지 못했습니다.")
        st.stop()

    col1, col2, col3, col4 = st.columns(4)
    col1.metric("Today Reservations", len(today))
    col2.metric("Tomorrow Reservations", len(tomorrow))
    col3.metric("Inactive Customers", len(inactive))
    col4.metric("Monthly Reservations", monthly_total)

    tab_dashboard, tab_excel = st.tabs(["Reservations", "Excel Downloads"])

    with tab_dashboard:
        show_table(
            "Today Reservations",
            reservation_display_frame(frames["today"]),
            "There are no reservations for today.",
        )
        show_table(
            "Tomorrow Reservations",
            reservation_display_frame(frames["tomorrow"]),
            "There are no reservations for tomorrow.",
        )
        show_table(
            "Inactive Customers",
            inactive_display_frame(frames["inactive"]),
            "There are no inactive customers.",
        )

    with tab_excel:
        st.warning(
            "Downloaded Excel files contain personal information. "
            "Store them securely and delete them when no longer needed."
        )
        st.download_button(
            "Tomorrow Reservations",
            data=excel_downloads["tomorrow"],
            file_name="tomorrow_reservations.xlsx",
            mime="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            disabled=frames["tomorrow"].empty,
        )
        st.download_button(
            "Inactive Customers",
            data=excel_downloads["inactive"],
            file_name="inactive_customers.xlsx",
            mime="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            disabled=frames["inactive"].empty,
        )
        st.download_button(
            "Monthly Report",
            data=excel_downloads["monthly"],
            file_name="monthly_report.xlsx",
            mime="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        )


if __name__ == "__main__":
    main()
