package com.example.springapp.repository;

import com.example.springapp.domain.Member;
import com.example.springapp.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// D5(SR-07): 예약 조회 리포지토리
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // 내 예약 목록 — 최신 예약일·시간 순 정렬
    List<Reservation> findByMemberOrderByReservationDateDescReservationTimeDesc(Member member);
}
