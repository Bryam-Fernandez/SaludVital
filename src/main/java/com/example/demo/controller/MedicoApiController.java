package com.example.demo.controller;

import com.example.demo.Entitys.Medico;
import com.example.demo.Entitys.Role;
import com.example.demo.Entitys.User;
import com.example.demo.enums.EstadoDoctor;
import com.example.demo.Repository.RoleRepository;
import com.example.demo.Repository.UserRepository;
import com.example.demo.service.MedicoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/medicos")
@CrossOrigin(origins = "http://localhost:4200")
public class MedicoApiController {

    private final MedicoService medicoService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public MedicoApiController(MedicoService medicoService,
                               UserRepository userRepository,
                               RoleRepository roleRepository,
                               PasswordEncoder passwordEncoder) {
        this.medicoService = medicoService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public List<Medico> listar() {
        return medicoService.listarTodos();
    }

    @PostMapping
    public ResponseEntity<Medico> crear(@RequestBody Medico medico) {
        if (medico.getUsuario() == null || medico.getUsuario().getPassword() == null) {
            return ResponseEntity.badRequest().build();
        }

        if (userRepository.findByEmail(medico.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().build();
        }

        // Crear usuario
        Role rolMedico = roleRepository.findByName("ROLE_MEDICO")
                .orElseThrow(() -> new IllegalStateException("Falta el rol ROLE_MEDICO."));
        User user = medico.getUsuario();
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.getRoles().add(rolMedico);
        userRepository.save(user);

        if (medico.getEstado() == null) medico.setEstado(EstadoDoctor.ACTIVO);
        medico.setUsuario(user);

        Medico creado = medicoService.guardar(medico);
        return ResponseEntity.ok(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Medico> actualizar(@PathVariable Long id, @RequestBody Medico medico) {
        Optional<Medico> existingOpt = medicoService.buscarPorId(id);
        
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Medico existing = existingOpt.get();
        
        existing.setNombre(medico.getNombre());
        existing.setApellido(medico.getApellido());
        existing.setNumeroLicencia(medico.getNumeroLicencia());
        existing.setTelefono(medico.getTelefono());
        existing.setEmail(medico.getEmail());
        existing.setEspecialidad(medico.getEspecialidad());
        existing.setEstado(medico.getEstado());
        existing.setDisponible(medico.isDisponible());
        existing.setTarifaConsulta(medico.getTarifaConsulta());

        if (medico.getUsuario() != null && medico.getUsuario().getPassword() != null) {
            existing.getUsuario().setPassword(passwordEncoder.encode(medico.getUsuario().getPassword()));
            userRepository.save(existing.getUsuario());
        }

        Medico actualizado = medicoService.guardar(existing);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        medicoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
