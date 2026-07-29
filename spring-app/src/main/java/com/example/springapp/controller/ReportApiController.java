package com.example.springapp.controller;

import com.example.springapp.dto.InactiveCustomerResponse;
import com.example.springapp.dto.MonthlyReportResponse;
import com.example.springapp.dto.ReservationReportResponse;
import com.example.springapp.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.List;

// WO-0727-14(D7): Streamlit 소비용 조회 전용 REST API 4종.
//   보안은 SecurityConfig의 anyRequest().authenticated() 정책을 그대로 따름(별도 permitAll 미추가).
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReportApiController {

    private final ReportService reportService;

    // 오늘 예약 목록
    @GetMapping("/reservations/today")
    public List<ReservationReportResponse> today() {
        return reportService.today();
    }

    // 내일 예약 목록
    @GetMapping("/reservations/tomorrow")
    public List<ReservationReportResponse> tomorrow() {
        return reportService.tomorrow();
    }

    // 휴면 고객 목록 — days 기본 60일
    @GetMapping("/customers/inactive")
    public List<InactiveCustomerResponse> inactiveCustomers(
            @RequestParam(defaultValue = "60") int days) {
        return reportService.inactiveCustomers(days);
    }

    // 월간 예약 통계 — year·month 미지정 시 이번 달
    @GetMapping("/reports/monthly")
    public MonthlyReportResponse monthlyReport(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        YearMonth ym = (year != null && month != null)
                ? YearMonth.of(year, month)
                : YearMonth.now();
        return reportService.monthlyReport(ym);
    }
}
