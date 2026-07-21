package com.example.springapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    @GetMapping("/")
    public String home() {


        return "home";
    }

    // D3(SR-03·B4): 커스텀 로그인 페이지 (formLogin.loginPage("/login") 렌더 대상)
    @GetMapping("/login")
    public String login() {
        return "login";
    }

}