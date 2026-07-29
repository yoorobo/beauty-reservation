package com.example.springapp.repository;

import com.example.springapp.domain.Member;
import com.example.springapp.domain.Reservation;
import com.example.springapp.domain.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

// D5(SR-07): 예약 조회 리포지토리
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // 내 예약 목록 — 최신 예약일·시간 순 정렬
    List<Reservation> findByMemberOrderByReservationDateDescReservationTimeDesc(Member member);

    // D6(SR-11): 관리자 예약 조회 — 전체 / 날짜 / 상태 / 날짜+상태 (모두 날짜·시간 내림차순)
    List<Reservation> findAllByOrderByReservationDateDescReservationTimeDesc();

    List<Reservation> findByReservationDateOrderByReservationDateDescReservationTimeDesc(LocalDate reservationDate);

    List<Reservation> findByStatusOrderByReservationDateDescReservationTimeDesc(ReservationStatus status);

    List<Reservation> findByReservationDateAndStatusOrderByReservationDateDescReservationTimeDesc(
            LocalDate reservationDate, ReservationStatus status);

    // WO-0727-14(D7): Streamlit 리포트용 조회 전용 메서드 4종 (시간 오름차순 — 일자 내 진행 순서)
    // 특정 일자 예약 — 시간 오름차순
    List<Reservation> findByReservationDateOrderByReservationTimeAsc(LocalDate reservationDate);

    // 특정 일자+상태 예약 — 시간 오름차순
    List<Reservation> findByReservationDateAndStatusOrderByReservationTimeAsc(
            LocalDate reservationDate, ReservationStatus status);

    // 회원의 최근 특정 상태 예약 1건 (휴면 판정용 — 최근 완료일 조회)
    Optional<Reservation> findFirstByMemberAndStatusOrderByReservationDateDescReservationTimeDesc(
            Member member, ReservationStatus status);

    // 월간 통계용 — [시작일, 종료일) 반열림 구간 조회
    List<Reservation> findByReservationDateGreaterThanEqualAndReservationDateLessThan(
            LocalDate startInclusive, LocalDate endExclusive);
}
