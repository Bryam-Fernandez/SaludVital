package com.example.demo.service.impl;

import com.example.demo.Entitys.Cita;
import com.example.demo.Entitys.Medico;
import com.example.demo.Entitys.Role;
import com.example.demo.Entitys.User;
import com.example.demo.enums.EstadoDoctor; // IMPORTANTE: Agregar este import
import com.example.demo.Repository.MedicoRepository;
import com.example.demo.Repository.RoleRepository;
import com.example.demo.Repository.UserRepository;
import com.example.demo.service.MedicoService;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MedicoServiceImp implements MedicoService {

    private final MedicoRepository medicoRepository; // Usa solo uno
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // Constructor con solo un MedicoRepository
    public MedicoServiceImp(MedicoRepository medicoRepository,
                            UserRepository userRepository,
                            RoleRepository roleRepository,
                            PasswordEncoder passwordEncoder) {
        this.medicoRepository = medicoRepository; // Solo este
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<Medico> listarTodos() {
        return medicoRepository.findAllByOrderByApellidoAscNombreAsc();
    }

    @Override
    public Optional<Medico> buscarPorUsuario(User usuario) {
        return medicoRepository.findByUsuario(usuario);
    }
    
    @Override
    public List<Medico> listarMedicosDisponibles() {
        // Usar query del repository si existe, o filtrar manualmente
        try {
            return medicoRepository.findMedicosActivosYDisponibles();
        } catch (Exception e) {
            // Fallback: filtrar manualmente
            return medicoRepository.findAll().stream()
                    .filter(medico -> medico.getEstado() != null && 
                                     medico.getEstado().equals(EstadoDoctor.ACTIVO) && 
                                     medico.isDisponible())
                    .collect(Collectors.toList());
        }
    }
    
    @Override
    public List<Medico> buscarActivos() {
        return medicoRepository.findByEstado(EstadoDoctor.ACTIVO);
    }

    @Override
    public Optional<Medico> buscarPorId(Long id) {
        return medicoRepository.findById(id);  
    }
    
    @Override
    @Transactional
    public Medico guardar(Medico medico) {
        // Normalizar email
        String email = medico.getEmail().trim().toLowerCase();

        // Verificar si el usuario ya existe
        Optional<User> usuarioExistente = userRepository.findByEmail(email);
        User usuario;

        if (usuarioExistente.isPresent()) {
            // Si existe, usarlo
            usuario = usuarioExistente.get();
        } else {
            // Si no existe, crear nuevo usuario
            usuario = new User();
            usuario.setName(medico.getNombre() + " " + medico.getApellido());
            usuario.setEmail(email);
            
            // IMPORTANTE: Verificar si el médico tiene usuario con contraseña
            if (medico.getUsuario() != null && medico.getUsuario().getPassword() != null) {
                usuario.setPassword(passwordEncoder.encode(medico.getUsuario().getPassword()));
            } else {
                // Contraseña por defecto si no se proporciona
                usuario.setPassword(passwordEncoder.encode("Medico123"));
            }

            Role rolMedico = roleRepository.findByName("ROLE_MEDICO")
                    .orElseThrow(() -> new RuntimeException("Rol MEDICO no encontrado"));
            usuario.setRoles(Arrays.asList(rolMedico));

            userRepository.save(usuario);
        }

        // Asociar usuario al médico
        medico.setUsuario(usuario);
        medico.setEmail(email); // Asegurar email normalizado

        // Guardar médico
        return medicoRepository.save(medico);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        medicoRepository.deleteById(id);
    }

    @Override
    public Optional<Medico> obtenerPorEmail(String email) {
        return medicoRepository.findByEmail(email);
    }

    @Override
    public Medico medicoActual(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            return null;
        }
        return obtenerPorEmail(auth.getName()).orElse(null);
    }
    
    // Métodos adicionales que podrías necesitar
    @Override
    public List<Medico> buscarPorEspecialidad(String especialidad) {
        return medicoRepository.findByEspecialidadAndEstado(especialidad, EstadoDoctor.ACTIVO);
    }
    
    @Override
    public Optional<Medico> buscarPorNumeroLicencia(String numeroLicencia) {
        return medicoRepository.findByNumeroLicencia(numeroLicencia);
    }

	@Override
	public List<Cita> obtenerCitasPorMedicoId(Long medicoId) {
		// TODO Auto-generated method stub
		return null;
	}
}