package com.example.springapp.service;

import com.example.springapp.domain.Member;
import com.example.springapp.domain.Reservation;
import com.example.springapp.domain.ReservationStatus;
import com.example.springapp.dto.ReservationForm;
import com.example.springapp.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

// D5(SR-07): 예약 생성·조회·수정·취소. 취소는 삭제가 아니라 상태변경(CANCELED)로 이력 보존.
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final BeautyServiceService beautyServiceService;
    private final DesignerService designerService;

    // 예약 생성 — 초기 status=REQUESTED
    @Transactional
    public Long create(Member member, ReservationForm form) {
        Reservation reservation = new Reservation();
        reservation.setMember(member);
        reservation.setBeautyService(beautyServiceService.findById(form.getServiceId()));
        reservation.setDesigner(designerService.findById(form.getDesignerId()));
        reservation.setReservationDate(form.getReservationDate());
        reservation.setReservationTime(form.getReservationTime());
        reservation.setRequestMemo(form.getRequestMemo());
        reservation.setStatus(ReservationStatus.REQUESTED);
        return reservationRepository.save(reservation).getId();
    }

    // 내 예약 목록
    public List<Reservation> findMyReservations(Member member) {
        return reservationRepository.findByMemberOrderByReservationDateDescReservationTimeDesc(member);
    }

    // 본인 예약 단건 조회 — 소유자 검증 포함(타인 접근 시 403)
    public Reservation findOwn(Long id, Member member) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다. id=" + id));
        if (!reservation.getMember().getId().equals(member.getId())) {
            throw new AccessDeniedException("본인의 예약만 접근할 수 있습니다.");
        }
        return reservation;
    }

    // 예약 수정 — 본인 예약만 (더티 체킹으로 자동 UPDATE)
    @Transactional
    public void update(Long id, Member member, ReservationForm form) {
        Reservation reservation = findOwn(id, member);
        reservation.setBeautyService(beautyServiceService.findById(form.getServiceId()));
        reservation.setDesigner(designerService.findById(form.getDesignerId()));
        reservation.setReservationDate(form.getReservationDate());
        reservation.setReservationTime(form.getReservationTime());
        reservation.setRequestMemo(form.getRequestMemo());
    }

    // 예약 취소 — 삭제가 아니라 상태변경(CANCELED). 본인 예약만.
    @Transactional
    public void cancel(Long id, Member member) {
        Reservation reservation = findOwn(id, member);
        reservation.setStatus(ReservationStatus.CANCELED);
    }

    // D6(SR-11): 관리자 예약 조회 — date/status 조합(둘 다면 AND, 없으면 전체)
    public List<Reservation> findForAdmin(LocalDate date, ReservationStatus status) {
        if (date != null && status != null) {
            return reservationRepository
                    .findByReservationDateAndStatusOrderByReservationDateDescReservationTimeDesc(date, status);
        }
        if (date != null) {
            return reservationRepository
                    .findByReservationDateOrderByReservationDateDescReservationTimeDesc(date);
        }
        if (status != null) {
            return reservationRepository
                    .findByStatusOrderByReservationDateDescReservationTimeDesc(status);
        }
        return reservationRepository.findAllByOrderByReservationDateDescReservationTimeDesc();
    }

    // D6(SR-11): 관리자 상태 변경 — 소유자 검증 없음(관리자 권한은 SecurityConfig에서 보장)
    // [WO-0727-11] 미래 예약 완료/노쇼 방지: COMPLETED·NO_SHOW는 예약일이 오늘 이하일 때만 허용
    //   (날짜 기준·당일 허용 — 당일 완료 처리 필요. 승인/취소는 날짜 제약 없음. 위반 시 예외로 상태 불변)
    @Transactional
    public void changeStatus(Long id, ReservationStatus status) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다. id=" + id));
        if ((status == ReservationStatus.COMPLETED || status == ReservationStatus.NO_SHOW)
                && reservation.getReservationDate().isAfter(LocalDate.now())) {
            throw new IllegalStateException("미래 예약은 완료/노쇼 처리할 수 없습니다.");
        }
        reservation.setStatus(status);
    }
}
