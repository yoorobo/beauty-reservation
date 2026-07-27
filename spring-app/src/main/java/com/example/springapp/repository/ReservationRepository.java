package com.example.springapp.repository;

import com.example.springapp.domain.Member;
import com.example.springapp.domain.Reservation;
import com.example.springapp.domain.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

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
}
