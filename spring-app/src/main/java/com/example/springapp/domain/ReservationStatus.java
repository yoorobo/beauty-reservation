package com.example.springapp.domain;

// D5(SR-07): 예약 상태 — @Enumerated(STRING)으로 저장 (ORDINAL 금지: 순서 변경 시 데이터 깨짐)
// WO-0723-09: 화면 표시용 한글 label 추가 (배지 색상 클래스는 템플릿에 유지 — 도메인은 CSS 비의존)
public enum ReservationStatus {
    REQUESTED("예약 요청"),   // 예약 요청(초기 상태)
    CONFIRMED("예약 승인"),   // 확정
    COMPLETED("완료"),        // 완료
    CANCELED("취소"),         // 취소(삭제가 아닌 상태 변경 — 이력 보존)
    NO_SHOW("노쇼");          // 노쇼

    private final String label;

    ReservationStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
