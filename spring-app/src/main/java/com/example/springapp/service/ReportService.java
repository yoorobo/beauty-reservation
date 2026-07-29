package com.example.springapp.service;

import com.example.springapp.domain.Member;
import com.example.springapp.domain.Reservation;
import com.example.springapp.domain.ReservationStatus;
import com.example.springapp.domain.Role;
import com.example.springapp.dto.InactiveCustomerResponse;
import com.example.springapp.dto.MonthlyReportResponse;
import com.example.springapp.dto.ReservationReportResponse;
import com.example.springapp.repository.MemberRepository;
import com.example.springapp.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// WO-0727-14(D7): Streamlit 소비용 조회 전용 리포트 서비스.
//   LAZY 연관(member·beautyService·designer) 매핑을 트랜잭션 경계 내에서 수행하도록 readOnly 트랜잭션 유지.
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;

    // 오늘 예약 — 시간 오름차순
    public List<ReservationReportResponse> today() {
        return reservationRepository
                .findByReservationDateOrderByReservationTimeAsc(LocalDate.now())
                .stream()
                .map(ReservationReportResponse::from)
                .toList();
    }

    // 내일 예약 — 시간 오름차순
    public List<ReservationReportResponse> tomorrow() {
        return reservationRepository
                .findByReservationDateOrderByReservationTimeAsc(LocalDate.now().plusDays(1))
                .stream()
                .map(ReservationReportResponse::from)
                .toList();
    }

    // 휴면 고객 — 최근 완료(COMPLETED) 예약일이 days일 이전이거나 완료 이력이 없는 회원(ADMIN 제외).
    public List<InactiveCustomerResponse> inactiveCustomers(int days) {
        LocalDate threshold = LocalDate.now().minusDays(days);
        List<InactiveCustomerResponse> result = new ArrayList<>();
        for (Member member : memberRepository.findAll()) {
            if (member.getRole() == Role.ADMIN) {
                continue;
            }
            Optional<Reservation> lastCompleted = reservationRepository
                    .findFirstByMemberAndStatusOrderByReservationDateDescReservationTimeDesc(
                            member, ReservationStatus.COMPLETED);
            LocalDate lastVisitDate = lastCompleted
                    .map(Reservation::getReservationDate)
                    .orElse(null);
            boolean inactive = lastVisitDate == null || lastVisitDate.isBefore(threshold);
            if (inactive) {
                result.add(new InactiveCustomerResponse(
                        member.getId(), member.getName(), member.getEmail(), lastVisitDate, true));
            }
        }
        return result;
    }

    // 월간 예약 통계 — 해당 월 [1일, 익월 1일) 반열림 구간 기준 집계.
    public MonthlyReportResponse monthlyReport(YearMonth ym) {
        List<Reservation> reservations = reservationRepository
                .findByReservationDateGreaterThanEqualAndReservationDateLessThan(
                        ym.atDay(1), ym.plusMonths(1).atDay(1));
        long completed = reservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.COMPLETED)
                .count();
        long canceled = reservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.CANCELED)
                .count();
        return new MonthlyReportResponse(ym.toString(), reservations.size(), completed, canceled);
    }
}
