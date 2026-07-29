package com.example.springapp.controller;

import com.example.springapp.service.BeautyServiceService;
import com.example.springapp.service.DesignerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final BeautyServiceService beautyServiceService;
    private final DesignerService designerService;

    // WO-0728-15: 홈 화면 실데이터 연결 — 시술·디자이너 목록을 Model에 주입
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("services", beautyServiceService.findAll());
        model.addAttribute("designers", designerService.findAll());
        return "home";
    }

    // D3(SR-03·B4): 커스텀 로그인 페이지 (formLogin.loginPage("/login") 렌더 대상)
    @GetMapping("/login")
    public String login() {
        return "login";
    }

}