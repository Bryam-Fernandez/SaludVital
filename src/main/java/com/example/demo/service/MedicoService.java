package com.example.demo.service;

import com.example.demo.Entitys.Cita;
import com.example.demo.Entitys.Medico;
import com.example.demo.Entitys.User;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

public interface MedicoService {
    
    // Métodos básicos
    List<Medico> listarTodos();
    Optional<Medico> buscarPorId(Long id);
    Medico guardar(Medico medico);
    void eliminar(Long id);
    
    // Métodos para disponibilidad
    List<Medico> listarMedicosDisponibles();
    List<Medico> buscarActivos();
    
    // Métodos de búsqueda
    Optional<Medico> buscarPorUsuario(User usuario);
    Optional<Medico> obtenerPorEmail(String email);
    Optional<Medico> buscarPorNumeroLicencia(String numeroLicencia);
    List<Medico> buscarPorEspecialidad(String especialidad);
    
    // Método para obtener médico actual (logueado)
    Medico medicoActual(Authentication auth);
    List<Cita> obtenerCitasPorMedicoId(Long medicoId);
}