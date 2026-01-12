package com.example.demo.service;

import com.example.demo.Entitys.EntradaHistorial;
import java.util.List;
import java.util.Optional;

public interface EntradaHistorialService {
    
    // Métodos que YA tienes en la implementación
    void guardar(EntradaHistorial entrada);
    List<EntradaHistorial> listarPorExpediente(Long expedienteId);
    
    // Métodos NUEVOS que agregaste en la implementación pero 
    // probablemente NO están en la interfaz
    List<EntradaHistorial> obtenerPorExpedienteId(Long expedienteId);
    List<EntradaHistorial> listarTodas();
    Optional<EntradaHistorial> obtenerPorId(Long id);
    List<EntradaHistorial> obtenerPorTipo(String tipo);
    void actualizar(Long id, EntradaHistorial entradaActualizada);
    void eliminar(Long id);
}