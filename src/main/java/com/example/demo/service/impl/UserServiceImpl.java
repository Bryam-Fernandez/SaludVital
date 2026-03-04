package com.example.demo.service.impl;

import com.example.demo.Entitys.Paciente;
import com.example.demo.Entitys.Role;
import com.example.demo.Entitys.User;
import com.example.demo.Repository.PacienteRepository;
import com.example.demo.Repository.RoleRepository;
import com.example.demo.Repository.UserRepository;
import com.example.demo.dto.UserDto;
import com.example.demo.enums.RoleEnum;
import com.example.demo.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final PacienteRepository pacienteRepository; 

    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder,
                           PacienteRepository pacienteRepository) { 
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.pacienteRepository = pacienteRepository; 
    }

    @Override
    @Transactional
    public void saveUser(UserDto userDto) {
        // 1. Crear usuario
        User user = new User();
        user.setName(userDto.getFirstName() + " " + userDto.getLastName());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setNumeroIdentificacion(userDto.getNumeroIdentificacion());
        user.setFechaNacimiento(userDto.getFechaNacimiento());

        // 2. Buscar o crear el rol PACIENTE
        Role role = roleRepository.findByName("ROLE_PACIENTE")
        	    .orElseGet(() -> {
        	        Role nuevoRol = new Role();
        	        nuevoRol.setName("ROLE_PACIENTE"); // 👈 CON ROLE_
        	        return roleRepository.save(nuevoRol);
        	    });

        // 3. Asignar rol al usuario
        user.getRoles().add(role);

        // 4. Guardar usuario
        User savedUser = userRepository.save(user);

        // 5. Crear paciente asociado
        Paciente paciente = new Paciente();
        paciente.setNombre(userDto.getFirstName() + " " + userDto.getLastName());
        paciente.setNumeroIdentificacion(userDto.getNumeroIdentificacion());
        paciente.setFechaNacimiento(userDto.getFechaNacimiento());
        paciente.setUsuario(user);
        pacienteRepository.save(paciente);
    }
    
    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    @Override
    public Optional<UserDto> findUserDtoByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(this::convertToDto);
    }
    
    @Override
    public UserDto convertToDto(User user) {
        return toDto(user);
    }

    public User saveUserEntity(User user) {
        return userRepository.save(user);
    }
    
    public User buscarPorEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    @Override
    public List<UserDto> findAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private UserDto toDto(User user) {
        UserDto dto = new UserDto();
        
        // Separar nombre completo en first y last name
        String fullName = user.getName() == null ? "" : user.getName().trim();
        String firstName = fullName;
        String lastName = "";
        
        int idx = fullName.lastIndexOf(' ');
        if (idx > 0) {
            firstName = fullName.substring(0, idx).trim();
            lastName = fullName.substring(idx + 1).trim();
        }
        
        dto.setFirstName(firstName);
        dto.setLastName(lastName);
        dto.setEmail(user.getEmail());
        dto.setId(user.getId());
        dto.setNumeroIdentificacion(user.getNumeroIdentificacion());
        dto.setFechaNacimiento(user.getFechaNacimiento());
        dto.setActivo(user.isActivo());
        dto.setFechaRegistro(user.getCreatedAt());
        
        // Convertir roles a lista de strings
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            List<String> roleNames = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toList());
            dto.setRoles(roleNames);
        }
        
        return dto;
    }
    
    // Métodos adicionales para el admin (mantener los que ya tienes)
    @Transactional
    public void saveUserByAdmin(UserDto userDto, RoleEnum roleEnum) {
        User user = new User();
        user.setName(userDto.getFirstName() + " " + userDto.getLastName());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setNumeroIdentificacion(userDto.getNumeroIdentificacion());
        user.setFechaNacimiento(userDto.getFechaNacimiento());

        Role role = roleRepository.findByName(roleEnum.name())
                .orElseThrow(() -> new RuntimeException("Rol " + roleEnum.name() + " no encontrado"));
        user.getRoles().add(role);

        userRepository.save(user);
    }
    
    @Transactional
    public void updateUser(Long id, UserDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        user.setName(userDto.getFirstName() + " " + userDto.getLastName());
        user.setEmail(userDto.getEmail());
        user.setNumeroIdentificacion(userDto.getNumeroIdentificacion());
        user.setFechaNacimiento(userDto.getFechaNacimiento());
        
        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }
        
        userRepository.save(user);
    }
    
    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
    
    @Transactional
    public void toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        user.setActivo(!user.isActivo());
        userRepository.save(user);
    }
    
    public UserDto findUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return toDto(user);
    }
    
    public List<UserDto> findUsersByRole(String roleName) {
        List<User> users = userRepository.findByRoleName(roleName);
        return users.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    public long countUsersByRole(String roleName) {
        return userRepository.countByRoleName(roleName);
    }
    
    public long countActiveUsers() {
        return userRepository.findByActivoTrue().size();
    }
}