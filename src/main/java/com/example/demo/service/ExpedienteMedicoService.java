package com.example.demo.service;

import com.example.demo.Entitys.ExpedienteMedico;
import java.util.List;
import java.util.Optional;

public interface ExpedienteMedicoService {
    
    // Métodos CRUD básicos
    ExpedienteMedico guardar(ExpedienteMedico expediente);
    Optional<ExpedienteMedico> obtenerPorId(Long id);
    List<ExpedienteMedico> listarTodos();
    void eliminar(Long id);
    
    // Métodos de búsqueda
    List<ExpedienteMedico> buscarPorDocumentoPaciente(String documento);
    List<ExpedienteMedico> buscarPorNombrePaciente(String nombre);
    Optional<ExpedienteMedico> buscarPorPacienteId(Long pacienteId);
    List<ExpedienteMedico> buscarPorEstado(String estado); // NUEVO
    
    // Métodos de validación y utilidad
    boolean existeExpedienteParaPaciente(Long pacienteId);
    
    // Métodos de operaciones específicas
    ExpedienteMedico actualizar(Long id, ExpedienteMedico expediente);
    ExpedienteMedico aprobarExpediente(Long id);
}