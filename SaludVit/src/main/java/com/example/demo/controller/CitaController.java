package com.example.demo.controller;


import com.example.demo.Entitys.*;
import com.example.demo.service.CitaService;
import com.example.demo.service.MedicoService;
import com.example.demo.service.PacienteService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/citas")
public class CitaController {

    @Autowired
    private CitaService citaService;

    @Autowired
    private PacienteService pacienteService;

    @Autowired
    private MedicoService medicoService;

    // Ver listado de citas
    @GetMapping
    public String listarCitas(Model model) {
    	model.addAttribute("listaCitas", citaService.listarTodas());
        return "citas/lista";
    }


    // Formulario para nueva cita
    @GetMapping("/nueva")
    public String mostrarFormularioNuevaCita(Model model) {
        model.addAttribute("cita", new Cita());
        model.addAttribute("pacientes", pacienteService.listarTodos());
        model.addAttribute("medicos", medicoService.listarTodos());
        return "citas/formulario"; // Thymeleaf: src/main/resources/templates/citas/formulario.html
    }

    // Procesar formulario
    @PostMapping("/guardar")
    public String guardarCita(@ModelAttribute @Valid Cita cita, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("pacientes", pacienteService.listarTodos());
            model.addAttribute("medicos", medicoService.listarTodos());
            return "citas/formulario";
        }

        String error = citaService.programarCita(cita);
        if (error != null) {
            model.addAttribute("error", error);
            model.addAttribute("pacientes", pacienteService.listarTodos());
            model.addAttribute("medicos", medicoService.listarTodos());
            return "citas/formulario";
        }

        return "redirect:/citas";
    }

    // Eliminar una cita
    @GetMapping("/eliminar/{id}")
    public String eliminarCita(@PathVariable Long id) {
        citaService.cancelarCita(id);
        return "redirect:/citas";
    }
}
