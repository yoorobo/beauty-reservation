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

// D5(SR-07): 고객 예약하기·내 예약·수정·취소. /reservations/** 는 SecurityConfig anyRequest().authenticated() 로 인증 필요.
@Controller
@RequiredArgsConstructor
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final BeautyServiceService beautyServiceService;
    private final DesignerService designerService;
    private final MemberService memberService;

    // 폼 select 옵션(시술·디자이너) 공통 주입
    private void addSelectOptions(Model model) {
        model.addAttribute("services", beautyServiceService.findAll());
        model.addAttribute("designers", designerService.findAll());
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
        model.addAttribute("reservationForm", new ReservationForm());
        addSelectOptions(model);
        return "reservations/form";
    }

    // POST /reservations — 예약 저장 → 내 예약으로
    @PostMapping
    public String create(@Valid @ModelAttribute("reservationForm") ReservationForm form,
                         BindingResult bindingResult, Authentication authentication, Model model) {
        if (bindingResult.hasErrors()) {
            addSelectOptions(model);
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
        addSelectOptions(model);
        return "reservations/form";
    }

    // POST /reservations/{id}/edit — 수정 처리(본인 예약만)
    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id,
                       @Valid @ModelAttribute("reservationForm") ReservationForm form,
                       BindingResult bindingResult, Authentication authentication, Model model) {
        if (bindingResult.hasErrors()) {
            addSelectOptions(model);
            return "reservations/form";
        }
        reservationService.update(id, resolveMember(authentication), form);
        return "redirect:/reservations/my";
    }

    // POST /reservations/{id}/cancel — 취소 처리(상태변경 CANCELED, 삭제 아님)
    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, Authentication authentication) {
        reservationService.cancel(id, resolveMember(authentication));
        return "redirect:/reservations/my";
    }
}
