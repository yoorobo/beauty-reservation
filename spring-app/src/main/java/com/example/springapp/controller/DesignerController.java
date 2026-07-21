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

// TODO ADMIN 권한 D3에서 적용 (/admin/** 는 SecurityConfig 생성 후 ADMIN 전용으로 제한)
@Controller
@RequiredArgsConstructor
public class DesignerController {

    private final DesignerService designerService;

    // W-09 GET /designers — 공개 디자이너 목록 [SR-05]
    @GetMapping("/designers")
    public String list(Model model) {
        model.addAttribute("designers", designerService.findAll());
        return "designer/list";
    }

    // W-17 GET /admin/designers — 관리 목록 + 등록폼 [SR-11]
    @GetMapping("/admin/designers")
    public String adminList(Model model) {
        model.addAttribute("designers", designerService.findAll());
        model.addAttribute("designerForm", new DesignerForm());
        return "admin/designers";
    }

    // W-17 POST /admin/designers — 등록 [SR-11]
    @PostMapping("/admin/designers")
    public String create(@Valid @ModelAttribute("designerForm") DesignerForm form,
                         BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("designers", designerService.findAll());
            return "admin/designers";
        }
        designerService.create(form);
        return "redirect:/admin/designers";
    }

    // W-17 GET /admin/designers/{id}/edit — 수정폼(목록 + 프리필 폼) [SR-11]
    @GetMapping("/admin/designers/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        var designer = designerService.findById(id);
        DesignerForm form = new DesignerForm();
        form.setName(designer.getName());
        form.setSpecialty(designer.getSpecialty());
        form.setIntroduction(designer.getIntroduction());
        model.addAttribute("designers", designerService.findAll());
        model.addAttribute("designerForm", form);
        model.addAttribute("editId", id);
        return "admin/designers";
    }

    // W-17 POST /admin/designers/{id}/update — 수정 [SR-11]
    @PostMapping("/admin/designers/{id}/update")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("designerForm") DesignerForm form,
                         BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("designers", designerService.findAll());
            model.addAttribute("editId", id);
            return "admin/designers";
        }
        designerService.update(id, form);
        return "redirect:/admin/designers";
    }

    // W-17 GET /admin/designers/{id}/delete — 삭제 [SR-11] (login 관례: <a> GET 링크)
    @GetMapping("/admin/designers/{id}/delete")
    public String delete(@PathVariable Long id) {
        designerService.delete(id);
        return "redirect:/admin/designers";
    }
}
