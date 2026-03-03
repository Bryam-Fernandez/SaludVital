package com.example.demo.controller;

import com.example.demo.Entitys.Medico;
import com.example.demo.Entitys.Role;
import com.example.demo.Entitys.User;
import com.example.demo.enums.Especialidad;
import com.example.demo.enums.EstadoDoctor;
import com.example.demo.Repository.RoleRepository;
import com.example.demo.Repository.UserRepository;
import com.example.demo.dto.UserDto;
import com.example.demo.service.MedicoService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.transaction.Transactional;

import java.math.BigDecimal;
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

    @GetMapping("/registrar")
    public String mostrarFormulario(Model model) {
        // Creamos un UserDto para el template registro-medico-admin.html
        model.addAttribute("usuario", new UserDto());
        
        // También puedes pasar otros atributos si los necesitas
        model.addAttribute("especialidades", Especialidad.values());
        model.addAttribute("estados", EstadoDoctor.values());
        
        return "registro-medico-admin"; // Usamos tu template existente
    }

    @PostMapping("/guardar-desde-registro")
    @Transactional
    public String guardarMedicoDesdeRegistro(@ModelAttribute("usuario") UserDto userDto,
                                             RedirectAttributes ra) {
        
        System.out.println("===== INICIO GUARDAR MÉDICO =====");
        System.out.println("Email: " + userDto.getEmail());
        System.out.println("Nombre: " + userDto.getFirstName());
        System.out.println("Apellido: " + userDto.getLastName());
        System.out.println("Rol: " + userDto.getSelectedRole());
        
        try {
            // Validar que el rol sea MÉDICO
            if (!"ROLE_MEDICO".equals(userDto.getSelectedRole())) {
                System.out.println("ERROR: Rol incorrecto - " + userDto.getSelectedRole());
                ra.addFlashAttribute("error", "Este formulario es solo para registrar médicos");
                return "redirect:/medicos/registrar";
            }
            
            // Validaciones básicas
            if (userDto.getEmail() == null || userDto.getEmail().isBlank()) {
                System.out.println("ERROR: Email vacío");
                ra.addFlashAttribute("error", "El email es obligatorio para crear el usuario del médico.");
                return "redirect:/medicos/registrar";
            }
            
            if (userDto.getPassword() == null || userDto.getPassword().isBlank()) {
                System.out.println("ERROR: Password vacío");
                ra.addFlashAttribute("error", "La contraseña es obligatoria.");
                return "redirect:/medicos/registrar";
            }

            // Verificar si el email ya existe
            Optional<User> usuarioExistente = userRepository.findByEmail(userDto.getEmail());
            if (usuarioExistente.isPresent()) {
                System.out.println("ERROR: Email ya existe - " + userDto.getEmail());
                ra.addFlashAttribute("error", "Ya existe un usuario con el email: " + userDto.getEmail());
                return "redirect:/medicos/registrar";
            }

            // Crear objeto Medico con los datos del UserDto
            Medico medico = new Medico();
            medico.setNombre(userDto.getFirstName());
            medico.setApellido(userDto.getLastName());
            medico.setEmail(userDto.getEmail());
            
            // VALORES POR DEFECTO OBLIGATORIOS
            medico.setNumeroLicencia("PENDIENTE-" + System.currentTimeMillis()); // Temporal
            medico.setTelefono("PENDIENTE"); // Temporal
            medico.setEspecialidad(Especialidad.MEDICINA_GENERAL); // Por defecto
            medico.setTarifaConsulta(new BigDecimal("150.00")); // Por defecto
            medico.setDisponible(true); // Por defecto

            // Crear usuario
            Role rolMedico = roleRepository.findByName("ROLE_MEDICO")
                    .orElseThrow(() -> new IllegalStateException("Falta el rol ROLE_MEDICO en la tabla roles."));
            System.out.println("Rol encontrado: " + rolMedico.getName());

            User user = new User();
            user.setEmail(userDto.getEmail());
            user.setName(userDto.getFirstName() + " " + userDto.getLastName());
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
            user.getRoles().add(rolMedico);
            
            // Guardar usuario primero
            System.out.println("Guardando usuario...");
            userRepository.save(user);
            System.out.println("Usuario guardado con ID: " + user.getId());

            // Asociar usuario al médico
            medico.setUsuario(user);
            
            // Guardar médico
            System.out.println("Guardando médico...");
            medicoService.guardar(medico);
            System.out.println("Médico guardado con ID: " + medico.getId());
            
            ra.addFlashAttribute("success", "Médico registrado y usuario creado correctamente.");
            return "redirect:/medicos";
            
        } catch (Exception e) {
            System.out.println("ERROR EXCEPCIÓN: " + e.getMessage());
            e.printStackTrace();
            ra.addFlashAttribute("error", "Error al guardar médico: " + e.getMessage());
            return "redirect:/medicos/registrar";
        }
    }
}