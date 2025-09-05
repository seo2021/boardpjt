package com.example.boardpjt.controller;

import com.example.boardpjt.service.UserAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
                           @RequestParam String password,
                           RedirectAttributes redirectAttributes) {
        // @Valid -> 유효성 검증
        try {
            userAccountService.register(username, password);
            return "redirect:/";
        } catch (IllegalArgumentException e) {
            // 중복사용자
//            redirectAttributes.addAttribute("error", e.getMessage()); // Model이 받아서 쓸 수 있게 RequestParam으로 주는 것
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            // 자동으로 model에 넣어줘서 request attribute로 꺼내 쓸 수 있음
            return "redirect:/auth/register";
        }
//        return "redirect:/auth/login"; // login은 없으니까 403?
    }
}
