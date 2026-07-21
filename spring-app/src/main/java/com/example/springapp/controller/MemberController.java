package com.example.springapp.controller;

import com.example.springapp.domain.Member;
import com.example.springapp.dto.MemberForm;
import com.example.springapp.dto.MemberUpdateForm;
import com.example.springapp.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

// D3(SR-03): 회원가입 + 내 정보 조회·수정 (내 정보는 로그인 세션 이메일 기준)
@Controller
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    // 빈 문자열 → null 변환: 수정폼(memberUpdateForm) 전용 [B3]
    // 이름 지정으로 join의 memberForm은 제외 — 가입폼은 강사 검증 동작(빈값 시
    // @NotBlank+@Size 메시지 2개) 유지, 수정폼은 비밀번호 비우면 기존 유지
    @InitBinder("memberUpdateForm")
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    // 회원가입 폼
    @GetMapping("/join")
    public String joinForm(Model model) {
        model.addAttribute("memberForm", new MemberForm());
        return "members/join";
    }

    // 회원가입 처리 → 성공 시 로그인 페이지로
    @PostMapping("/join")
    public String join(@Valid @ModelAttribute("memberForm") MemberForm form,
                       BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "members/join";
        }
        try {
            memberService.join(form);
        } catch (IllegalStateException e) {
            // 이메일 중복: "이미 가입된 이메일입니다."
            bindingResult.rejectValue("email", "duplicate", e.getMessage());
            return "members/join";
        }
        return "redirect:/login";
    }

    // 내 정보 조회
    @GetMapping("/my")
    public String myInfo(@AuthenticationPrincipal UserDetails principal, Model model) {
        Member member = memberService.findByEmail(principal.getUsername());
        model.addAttribute("member", member);
        return "members/my";
    }

    // 내 정보 수정 폼
    @GetMapping("/my/edit")
    public String editForm(@AuthenticationPrincipal UserDetails principal, Model model) {
        Member member = memberService.findByEmail(principal.getUsername());
        MemberUpdateForm form = new MemberUpdateForm();
        form.setName(member.getName());
        form.setEmail(member.getEmail());
        model.addAttribute("memberUpdateForm", form);
        return "members/my-edit";
    }

    // 내 정보 수정 처리 (세션 이메일 사용)
    @PostMapping("/my/edit")
    public String edit(@AuthenticationPrincipal UserDetails principal,
                       @Valid @ModelAttribute("memberUpdateForm") MemberUpdateForm form,
                       BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "members/my-edit";
        }
        try {
            memberService.updateMyInfo(principal.getUsername(), form);
        } catch (IllegalStateException e) {
            bindingResult.rejectValue("email", "duplicate", e.getMessage());
            return "members/my-edit";
        }
        return "redirect:/members/my";
    }
}