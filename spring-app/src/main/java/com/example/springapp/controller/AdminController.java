package com.example.springapp.controller;

import com.example.springapp.domain.ReservationStatus;
import com.example.springapp.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

// D3(SR-03): 관리자 대시보드 (접근 제어는 SecurityConfig /admin/** = ADMIN)
// D6(SR-11): 관리자 예약 관리 (조회·검색·상태 변경) 추가
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final ReservationService reservationService;

    @GetMapping
    public String dashboard() {
        return "admin/index";
    }

    // D6 GET /admin/reservations — 전체/날짜/상태/날짜+상태(AND) 조회
    // date·status는 빈 문자열 유입(빈 검색 제출)에 안전하도록 String으로 받아 수동 파싱
    @GetMapping("/reservations")
    public String reservations(@RequestParam(required = false) String date,
                               @RequestParam(required = false) String status,
                               Model model) {
        LocalDate parsedDate = (date != null && !date.isBlank()) ? LocalDate.parse(date) : null;
        ReservationStatus parsedStatus =
                (status != null && !status.isBlank()) ? ReservationStatus.valueOf(status) : null;

        model.addAttribute("reservations", reservationService.findForAdmin(parsedDate, parsedStatus));
        model.addAttribute("date", parsedDate);              // 검색바 input 값·상태변경 폼 전달용
        model.addAttribute("selectedStatus", parsedStatus);  // select 선택 유지
        model.addAttribute("statuses", ReservationStatus.values());
        return "admin/reservations";
    }

    // D6 POST /admin/reservations/{id}/status — 상태 변경 후 검색조건 유지 리다이렉트
    @PostMapping("/reservations/{id}/status")
    public String changeStatus(@PathVariable Long id,
                               @RequestParam ReservationStatus status,
                               @RequestParam(required = false) String date,
                               @RequestParam(required = false) String filterStatus,
                               RedirectAttributes redirectAttributes) {
        reservationService.changeStatus(id, status);
        // 검색조건 유지 (빈 값은 제외)
        if (date != null && !date.isBlank()) {
            redirectAttributes.addAttribute("date", date);
        }
        if (filterStatus != null && !filterStatus.isBlank()) {
            redirectAttributes.addAttribute("status", filterStatus);
        }
        return "redirect:/admin/reservations";
    }
}
