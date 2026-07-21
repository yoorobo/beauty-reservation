package com.example.springapp.controller;

import com.example.springapp.dto.BeautyServiceForm;
import com.example.springapp.service.BeautyServiceService;
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

// TODO ADMIN 권한 D3에서 적용 (/admin/** 는 SecurityConfig 생성 후 ADMIN 전용으로 제한)
// W-16 시술 관리(등록·수정·삭제) [SR-11] — 강사 구조: 클래스 @RequestMapping + 메서드 상대경로
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/services")
public class BeautyServiceController {

    private final BeautyServiceService beautyServiceService;

    // W-16 GET /admin/services — 관리 목록 + 등록폼 [SR-11]
    @GetMapping
    public String adminList(Model model) {
        model.addAttribute("services", beautyServiceService.findAll());
        model.addAttribute("beautyServiceForm", new BeautyServiceForm());
        return "admin/services";
    }

    // W-16 POST /admin/services — 등록·수정 통합 (form.getId()==null → 등록, 아니면 수정) [SR-11]
    @PostMapping
    public String save(@Valid @ModelAttribute("beautyServiceForm") BeautyServiceForm form,
                       BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("services", beautyServiceService.findAll());
            return "admin/services";
        }
        if (form.getId() == null) {
            beautyServiceService.create(form);
        } else {
            beautyServiceService.update(form.getId(), form);
        }
        return "redirect:/admin/services";
    }

    // W-16 GET /admin/services/{id}/edit — 수정폼(목록 + 프리필 폼) [SR-11]
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        var service = beautyServiceService.findById(id);
        BeautyServiceForm form = new BeautyServiceForm();
        form.setId(service.getId());
        form.setServiceName(service.getServiceName());
        form.setPrice(service.getPrice());
        form.setDuration(service.getDuration());
        form.setDescription(service.getDescription());
        model.addAttribute("services", beautyServiceService.findAll());
        model.addAttribute("beautyServiceForm", form);
        return "admin/services";
    }

    // W-16 POST /admin/services/{id}/delete — 삭제 [SR-11]
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        beautyServiceService.delete(id);
        return "redirect:/admin/services";
    }
}
