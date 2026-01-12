package com.example.demo.service;

import java.util.Date;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.Entitys.Role;
import com.example.demo.Entitys.User;
import com.example.demo.Repository.RoleRepository;
import com.example.demo.Repository.UserRepository;
import com.example.demo.dto.AdminDto;
import com.example.demo.dto.LoginResponseDto;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.transaction.Transactional;
import java.security.Key;
import io.jsonwebtoken.security.Keys;


@Service
public class AdminService {
	private static final String JWT_SECRET = "k3Hf9vN8rXqPz6wB2sTgY4mL0aJ7cV1e"; // 32 caracteres
	private static final Key JWT_KEY = Keys.hmacShaKeyFor(JWT_SECRET.getBytes());
	    private final long JWT_EXPIRATION = 3600000; // 1 hora en ms

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder encoder;

    public AdminService(UserRepository userRepo, RoleRepository roleRepo, PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.encoder = encoder;
    }

    @Transactional
    public AdminDto create(AdminDto dto) {
        if (userRepo.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("El email ya existe");
            
        }

        Role adminRole = roleRepo.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new IllegalStateException("El rol ROLE_ADMIN no existe"));

        User u = new User();
        u.setName(dto.getName());
        u.setEmail(dto.getEmail());
        u.setPassword(encoder.encode(dto.getPassword()));
        u.addRole(adminRole);

        User saved = userRepo.save(u);

        AdminDto response = new AdminDto();
        response.setId(saved.getId());
        response.setName(saved.getName());
        response.setEmail(saved.getEmail());
        response.setPassword("********"); 
        response.setRoles(saved.getRoles().stream().map(Role::getName).toList());

        return response;   
    }
    
    @Transactional
    public List<AdminDto> findAll() {
        // Obtener todos los usuarios con rol ADMIN
        List<User> users = userRepo.findAll();
        
        // Filtrar solo los usuarios que tienen rol ADMIN
        List<User> admins = users.stream()
                .filter(user -> user.getRoles().stream()
                        .anyMatch(role -> role.getName().equals("ROLE_ADMIN")))
                .toList();
        
        // Convertir a AdminDto
        return admins.stream().map(user -> {
            AdminDto dto = new AdminDto();
            dto.setId(user.getId());
            dto.setName(user.getName());
            dto.setEmail(user.getEmail());
            dto.setPassword("********");
            dto.setRoles(user.getRoles().stream()
                    .map(Role::getName)
                    .toList());
            return dto;
        }).toList();
    }
    @Transactional
    public void delete(Long id) {
        if (!userRepo.existsById(id)) {
            throw new IllegalArgumentException("No existe admin con id " + id);
        }
        userRepo.deleteById(id);
    }
    @Transactional
    public AdminDto update(Long id, AdminDto dto) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe admin con id " + id));

        user.setName(dto.getName());
        user.setEmail(dto.getEmail());

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(encoder.encode(dto.getPassword()));
        }

        Role adminRole = roleRepo.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new IllegalStateException("El rol ROLE_ADMIN no existe"));
        user.getRoles().clear();
        user.addRole(adminRole);

        User saved = userRepo.save(user);

        AdminDto response = new AdminDto();
        response.setId(saved.getId());
        response.setName(saved.getName());
        response.setEmail(saved.getEmail());
        response.setPassword("********");
        response.setRoles(saved.getRoles().stream().map(Role::getName).toList());
        return response;
    }
    @Transactional
    public AdminDto findById(Long id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe admin con id " + id));

        AdminDto dto = new AdminDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPassword("********");
        dto.setRoles(user.getRoles().stream().map(Role::getName).toList());
        return dto;
    }
    public LoginResponseDto login(String email, String password) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if(!encoder.matches(password, user.getPassword())){
            throw new IllegalArgumentException("Contraseña incorrecta");
        }

        String token = Jwts.builder()
                .setSubject(user.getEmail())
                .claim("roles", user.getRoles().stream().map(r -> r.getName()).toList())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION))
                .signWith(JWT_KEY, SignatureAlgorithm.HS256)
                .compact();

        AdminDto dto = new AdminDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPassword("********");
        dto.setRoles(user.getRoles().stream().map(r -> r.getName()).toList());

        return new LoginResponseDto(token, dto);
    }
}