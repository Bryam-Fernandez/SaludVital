package com.example.demo.service.impl;

import com.example.demo.Entitys.*;
import com.example.demo.Repository.*;
import com.example.demo.dto.RecetaDTO;
import com.example.demo.dto.ItemRecetaDTO;
import com.example.demo.enums.EstadoReceta;
import com.example.demo.service.RecetaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RecetaServiceImpl implements RecetaService {
    
    @Autowired
    private RecetaRepository recetaRepository;
    
    @Autowired
    private ItemRecetaRepository itemRecetaRepository;
    
    @Autowired
    private PacienteRepository pacienteRepository;
    
    @Autowired
    private MedicoRepository medicoRepository;
    
    @Autowired
    private CitaRepository citaRepository;
    
    @Autowired
    private TratamientoRepository tratamientoRepository;
    
    @Override
    public List<Receta> listarTodas() {
        return recetaRepository.findAll();
    }
    
    @Override
    public Optional<Receta> buscarPorId(Long id) {
        return recetaRepository.findById(id);
    }
    
    @Override
    public Receta guardar(Receta receta) {
        return recetaRepository.save(receta);
    }
    
    @Override
    public void eliminar(Long id) {
        recetaRepository.deleteById(id);
    }
    
    @Override
    public Receta crearRecetaConItems(RecetaDTO recetaDTO) {
        // Validar datos
        if (recetaDTO.getPacienteId() == null || recetaDTO.getMedicoId() == null) {
            throw new IllegalArgumentException("Datos incompletos para crear receta");
        }
        
        // Buscar entidades relacionadas
        Paciente paciente = pacienteRepository.findById(recetaDTO.getPacienteId())
            .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado"));
        
        Medico medico = medicoRepository.findById(recetaDTO.getMedicoId())
            .orElseThrow(() -> new IllegalArgumentException("Médico no encontrado"));
        
        // Crear receta
        Receta receta = new Receta();
        receta.setPaciente(paciente);
        receta.setMedico(medico);
        
        // Asignar cita si existe
        if (recetaDTO.getCitaId() != null) {
            Optional<Cita> citaOpt = citaRepository.findById(recetaDTO.getCitaId());
            citaOpt.ifPresent(receta::setCita);
        }
        
        // Asignar tratamiento si existe
        if (recetaDTO.getTratamientoId() != null) {
            Optional<Tratamiento> tratamientoOpt = tratamientoRepository.findById(recetaDTO.getTratamientoId());
            tratamientoOpt.ifPresent(receta::setTratamiento);
        }
        
        receta.setInstruccionesGenerales(recetaDTO.getInstruccionesGenerales());
        receta.setFechaEmision(recetaDTO.getFechaEmision() != null ? 
                              recetaDTO.getFechaEmision() : LocalDate.now());
        receta.setFechaCaducidad(recetaDTO.getFechaCaducidad() != null ?
                                recetaDTO.getFechaCaducidad() : LocalDate.now().plusDays(30));
        receta.setEstado(EstadoReceta.ACTIVA);
        
        // Guardar receta primero
        receta = recetaRepository.save(receta);
        
        // Crear items de la receta
        if (recetaDTO.getItems() != null && !recetaDTO.getItems().isEmpty()) {
            for (ItemRecetaDTO itemDTO : recetaDTO.getItems()) {
                ItemReceta item = new ItemReceta();
                item.setReceta(receta);
                item.setMedicamento(itemDTO.getMedicamento());
                item.setDosis(itemDTO.getDosis());
                item.setFrecuencia(itemDTO.getFrecuencia());
                item.setDuracionDias(itemDTO.getDuracionDias());
                item.setIndicaciones(itemDTO.getIndicaciones());
                
                // Campos adicionales si existen
                if (itemDTO.getCantidadTotal() != null) {
                    item.setCantidadTotal(itemDTO.getCantidadTotal());
                }
                if (itemDTO.getUnidadMedida() != null) {
                    item.setUnidadMedida(itemDTO.getUnidadMedida());
                }
                if (itemDTO.getViaAdministracion() != null) {
                    item.setViaAdministracion(itemDTO.getViaAdministracion());
                }
                
                itemRecetaRepository.save(item);
            }
        }
        
        return receta;
    }
    
    @Override
    public List<Receta> buscarPorPaciente(Long pacienteId) {
        return recetaRepository.findByPacienteIdOrderByFechaEmisionDesc(pacienteId);
    }
    
    @Override
    public List<Receta> buscarPorPacienteYEstado(Long pacienteId, EstadoReceta estado) {
        return recetaRepository.findByPacienteIdAndEstado(pacienteId, estado);
    }
    
    @Override
    public List<Receta> buscarPorMedico(Long medicoId) {
        return recetaRepository.findByMedicoIdOrderByFechaEmisionDesc(medicoId);
    }
    
    @Override
    public List<Receta> buscarPorTratamiento(Long tratamientoId) {
        return recetaRepository.findByTratamientoIdOrderByFechaEmisionDesc(tratamientoId);
    }
    
    @Override
    public Optional<Receta> buscarPorNumero(String numeroReceta) {
        return recetaRepository.findByNumeroReceta(numeroReceta);
    }
    
    @Override
    public Receta actualizarEstado(Long recetaId, EstadoReceta estado) {
        Receta receta = recetaRepository.findById(recetaId)
            .orElseThrow(() -> new IllegalArgumentException("Receta no encontrada"));
        
        receta.setEstado(estado);
        return recetaRepository.save(receta);
    }
    
    @Override
    public Receta marcarComoDispensada(Long recetaId) {
        return actualizarEstado(recetaId, EstadoReceta.DISPENSADA);
    }
    
    @Override
    public boolean esRecetaVigente(Long recetaId) {
        Optional<Receta> recetaOpt = recetaRepository.findById(recetaId);
        return recetaOpt.map(Receta::isVigente).orElse(false);
    }
    
    @Override
    public List<Receta> obtenerProximasAVencer(Long pacienteId, int dias) {
        LocalDate hoy = LocalDate.now();
        LocalDate fin = hoy.plusDays(dias);
        return recetaRepository.findProximasAVencer(pacienteId, hoy, fin);
    }
    
    @Override
    public long contarActivasPorPaciente(Long pacienteId) {
        return recetaRepository.countByPacienteIdAndEstadoIn(pacienteId, 
            List.of(EstadoReceta.ACTIVA, EstadoReceta.DISPENSADA));
    }
    
    @Override
    public long contarVencidasPorPaciente(Long pacienteId) {
        List<Receta> vencidas = recetaRepository.findVencidasByPaciente(pacienteId, LocalDate.now());
        return vencidas.size();
    }
    
    // Método por compatibilidad (ya tenías obtenerPorId)
    @Override
    public Receta obtenerPorId(Long id) {
        return recetaRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Receta no encontrada"));
    }
}