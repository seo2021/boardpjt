package com.example.boardpjt.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller // view resolver
public class MainController {
    @GetMapping
    public String index() {
        return "index";
    }

    @GetMapping("/my-page")
    public String myPage(Model model, Authentication authentication) {
        if (authentication != null) {
            model.addAttribute("username", authentication.getName());
        }
        return "my-page";
    }
}
