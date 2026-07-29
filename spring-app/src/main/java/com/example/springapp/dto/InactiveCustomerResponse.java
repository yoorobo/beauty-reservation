package com.example.springapp.dto;

import java.time.LocalDate;

// WO-0727-14(D7): 휴면 고객 응답 DTO — lastVisitDate는 최근 완료(COMPLETED) 예약일(완료 이력 없으면 null).
public record InactiveCustomerResponse(
        Long memberId,
        String name,
        String email,
        LocalDate lastVisitDate,
        boolean inactive
) {
}
