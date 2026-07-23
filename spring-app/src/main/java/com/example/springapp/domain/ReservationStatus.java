package com.example.springapp.domain;

// D5(SR-07): 예약 상태 — @Enumerated(STRING)으로 저장 (ORDINAL 금지: 순서 변경 시 데이터 깨짐)
public enum ReservationStatus {
    REQUESTED,   // 예약 요청(초기 상태)
    CONFIRMED,   // 확정
    COMPLETED,   // 완료
    CANCELED,    // 취소(삭제가 아닌 상태 변경 — 이력 보존)
    NO_SHOW      // 노쇼
}
