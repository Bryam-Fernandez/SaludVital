package com.example.demo.controller;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.AdminDto;
import com.example.demo.dto.LoginResponseDto;
import com.example.demo.Entitys.User;
import com.example.demo.service.AdminService;
import com.example.demo.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.List;
import com.example.demo.dto.UserDto;


@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    private final AdminService adminService;
    private final UserService userService;

    public AuthRestController(AdminService adminService, UserService userService) {
        this.adminService = adminService;
        this.userService = userService;
    }

    
    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userService.findAllUsers();
        return ResponseEntity.ok(users);
    }
    
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UserDto userDto) {
        try {
            userService.saveUser(userDto);
            return ResponseEntity.ok("Usuario registrado con éxito");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body("Error al registrar el usuario: " + e.getMessage());
        }
    }


    @PostMapping("/signin") 
    public LoginResponseDto login(@RequestBody LoginRequest request) {
        return adminService.login(request.getEmail(), request.getPassword());
    }

    // DTO para recibir login
    public static class LoginRequest {
        private String email;
        private String password;
        // getters y setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
