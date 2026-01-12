package com.example.demo.controller;

import com.example.demo.Entitys.Medico;
import com.example.demo.Entitys.Paciente;
import com.example.demo.service.*;
import com.example.demo.service.MedicoService;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/medico")
public class PanelMedicoController {

    private final MedicoService medicoService;
    private final PacienteService pacienteService; 

    public PanelMedicoController(MedicoService medicoService,
                                 PacienteService pacienteService) {
        this.medicoService = medicoService;
        this.pacienteService = pacienteService; 
    }

    @GetMapping("/panel")
    public String panelMedico(Model model, Authentication auth) {
        Medico m = medicoService.medicoActual(auth);
        model.addAttribute("medico", m);
        return "medico/panelMedico"; 
    }

    @GetMapping("/pacientes")
    public String listarPacientes(Model model, @RequestParam(value = "q", required = false) String q) {
        List<Paciente> pacientes;
        
        if (q != null && !q.isEmpty()) {
            pacientes = pacienteService.buscarPorDocumentoONombre(q);
        } else {
            pacientes = pacienteService.listarTodos();
        }

        model.addAttribute("pacientes", pacientes);
        model.addAttribute("q", q); 
        return "pacientes/lista";
    }
    
    
}
