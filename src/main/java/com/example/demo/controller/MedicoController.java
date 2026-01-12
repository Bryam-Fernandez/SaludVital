package com.example.demo.controller;

import com.example.demo.Entitys.Medico;
import com.example.demo.Entitys.Role;
import com.example.demo.Entitys.User;
import com.example.demo.enums.Especialidad;
import com.example.demo.enums.EstadoDoctor;
import com.example.demo.Repository.RoleRepository;
import com.example.demo.Repository.UserRepository;
import com.example.demo.service.MedicoService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.transaction.Transactional;
import java.util.Optional;

@Controller
@RequestMapping("/medicos")
public class MedicoController {

    private final MedicoService medicoService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public MedicoController(MedicoService medicoService,
                            UserRepository userRepository,
                            RoleRepository roleRepository,
                            PasswordEncoder passwordEncoder) {
        this.medicoService = medicoService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("")
    public String listarMedicos(Model model) {
        model.addAttribute("medicos", medicoService.listarTodos());
        return "medicos/lista";  
    }

    @GetMapping("/nuevo")
    public String mostrarFormulario(Model model) {
        model.addAttribute("medico", new Medico());
        model.addAttribute("especialidades", Especialidad.values());
        model.addAttribute("estados", EstadoDoctor.values());
        return "medicos/formulario"; 
    }

    @PostMapping("/guardar")
    @Transactional
    public String guardarMedico(@ModelAttribute("medico") Medico medico,
                                @RequestParam("userPassword") String userPassword,
                                RedirectAttributes ra) {
        
        // Validaciones básicas
        if (medico.getEmail() == null || medico.getEmail().isBlank()) {
            ra.addFlashAttribute("error", "El email es obligatorio para crear el usuario del médico.");
            return "redirect:/medicos/nuevo";
        }
        
        if (userPassword == null || userPassword.isBlank()) {
            ra.addFlashAttribute("error", "La contraseña es obligatoria.");
            return "redirect:/medicos/nuevo";
        }

        // Verificar si el email ya existe
        Optional<User> usuarioExistente = userRepository.findByEmail(medico.getEmail());
        if (usuarioExistente.isPresent()) {
            ra.addFlashAttribute("error", "Ya existe un usuario con el email: " + medico.getEmail());
            return "redirect:/medicos/nuevo";
        }

        // Crear usuario
        Role rolMedico = roleRepository.findByName("ROLE_MEDICO")
                .orElseThrow(() -> new IllegalStateException("Falta el rol ROLE_MEDICO en la tabla roles."));

        User user = new User();
        user.setEmail(medico.getEmail());
        user.setName((medico.getNombre() == null ? "" : medico.getNombre()) + " " +
                     (medico.getApellido() == null ? "" : medico.getApellido()));
        user.setPassword(passwordEncoder.encode(userPassword));
        user.getRoles().add(rolMedico);
        
        // Guardar usuario primero
        userRepository.save(user);

        // Configurar estado por defecto si es necesario
        if (medico.getEstado() == null) {
            medico.setEstado(EstadoDoctor.ACTIVO);
        }

        // Asociar usuario al médico
        medico.setUsuario(user);
        
        // Guardar médico
        try {
            medicoService.guardar(medico);
            ra.addFlashAttribute("success", "Médico registrado y usuario creado correctamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error al guardar médico: " + e.getMessage());
            return "redirect:/medicos/nuevo";
        }

        return "redirect:/medicos";
    }
}