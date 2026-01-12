package com.example.demo.service.impl;

import com.example.demo.Entitys.EntradaHistorial;
import com.example.demo.Repository.EntradaHistorialRepository;
import com.example.demo.service.EntradaHistorialService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class EntradaHistorialServiceImpl implements EntradaHistorialService {

    @Autowired
    private EntradaHistorialRepository entradaRepository;

    @Override
    public void guardar(EntradaHistorial entrada) {
        entradaRepository.save(entrada);
    }

    @Override
    public List<EntradaHistorial> listarPorExpediente(Long expedienteId) {
        return entradaRepository.findByExpedienteId(expedienteId);
    }

    // Nuevos métodos
    @Override
    public List<EntradaHistorial> obtenerPorExpedienteId(Long expedienteId) {
        return entradaRepository.findByExpedienteId(expedienteId);
    }

    @Override
    public List<EntradaHistorial> listarTodas() {
        return entradaRepository.findAll();
    }

    @Override
    public Optional<EntradaHistorial> obtenerPorId(Long id) {
        return entradaRepository.findById(id);
    }

    @Override
    public List<EntradaHistorial> obtenerPorTipo(String tipo) {
        return entradaRepository.findByTipo(tipo);
    }

    @Override
    @Transactional
    public void actualizar(Long id, EntradaHistorial entradaActualizada) {
        EntradaHistorial entradaExistente = entradaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entrada no encontrada con ID: " + id));
        
        // Actualizar campos según tu entidad
        entradaExistente.setTitulo(entradaActualizada.getTitulo());
        entradaExistente.setDescripcion(entradaActualizada.getDescripcion());
        entradaExistente.setTipo(entradaActualizada.getTipo());
        entradaExistente.setNotas(entradaActualizada.getNotas());
        
        entradaRepository.save(entradaExistente);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (!entradaRepository.existsById(id)) {
            throw new RuntimeException("Entrada no encontrada con ID: " + id);
        }
        entradaRepository.deleteById(id);
    }
}