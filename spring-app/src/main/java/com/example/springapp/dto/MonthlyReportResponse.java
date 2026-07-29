package com.example.springapp.dto;

// WO-0727-14(D7): 월간 예약 통계 응답 DTO — month는 YearMonth.toString()(yyyy-MM).
public record MonthlyReportResponse(
        String month,
        long totalReservations,
        long completed,
        long canceled
) {
}
