package com.example.demo.controller;

import com.example.demo.Entitys.Medico;
import com.example.demo.Entitys.Paciente;
import com.example.demo.Entitys.User;
import com.example.demo.service.MedicoService;
import com.example.demo.service.PacienteService;
import com.example.demo.service.UserService;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/pacientes")
public class PacienteController {

    @Autowired
    private PacienteService pacienteService;
    
    @Autowired
    private MedicoService medicoService;
    
    @Autowired
    private UserService userService;

 // En tu PacienteController o en un nuevo MedicoPacienteController

    @GetMapping("/medico/pacientes")
    public String listarPacientesMedico(Model model, Principal principal) {
        System.out.println("=== LISTANDO PACIENTES PARA MÉDICO ===");
        
        try {
            // 1. Obtener médico logueado
            String username = principal.getName(); // email del usuario
            System.out.println("Usuario: " + username);
            
            // 2. Buscar médico por usuario
            Optional<User> usuario = userService.findByEmail(username);
            if (usuario.isEmpty()) {
                return "redirect:/login";
            }
            
            Optional<Medico> medicoOpt = medicoService.buscarPorUsuario(usuario.get());
            if (medicoOpt.isEmpty()) {
                model.addAttribute("error", "No se encontró perfil de médico");
                return "error";
            }
            
            Medico medico = medicoOpt.get();
            System.out.println("Médico: " + medico.getNombre() + " " + medico.getApellido());
            
            // 3. Obtener pacientes de ESTE médico
            // Depende de tu lógica: ¿pacientes con citas? ¿pacientes con tratamientos?
            List<Paciente> pacientes = pacienteService.buscarPorMedico(medico.getId());
            
            System.out.println("Pacientes encontrados para médico: " + pacientes.size());
            
            // 4. Pasar datos al modelo (EXACTAMENTE lo que el template espera)
            model.addAttribute("medicoNombre", medico.getNombre() + " " + medico.getApellido());
            model.addAttribute("pacientes", pacientes);
            
            return "medico/pacientes"; // <- Esto es lo IMPORTANTE
            
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }
 // En tu PacienteController, agrega:
    @GetMapping("/test")
    @ResponseBody
    public String test() {
        System.out.println("=== TEST ENDPOINT EJECUTADO ===");
        return "Test funciona - " + new java.util.Date();
    }
 

    // 2. MOSTRAR FORMULARIO DE BÚSQUEDA (GET)
    @GetMapping("/buscar")
    public String mostrarFormularioBusqueda(Model model) {
        model.addAttribute("query", "");
        return "pacientes/buscar-formulario"; // Página solo con formulario
    }

    // 3. PROCESAR BÚSQUEDA (POST)
    @PostMapping("/buscar")
    public String buscarPacientes(@RequestParam("query") String query, Model model) {
        List<Paciente> pacientes = pacienteService.buscarPorDocumentoONombre(query);
        model.addAttribute("pacientes", pacientes);
        model.addAttribute("query", query);
        return "pacientes/buscar-resultados"; // Página con resultados
    }

    // 4. BÚSQUEDA POR DOCUMENTO ESPECÍFICO (para redirección a editar)
    @GetMapping("/buscar-documento")
    public String buscarPorDocumentoForm(Model model) {
        return "pacientes/buscar-documento-form";
    }

    @PostMapping("/buscar-documento")
    public String buscarPorDocumento(@RequestParam("documento") String documento, RedirectAttributes redirectAttrs) {
        Paciente encontrado = pacienteService.buscarPorDocumento(documento);
        if (encontrado == null) {
            redirectAttrs.addFlashAttribute("mensaje", "No se encontró paciente con ese documento");
            return "redirect:/pacientes/buscar-documento";
        } else {
            return "redirect:/pacientes/editar/" + encontrado.getNumeroIdentificacion();
        }
    }

    // 5. CREAR NUEVO PACIENTE
    @GetMapping("/nuevo")
    public String mostrarFormulario(Model model) {
        model.addAttribute("paciente", new Paciente());
        return "pacientes/formulario";
    }

    @PostMapping("/guardar")
    public String guardarPaciente(@ModelAttribute Paciente paciente) {
        pacienteService.guardar(paciente);
        return "redirect:/pacientes";
    }

    // 6. EDITAR PACIENTE
    @GetMapping("/editar/{numeroIdentificacion}")
    public String editarPaciente(@PathVariable String numeroIdentificacion, Model model) {
        Paciente paciente = pacienteService.buscarPorDocumento(numeroIdentificacion); 
        if (paciente == null) {
            return "redirect:/pacientes";
        }
        model.addAttribute("paciente", paciente); 
        return "pacientes/editar";
    }

    @PostMapping("/actualizar")
    public String actualizarPaciente(@ModelAttribute Paciente paciente, Model model) {
        pacienteService.actualizarPaciente(paciente);
        model.addAttribute("mensaje", "Paciente actualizado con éxito");
        return "redirect:/pacientes";
    }
    
    // 7. API PARA BÚSQUEDA AJAX (para usar desde el modal del médico)
    @GetMapping("/api/buscar")
    @ResponseBody
    public List<Paciente> buscarPacientesApi(@RequestParam String query) {
        return pacienteService.buscarPorDocumentoONombre(query);
    }
}