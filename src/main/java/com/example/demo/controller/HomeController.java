package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    @GetMapping("/")
    public String inicio() {
        return "home";
    }
    
    @GetMapping("/sobreNosotros")
    public String sobreNosotros() {
        return "sobreNosotros";
    }

    @GetMapping("/ayuda")
    public String ayuda() {
        return "ayuda";
    }

}