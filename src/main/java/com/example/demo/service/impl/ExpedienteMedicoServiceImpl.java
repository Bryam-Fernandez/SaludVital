package com.example.demo.service.impl;

import com.example.demo.Entitys.ExpedienteMedico;
import com.example.demo.Repository.ExpedienteMedicoRepository;
import com.example.demo.service.ExpedienteMedicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ExpedienteMedicoServiceImpl implements ExpedienteMedicoService {

    @Autowired
    private ExpedienteMedicoRepository expedienteRepository;

    @Override
    public ExpedienteMedico guardar(ExpedienteMedico expediente) {
        return expedienteRepository.save(expediente);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<ExpedienteMedico> obtenerPorId(Long id) {
        return expedienteRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpedienteMedico> listarTodos() {
        return expedienteRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpedienteMedico> buscarPorDocumentoPaciente(String documento) {
        return expedienteRepository.findByPacienteNumeroIdentificacionContainingIgnoreCase(documento);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpedienteMedico> buscarPorNombrePaciente(String nombre) {
        return expedienteRepository.findByPacienteNombreContainingIgnoreCaseOrPacienteApellidoContainingIgnoreCase(nombre, nombre);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ExpedienteMedico> buscarPorPacienteId(Long pacienteId) {
        return expedienteRepository.findByPacienteId(pacienteId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpedienteMedico> buscarPorEstado(String estado) {
        return expedienteRepository.findByEstado(estado);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeExpedienteParaPaciente(Long pacienteId) {
        return expedienteRepository.existsByPacienteId(pacienteId);
    }

    @Override
    public ExpedienteMedico actualizar(Long id, ExpedienteMedico expedienteActualizado) {
        ExpedienteMedico expedienteExistente = expedienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expediente no encontrado con ID: " + id));
        
        // Actualizar solo campos permitidos (no actualizar el paciente)
        expedienteExistente.setTipoSangre(expedienteActualizado.getTipoSangre());
        expedienteExistente.setAlergias(expedienteActualizado.getAlergias());
        expedienteExistente.setMedicamentos(expedienteActualizado.getMedicamentos());
        expedienteExistente.setEnfermedadesCronicas(expedienteActualizado.getEnfermedadesCronicas());
        expedienteExistente.setObservaciones(expedienteActualizado.getObservaciones());
        expedienteExistente.setEstado(expedienteActualizado.getEstado());
        
        return expedienteRepository.save(expedienteExistente);
    }

    @Override
    public void eliminar(Long id) {
        if (!expedienteRepository.existsById(id)) {
            throw new RuntimeException("Expediente no encontrado con ID: " + id);
        }
        expedienteRepository.deleteById(id);
    }

    @Override
    public ExpedienteMedico aprobarExpediente(Long id) {
        ExpedienteMedico expediente = expedienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expediente no encontrado con ID: " + id));
        
        expediente.setEstado("APROBADO");
        return expedienteRepository.save(expediente);
    }
}