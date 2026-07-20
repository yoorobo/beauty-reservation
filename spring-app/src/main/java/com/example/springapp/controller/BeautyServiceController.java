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

// TODO ADMIN 권한 D3에서 적용 (/admin/** 는 SecurityConfig 생성 후 ADMIN 전용으로 제한)
@Controller
@RequiredArgsConstructor
public class BeautyServiceController {

    private final BeautyServiceService beautyServiceService;

    // W-08 GET /services — 공개 시술 목록 [SR-04]
    @GetMapping("/services")
    public String list(Model model) {
        model.addAttribute("services", beautyServiceService.findAll());
        return "service/list";
    }

    // W-16 GET /admin/services — 관리 목록 + 등록폼 [SR-11]
    @GetMapping("/admin/services")
    public String adminList(Model model) {
        model.addAttribute("services", beautyServiceService.findAll());
        model.addAttribute("form", new BeautyServiceForm());
        return "admin/services";
    }

    // W-16 POST /admin/services — 등록 [SR-11]
    @PostMapping("/admin/services")
    public String create(@Valid @ModelAttribute("form") BeautyServiceForm form,
                         BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("services", beautyServiceService.findAll());
            return "admin/services";
        }
        beautyServiceService.create(form);
        return "redirect:/admin/services";
    }

    // W-16 GET /admin/services/{id}/edit — 수정폼(목록 + 프리필 폼) [SR-11]
    @GetMapping("/admin/services/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        var service = beautyServiceService.findById(id);
        BeautyServiceForm form = new BeautyServiceForm();
        form.setServiceName(service.getServiceName());
        form.setPrice(service.getPrice());
        form.setDuration(service.getDuration());
        form.setDescription(service.getDescription());
        model.addAttribute("services", beautyServiceService.findAll());
        model.addAttribute("form", form);
        model.addAttribute("editId", id);
        return "admin/services";
    }

    // W-16 POST /admin/services/{id}/update — 수정 [SR-11]
    @PostMapping("/admin/services/{id}/update")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("form") BeautyServiceForm form,
                         BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("services", beautyServiceService.findAll());
            model.addAttribute("editId", id);
            return "admin/services";
        }
        beautyServiceService.update(id, form);
        return "redirect:/admin/services";
    }

    // W-16 POST /admin/services/{id}/delete — 삭제 [SR-11]
    @PostMapping("/admin/services/{id}/delete")
    public String delete(@PathVariable Long id) {
        beautyServiceService.delete(id);
        return "redirect:/admin/services";
    }
}
