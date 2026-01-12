package com.example.demo.service.impl;

import com.example.demo.Entitys.Paciente;
import com.example.demo.Entitys.Tratamiento;
import com.example.demo.Entitys.User;
import com.example.demo.Repository.PacienteRepository;
import com.example.demo.Repository.TratamientoRepository;
import com.example.demo.dto.PacienteDTO;
import com.example.demo.service.ExpedienteMedicoService;
import com.example.demo.service.PacienteService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PacienteServiceImpl implements PacienteService {

    @Autowired
    private PacienteRepository pacienteRepository;
    
    @Autowired
    private TratamientoRepository tratamientoRepository;
    
    @Autowired
    private ExpedienteMedicoService expedienteMedicoService; // Necesitas esto

    // ... otros métodos que ya tengas ...

    @Override
    public List<Paciente> listarPacientesSinExpediente() {
        // Obtener todos los pacientes
        List<Paciente> todosPacientes = pacienteRepository.findAll();
        
        // Filtrar los que NO tienen expediente
        return todosPacientes.stream()
                .filter(paciente -> {
                    // Verificar si el paciente tiene expediente
                    return !expedienteMedicoService.existeExpedienteParaPaciente(paciente.getId());
                })
                .collect(Collectors.toList());
    }
    @Override
    public List<Paciente> findAll() {
        return pacienteRepository.findAll();
    }
    
    @Override
    public List<Paciente> buscarTodos() {
        return pacienteRepository.findAll();
    }
    
    @Override
    public List<Paciente> buscarPorMedico(Long medicoId) {
        // Usa el método que SÍ existe: findByMedicoIdOrderByFechaCreacionDesc
        List<Tratamiento> tratamientos = tratamientoRepository.findByMedicoIdOrderByFechaCreacionDesc(medicoId);
        
        return tratamientos.stream()
            .map(Tratamiento::getPaciente)
            .distinct()
            .collect(Collectors.toList());
    }

    // Métodos que ya tienes (solo como referencia)
    @Override
    public List<Paciente> listarTodos() {
        return pacienteRepository.findAll();
    }
    
    @Override
    public boolean existePorId(Long id) {
        return pacienteRepository.existsById(id);
    }

    @Override
    public Optional<Paciente> buscarPorId(Long id) {
        return pacienteRepository.findById(id);
    }

    public List<Paciente> buscarPorDocumentoONombre1(String query) {
        return pacienteRepository.buscarPorDocumentoONombre(query);
    }
    


  

    @Override
    public Paciente guardar(Paciente paciente) {
        return pacienteRepository.save(paciente);
    }

    @Override
    @Transactional
    public Paciente actualizar(Long id, Paciente paciente) {
        Paciente pacienteExistente = pacienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
        
        // Actualizar campos
        pacienteExistente.setNombre(paciente.getNombre());
        pacienteExistente.setApellido(paciente.getApellido());
        pacienteExistente.setNumeroIdentificacion(paciente.getNumeroIdentificacion());
        pacienteExistente.setFechaNacimiento(paciente.getFechaNacimiento());
        pacienteExistente.setGenero(paciente.getGenero());
        pacienteExistente.setTelefono(paciente.getTelefono());
        pacienteExistente.setDireccion(paciente.getDireccion());
        pacienteExistente.setEmail(paciente.getEmail());
        
        return pacienteRepository.save(pacienteExistente);
    }

    @Override
    public void eliminar(Long id) {
        pacienteRepository.deleteById(id);
    }

    @Override
    public List<PacienteDTO> listarTodosDTO() {
        return pacienteRepository.findAll().stream()
                .map(p -> new PacienteDTO(
                    p.getId(),
                    p.getNombre(),
                    p.getNumeroIdentificacion(),
                    p.getFechaNacimiento(),
                    p.getUsuario() != null ? p.getUsuario().getEmail() : "Sin usuario"
                ))
                .collect(Collectors.toList());
    }

    @Override
    public Paciente buscarPorDocumento(String numeroIdentificacion) {
        return pacienteRepository.findByNumeroIdentificacion(numeroIdentificacion)
                .orElse(null);
    }
    
    @Override
    public List<Paciente> buscarPorDocumentoONombre(String query) {
        return pacienteRepository.buscarPorDocumentoONombre(query);
    }

    @Override
    @Transactional
    public void actualizarPaciente(Paciente paciente) {
        pacienteRepository.save(paciente);
    }

    @Override
    public Optional<Paciente> buscarPorUsuario(User user) {
        return pacienteRepository.findByUsuario(user);
    }

    @Override
    public Optional<Paciente> buscarPorUsuarioEmail(String email) {
        return pacienteRepository.findByUsuarioEmail(email);
    }
}