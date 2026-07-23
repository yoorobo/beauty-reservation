package com.example.springapp.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

// D5(SR-07): 예약 등록·수정 폼 — 검증 메시지 한국어 원문(기존 폼 스타일 준수)
@Getter
@Setter
public class ReservationForm {

    // 수정 모드 식별용(신규는 null)
    private Long id;

    @NotNull(message = "시술을 선택해주세요.")
    private Long serviceId;

    @NotNull(message = "디자이너를 선택해주세요.")
    private Long designerId;

    // input type=date 바인딩 — 오늘 이후만 허용
    @NotNull(message = "예약 날짜를 선택해주세요.")
    @FutureOrPresent(message = "예약 날짜는 오늘 이후로 선택해주세요.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate reservationDate;

    // input type=time 바인딩(HH:mm)
    @NotNull(message = "예약 시간을 선택해주세요.")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime reservationTime;

    // 요청 메모: 선택 입력
    private String requestMemo;
}
