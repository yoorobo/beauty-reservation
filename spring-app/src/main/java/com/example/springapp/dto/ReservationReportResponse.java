package com.example.springapp.dto;

import com.example.springapp.domain.Reservation;
import com.example.springapp.domain.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalTime;

// WO-0727-14(D7): 예약 리포트 응답 DTO — 엔티티 직접 노출 금지, LAZY 연관은 서비스 트랜잭션 내에서 매핑.
//   statusLabel은 ReservationStatus.getLabel() SSOT 재사용, requestMemo는 null→"" 정규화.
public record ReservationReportResponse(
        Long reservationId,
        String memberName,
        String memberEmail,
        String serviceName,
        String designerName,
        LocalDate reservationDate,
        LocalTime reservationTime,
        ReservationStatus status,
        String statusLabel,
        String requestMemo
) {
    public static ReservationReportResponse from(Reservation r) {
        return new ReservationReportResponse(
                r.getId(),
                r.getMember().getName(),
                r.getMember().getEmail(),
                r.getBeautyService().getServiceName(),
                r.getDesigner().getName(),
                r.getReservationDate(),
                r.getReservationTime(),
                r.getStatus(),
                r.getStatus().getLabel(),
                r.getRequestMemo() == null ? "" : r.getRequestMemo()
        );
    }
}
