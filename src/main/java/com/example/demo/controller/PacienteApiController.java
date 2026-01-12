package com.example.demo.controller;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.example.demo.Entitys.Paciente;
import com.example.demo.Entitys.Role;
import com.example.demo.Entitys.User;
import com.example.demo.dto.PacienteDTO;
import com.example.demo.dto.UserDto;
import com.example.demo.service.PacienteService;
import com.example.demo.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/pacientes")
public class PacienteApiController {

    private final PacienteService pacienteService;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PacienteApiController(UserService userService, PacienteService pacienteService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.pacienteService = pacienteService;
        this.passwordEncoder = passwordEncoder;
    }

    // ENDPOINT CRÍTICO: Para búsqueda desde el modal del expediente
    @GetMapping("/buscar")
    public ResponseEntity<?> buscarPacientes(@RequestParam String query) {
        try {
            List<Paciente> pacientes = pacienteService.buscarPorDocumentoONombre(query);
            
            // Convertir a formato simple para el frontend
            List<Map<String, Object>> resultado = new ArrayList<>();
            
            for (Paciente paciente : pacientes) {
                Map<String, Object> pacienteMap = new HashMap<>();
                pacienteMap.put("id", paciente.getId());
                pacienteMap.put("nombres", paciente.getNombre()); // Tu entidad usa 'nombre'
                pacienteMap.put("apellidos", paciente.getApellido()); // Tu entidad usa 'apellido'
                pacienteMap.put("dni", paciente.getNumeroIdentificacion());
                pacienteMap.put("telefono", paciente.getTelefono() != null ? paciente.getTelefono() : "N/A");
                pacienteMap.put("email", paciente.getEmail() != null ? paciente.getEmail() : "N/A");
                pacienteMap.put("tieneExpediente", paciente.getExpediente() != null);
                
                resultado.add(pacienteMap);
            }
            
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al buscar pacientes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // También puedes mantener el endpoint específico por documento
    @GetMapping("/buscar-documento")
    public ResponseEntity<?> buscarPorDocumentoApi(@RequestParam String documento) {
        try {
            Paciente paciente = pacienteService.buscarPorDocumento(documento);
            
            if (paciente == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Paciente no encontrado"));
            }
            
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("id", paciente.getId());
            resultado.put("nombre", paciente.getNombre());
            resultado.put("apellido", paciente.getApellido());
            resultado.put("dni", paciente.getNumeroIdentificacion());
            resultado.put("telefono", paciente.getTelefono());
            resultado.put("email", paciente.getEmail());
            resultado.put("tieneExpediente", paciente.getExpediente() != null);
            
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    // Mantén tus otros endpoints existentes
    @GetMapping("/all")
    public List<PacienteDTO> listarPacientes() {
        return pacienteService.listarTodosDTO(); 
    }

    @GetMapping("/{id}")
    public ResponseEntity<PacienteDTO> obtenerPaciente(@PathVariable Long id) {
        return pacienteService.buscarPorId(id)
                .map(p -> new PacienteDTO(
                    p.getId(),
                    p.getNombre(),
                    p.getNumeroIdentificacion(),
                    p.getFechaNacimiento(),
                    p.getUsuario() != null ? p.getUsuario().getName() : null
                ))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/buscar/{documento}")
    public ResponseEntity<PacienteDTO> buscarPorDocumentoPath(@PathVariable String documento) {
        Paciente paciente = pacienteService.buscarPorDocumento(documento);
        if (paciente == null) {
            return ResponseEntity.notFound().build();
        }
        PacienteDTO dto = new PacienteDTO(
            paciente.getId(),
            paciente.getNombre(),
            paciente.getNumeroIdentificacion(),
            paciente.getFechaNacimiento(),
            paciente.getUsuario() != null ? paciente.getUsuario().getName() : null
        );
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<PacienteDTO> crearPaciente(@RequestBody UserDto userDto) {
        userService.saveUser(userDto);
        Paciente pacienteGuardado = pacienteService.buscarPorDocumento(userDto.getNumeroIdentificacion());

        PacienteDTO dto = new PacienteDTO(
                pacienteGuardado.getId(),
                pacienteGuardado.getNombre(),
                pacienteGuardado.getNumeroIdentificacion(),
                pacienteGuardado.getFechaNacimiento(),
                pacienteGuardado.getUsuario().getName() 
        );

        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Paciente> actualizarPaciente(@PathVariable Long id, @RequestBody Paciente paciente) {
        try {
            Paciente actualizado = pacienteService.actualizar(id, paciente);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPaciente(@PathVariable Long id) {
        pacienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}