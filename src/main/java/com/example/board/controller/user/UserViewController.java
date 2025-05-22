package com.example.board.controller.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserViewController {
    @GetMapping("/login")
    public String login(){
        return "user/login";
    }

    @GetMapping("signup")
    public String signup(){
        return "user/signup";
    }
}
