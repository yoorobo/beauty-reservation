package com.example.springapp.controller;

import com.example.springapp.dto.DesignerForm;
import com.example.springapp.service.DesignerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

// D3(SR-03) 적용 완료: /admin/** 는 SecurityConfig에서 ADMIN 전용으로 제한
// W-17 디자이너 관리(등록·수정·삭제) [SR-11] — 강사 구조: 클래스 @RequestMapping + 메서드 상대경로
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/designers")
public class DesignerController {

    private final DesignerService designerService;

    // W-17 GET /admin/designers — 관리 목록 + 등록폼 [SR-11]
    @GetMapping
    public String adminList(Model model) {
        model.addAttribute("designers", designerService.findAll());
        model.addAttribute("designerForm", new DesignerForm());
        return "admin/designers";
    }

    // W-17 POST /admin/designers — 등록·수정 통합 (form.getId()==null → 등록, 아니면 수정) [SR-11]
    @PostMapping
    public String save(@Valid @ModelAttribute("designerForm") DesignerForm form,
                       BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("designers", designerService.findAll());
            return "admin/designers";
        }
        if (form.getId() == null) {
            designerService.create(form);
        } else {
            designerService.update(form.getId(), form);
        }
        return "redirect:/admin/designers";
    }

    // W-17 GET /admin/designers/{id}/edit — 수정폼(목록 + 프리필 폼) [SR-11]
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        var designer = designerService.findById(id);
        DesignerForm form = new DesignerForm();
        form.setId(designer.getId());
        form.setName(designer.getName());
        form.setSpecialty(designer.getSpecialty());
        form.setIntroduction(designer.getIntroduction());
        model.addAttribute("designers", designerService.findAll());
        model.addAttribute("designerForm", form);
        return "admin/designers";
    }

    // W-17 POST /admin/designers/{id}/delete — 삭제 [SR-11]
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        designerService.delete(id);
        return "redirect:/admin/designers";
    }
}
