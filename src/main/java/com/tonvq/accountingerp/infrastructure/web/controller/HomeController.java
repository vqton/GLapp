package com.tonvq.accountingerp.infrastructure.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Home Controller
 * 
 * Xử lý các request về trang chính
 * 
 * @author Ton VQ
 */
@Controller
@RequestMapping("/")
public class HomeController {
    
    @GetMapping
    public String home() {
        return "index";
    }
    
    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }
}
