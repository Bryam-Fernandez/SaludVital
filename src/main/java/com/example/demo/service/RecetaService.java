package com.example.demo.service;

import com.example.demo.Entitys.Receta;
import com.example.demo.dto.RecetaDTO;
import com.example.demo.enums.EstadoReceta;

import java.util.List;
import java.util.Optional;

public interface RecetaService {
    
    // CRUD básico
    List<Receta> listarTodas();
    Optional<Receta> buscarPorId(Long id);
    Receta guardar(Receta receta);
    void eliminar(Long id);
    
    // Métodos que necesitas para el RecetaController
    Receta crearRecetaConItems(RecetaDTO recetaDTO);
    
    // Buscar recetas por paciente
    List<Receta> buscarPorPaciente(Long pacienteId);
    List<Receta> buscarPorPacienteYEstado(Long pacienteId, EstadoReceta estado);
    
    
    // Buscar recetas por médico
    List<Receta> buscarPorMedico(Long medicoId);
    
    // Buscar recetas por tratamiento
    List<Receta> buscarPorTratamiento(Long tratamientoId);
    
    // Buscar por número de receta
    Optional<Receta> buscarPorNumero(String numeroReceta);
    
    // Actualizar estado de receta
    Receta actualizarEstado(Long recetaId, EstadoReceta estado);
    
    // Marcar receta como dispensada (cuando se entrega en farmacia)
    Receta marcarComoDispensada(Long recetaId);
    
    // Verificar si una receta está vigente
    boolean esRecetaVigente(Long recetaId);
    
    // Obtener recetas próximas a vencer (últimos 7 días)
    List<Receta> obtenerProximasAVencer(Long pacienteId, int dias);
    
    // Estadísticas
    long contarActivasPorPaciente(Long pacienteId);
    long contarVencidasPorPaciente(Long pacienteId);
    
    // Método existente que ya tenías (por compatibilidad)
    Receta obtenerPorId(Long id);
}