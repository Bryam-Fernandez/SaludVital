package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/paciente")
public class PacienteMenuController {
    
    @GetMapping("/tratamientos")
    public String tratamientos(Model model, Principal principal) {
        model.addAttribute("title", "Mis Tratamientos");
        // Aquí agregarías la lógica para obtener tratamientos del paciente
        return "paciente/tratamientos";
    }
    
    @GetMapping("/recetas")
    public String recetas(Model model, Principal principal) {
        model.addAttribute("title", "Mis Recetas");
        // Aquí agregarías la lógica para obtener recetas del paciente
        return "paciente/recetas";
    }
    
    @GetMapping("/facturacion")
    public String facturacion(Model model, Principal principal) {
        model.addAttribute("title", "Mi Facturación");
        // Aquí agregarías la lógica para obtener facturas del paciente
        return "paciente/facturacion";
    }
    
    @GetMapping("/notificaciones")
    public String notificaciones(Model model, Principal principal) {
        model.addAttribute("title", "Mis Notificaciones");
        return "paciente/notificaciones";
    }
    
    @GetMapping("/configuracion")
    public String configuracion(Model model, Principal principal) {
        model.addAttribute("title", "Configuración");
        return "paciente/configuracion";
    }
}