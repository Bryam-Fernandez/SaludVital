package com.example.demo.service;

import com.example.demo.Entitys.*;
import com.example.demo.dto.*;
import com.example.demo.enums.EstadoTratamiento;
import java.util.List;
import java.util.Optional;

public interface TratamientoService {
    
    // CRUD básico
    List<Tratamiento> listarTodos();
    Optional<Tratamiento> buscarPorId(Long id);
    Tratamiento guardar(Tratamiento tratamiento);
    void eliminar(Long id);
    
    // Crear tratamiento completo (con recetas)
    Tratamiento crearTratamientoCompleto(TratamientoDTO tratamientoDTO);
    
    // Buscar tratamientos por paciente
    List<Tratamiento> buscarPorPaciente(Long pacienteId);
    List<Tratamiento> buscarPorPacienteYEstado(Long pacienteId, EstadoTratamiento estado);
    
    // Buscar tratamientos por médico
    List<Tratamiento> buscarPorMedico(Long medicoId);
    
    // Buscar por cita
    Optional<Tratamiento> buscarPorCita(Long citaId);
    
    // Actualizar estado
    Tratamiento actualizarEstado(Long tratamientoId, EstadoTratamiento estado, String observaciones);
    
    // Agregar receta a tratamiento existente
    Tratamiento agregarRecetaATratamiento(Long tratamientoId, RecetaDTO recetaDTO);
    
    // Agregar seguimiento
    Tratamiento agregarSeguimiento(Long tratamientoId, String observaciones, Long medicoId);
    
    // Estadísticas
    long contarActivosPorMedico(Long medicoId);
    long contarCompletadosPorPaciente(Long pacienteId);
    
    // Obtener tratamiento actual activo de un paciente
    Optional<Tratamiento> obtenerTratamientoActivo(Long pacienteId);
}