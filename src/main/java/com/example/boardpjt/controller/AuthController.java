package com.example.boardpjt.controller;

import com.example.boardpjt.service.UserAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller // view -> template -> html(thymeleaf)
@RequiredArgsConstructor // 생성자 주입
@RequestMapping("/auth") // prefix -> /auth/**
public class AuthController {
    private final UserAccountService userAccountService;

    // 회원가입용 페이지로 전달하는...
    @GetMapping("/register") // join? new? // GET
    public String registerForm() {
        return "register"; // templates/register.html
    }

    // 해당 처리를 Service로 전달해주는...
    @PostMapping("/register") // POST
    public String register(@RequestParam String username,
                           @RequestParam String password) {
        // @Valid -> 유효성 검증
        userAccountService.register(username, password);
        return "redirect:/";
//        return "redirect:/auth/login"; // login은 없으니까 403?
    }
}
