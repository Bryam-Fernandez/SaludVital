package com.example.demo.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.dto.UserDto;
import com.example.demo.Entitys.User;
import com.example.demo.service.UserService;

@Controller
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("index")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }
   
    @GetMapping("register")
    public String showRegistrationForm(Model model) {
        UserDto user = new UserDto();	
        model.addAttribute("user", user);
        return "register";
    }

    @PostMapping("/register/save")
    public String registrarPaciente(@ModelAttribute("user") UserDto userDto, 
                                   RedirectAttributes redirectAttrs) {
        try {
            userService.saveUser(userDto); 
            redirectAttrs.addFlashAttribute("success", "Usuario registrado exitosamente");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Error al registrar usuario: " + e.getMessage());
        }
        return "redirect:/register";
    }

    @GetMapping("/users")
    public String listRegisteredUsers(Model model) {
        List<UserDto> users = userService.findAllUsers();
        model.addAttribute("users", users);
        return "users";
    }
    
    @GetMapping("/panel")
    public String mostrarPanel(Model model, Principal principal) {
        System.out.println("========== ENTRO A /panel ==========");
        System.out.println("Usuario: " + principal.getName());
        
        try {
            String email = principal.getName(); 
            System.out.println("Email: " + email);
            
            UserDto usuarioDto = userService.findUserDtoByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            System.out.println("Usuario encontrado: " + usuarioDto.getName());
            
            model.addAttribute("nombre", usuarioDto.getName());
            model.addAttribute("totalUsuarios", 48);
            model.addAttribute("totalPacientes", 1248);
            model.addAttribute("totalMedicos", 56);
            model.addAttribute("citasHoy", 89);
            
            System.out.println("Renderizando vista: PanelAdmin");
            return "PanelAdmin";
            
        } catch (Exception e) {
            System.out.println("ERROR en /panel: " + e.getMessage());
            e.printStackTrace();
            return "error";
        }
    }
}