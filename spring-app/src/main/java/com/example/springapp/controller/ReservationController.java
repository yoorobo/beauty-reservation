package com.example.springapp.controller;

import com.example.springapp.domain.Member;
import com.example.springapp.domain.Reservation;
import com.example.springapp.dto.ReservationForm;
import com.example.springapp.service.BeautyServiceService;
import com.example.springapp.service.DesignerService;
import com.example.springapp.service.MemberService;
import com.example.springapp.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// D5(SR-07): 고객 예약하기·내 예약·수정·취소. /reservations/** 는 SecurityConfig anyRequest().authenticated() 로 인증 필요.
@Controller
@RequiredArgsConstructor
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final BeautyServiceService beautyServiceService;
    private final DesignerService designerService;
    private final MemberService memberService;

    // ── WO-0727-02 영업시간 슬롯 설정 (SSOT — 향후 매장 설정으로 분리 가능하도록 한 곳에 정의) ──
    // 영업 10:00~19:00 가정, 마지막 접수 18:30, 30분 간격
    private static final LocalTime BUSINESS_OPEN = LocalTime.of(10, 0);
    private static final LocalTime LAST_SLOT = LocalTime.of(18, 30);
    private static final int SLOT_INTERVAL_MINUTES = 30;
    private static final DateTimeFormatter SLOT_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    // 허용 슬롯 목록 — 표시(모델)·서버 검증 공통 소스
    private static List<LocalTime> businessSlots() {
        List<LocalTime> slots = new ArrayList<>();
        for (LocalTime t = BUSINESS_OPEN; !t.isAfter(LAST_SLOT); t = t.plusMinutes(SLOT_INTERVAL_MINUTES)) {
            slots.add(t);
        }
        return slots;
    }

    // 폼 공통 주입: 시술·디자이너 옵션 + 시간 슬롯("HH:mm") + 오늘 날짜(min) + 레거시 시각
    private void addFormModel(Model model, ReservationForm form) {
        model.addAttribute("services", beautyServiceService.findAll());
        model.addAttribute("designers", designerService.findAll());
        model.addAttribute("today", LocalDate.now());   // R4: date min = 오늘

        List<LocalTime> slots = businessSlots();
        List<String> timeSlots = new ArrayList<>();
        for (LocalTime t : slots) {
            timeSlots.add(t.format(SLOT_FORMAT));
        }
        model.addAttribute("timeSlots", timeSlots);      // R1: 슬롯은 컨트롤러 제공(템플릿 하드코딩 금지)

        // R2: 수정 대상의 기존 시각이 슬롯에 없으면(레거시 데이터) 값 유실 방지 위해 별도 노출
        LocalTime current = (form != null) ? form.getReservationTime() : null;
        String legacySlot = (current != null && !slots.contains(current)) ? current.format(SLOT_FORMAT) : null;
        model.addAttribute("legacySlot", legacySlot);
    }

    // R3: 서버측 슬롯 검증 — select는 UI 제약일 뿐이므로 POST 임의 시각을 거부(한국어 메시지).
    // 단 수정 모드에서 제출값이 '기존 저장 시각'(레거시)과 동일하면 저장을 막지 않는다(R2 케이스).
    private void validateTimeSlot(ReservationForm form, BindingResult bindingResult, LocalTime grandfathered) {
        LocalTime t = form.getReservationTime();
        if (t == null) {
            return; // @NotNull / 형식(typeMismatch) 오류는 기존 검증이 처리
        }
        if (businessSlots().contains(t)) {
            return;
        }
        if (grandfathered != null && t.equals(grandfathered)) {
            return; // 기존 예약 시각 유지 — 수정 저장 허용
        }
        bindingResult.rejectValue("reservationTime", "reservationTime.slot",
                "예약 시간은 영업시간(10:00~18:30) 내 30분 단위로 선택해주세요.");
    }

    // 로그인 사용자 → Member 해석. 폼 로그인(UserDetails)·카카오(OAuth2User) 모두 지원.
    // 카카오는 D4 CustomOAuth2UserService가 attributes에 넣어둔 loginEmail 재사용.
    private Member resolveMember(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        String email;
        if (principal instanceof UserDetails userDetails) {
            email = userDetails.getUsername();
        } else if (principal instanceof OAuth2User oAuth2User) {
            email = (String) oAuth2User.getAttributes().get("loginEmail");
        } else {
            throw new IllegalStateException("지원하지 않는 인증 주체입니다: " + principal);
        }
        return memberService.findByEmail(email);
    }

    // GET /reservations/new — 예약 폼(서비스·디자이너 목록 제공)
    @GetMapping("/new")
    public String newForm(Model model) {
        ReservationForm form = new ReservationForm();
        model.addAttribute("reservationForm", form);
        addFormModel(model, form);
        return "reservations/form";
    }

    // POST /reservations — 예약 저장 → 내 예약으로
    @PostMapping
    public String create(@Valid @ModelAttribute("reservationForm") ReservationForm form,
                         BindingResult bindingResult, Authentication authentication, Model model) {
        validateTimeSlot(form, bindingResult, null);   // R3: 신규는 슬롯만 허용(그랜드파더 없음)
        if (bindingResult.hasErrors()) {
            addFormModel(model, form);
            return "reservations/form";
        }
        reservationService.create(resolveMember(authentication), form);
        return "redirect:/reservations/my";
    }

    // GET /reservations/my — 내 예약 목록
    @GetMapping("/my")
    public String myList(Authentication authentication, Model model) {
        Member member = resolveMember(authentication);
        model.addAttribute("reservations", reservationService.findMyReservations(member));
        return "reservations/my";
    }

    // GET /reservations/{id}/edit — 수정 폼(본인 예약만)
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Authentication authentication, Model model) {
        Member member = resolveMember(authentication);
        Reservation reservation = reservationService.findOwn(id, member);
        ReservationForm form = new ReservationForm();
        form.setId(reservation.getId());
        form.setServiceId(reservation.getBeautyService().getId());
        form.setDesignerId(reservation.getDesigner().getId());
        form.setReservationDate(reservation.getReservationDate());
        form.setReservationTime(reservation.getReservationTime());
        form.setRequestMemo(reservation.getRequestMemo());
        model.addAttribute("reservationForm", form);
        addFormModel(model, form);
        return "reservations/form";
    }

    // POST /reservations/{id}/edit — 수정 처리(본인 예약만)
    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id,
                       @Valid @ModelAttribute("reservationForm") ReservationForm form,
                       BindingResult bindingResult, Authentication authentication, Model model) {
        Member member = resolveMember(authentication);
        // R2/R3: 기존 저장 시각을 그랜드파더로 넘겨 레거시(비슬롯) 예약도 수정 저장 가능하게
        LocalTime original = reservationService.findOwn(id, member).getReservationTime();
        validateTimeSlot(form, bindingResult, original);
        if (bindingResult.hasErrors()) {
            addFormModel(model, form);
            return "reservations/form";
        }
        reservationService.update(id, member, form);
        return "redirect:/reservations/my";
    }

    // POST /reservations/{id}/cancel — 취소 처리(상태변경 CANCELED, 삭제 아님)
    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, Authentication authentication) {
        reservationService.cancel(id, resolveMember(authentication));
        return "redirect:/reservations/my";
    }
}
