package com.example.demo.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.thymeleaf.exceptions.TemplateInputException;

@ControllerAdvice
public class ThymeleafExceptionHandler {

    @ExceptionHandler(TemplateInputException.class)
    public String handleThymeleafError(TemplateInputException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error-thymeleaf"; // una vista simple para mostrar el error
    }
}

