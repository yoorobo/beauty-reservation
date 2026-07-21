package com.example.springapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

// D3(SR-03): 관리자 대시보드 (접근 제어는 SecurityConfig /admin/** = ADMIN)
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    @GetMapping
    public String dashboard() {
        return "admin/index";
    }
}
