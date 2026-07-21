package com.example.springapp.controller;

import com.example.springapp.domain.Member;
import com.example.springapp.dto.MemberUpdateForm;
import com.example.springapp.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

// D3(SR-03): 관리자 회원 관리 — 목록·상세·수정·삭제
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/members")
public class AdminMemberController {

    private final MemberService memberService;

    // 빈 문자열 → null (수정 시 비밀번호 미입력 허용) [B3]
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    // 회원 목록
    @GetMapping
    public String list(Model model) {
        model.addAttribute("members", memberService.findAll());
        return "admin/members/list";
    }

    // 회원 상세
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("member", memberService.findById(id));
        return "admin/members/detail";
    }

    // 회원 수정 폼
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Member member = memberService.findById(id);
        MemberUpdateForm form = new MemberUpdateForm();
        form.setName(member.getName());
        form.setEmail(member.getEmail());
        model.addAttribute("memberUpdateForm", form);
        model.addAttribute("memberId", id);
        return "admin/members/edit";
    }

    // 회원 수정 처리
    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id,
                       @Valid @ModelAttribute("memberUpdateForm") MemberUpdateForm form,
                       BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("memberId", id);
            return "admin/members/edit";
        }
        try {
            memberService.updateMemberInfo(id, form);
        } catch (IllegalStateException e) {
            bindingResult.rejectValue("email", "duplicate", e.getMessage());
            model.addAttribute("memberId", id);
            return "admin/members/edit";
        }
        return "redirect:/admin/members/" + id;
    }

    // 회원 삭제
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        memberService.delete(id);
        return "redirect:/admin/members";
    }
}
