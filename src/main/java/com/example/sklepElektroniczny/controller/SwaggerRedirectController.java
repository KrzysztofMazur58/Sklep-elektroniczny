package com.example.sklepElektroniczny.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SwaggerRedirectController {

    @GetMapping("/api/docs")
    public String redirectToSwagger() {
        return "redirect:/swagger-ui/index.html";
    }
}

