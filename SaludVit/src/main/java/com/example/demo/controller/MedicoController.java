package com.example.demo.controller;

import com.example.demo.Entitys.Medico;
import com.example.demo.service.MedicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/medicos")
public class MedicoController {

    @Autowired
    private MedicoService medicoService;

    // Mostrar lista de médicos
    @GetMapping("")
    public String listarMedicos(Model model) {
        model.addAttribute("listaMedicos", medicoService.listarTodos());
        return "medicos/lista"; // HTML: templates/medicos/lista.html
    }

    // Mostrar formulario para registrar un médico
    @GetMapping("/nuevo")
    public String mostrarFormulario(Model model) {
        model.addAttribute("medico", new Medico());
        return "medicos/formulario"; // HTML: templates/medicos/formulario.html
    }

    // Guardar médico
    @PostMapping("/guardar")
    public String guardarMedico(@ModelAttribute Medico medico) {
        medicoService.guardar(medico);
        return "redirect:/medicos"; // Vuelve a la lista
    }
}
