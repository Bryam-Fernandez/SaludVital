package com.example.demo.service.impl;

import com.example.demo.Entitys.*;
import com.example.demo.Repository.*;
import com.example.demo.dto.*;
import com.example.demo.enums.*;
import com.example.demo.service.TratamientoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TratamientoServiceImpl implements TratamientoService {
    
    @Autowired
    private TratamientoRepository tratamientoRepository;
    
    @Autowired
    private RecetaRepository recetaRepository;
    
    @Autowired
    private PacienteRepository pacienteRepository;
    
    @Autowired
    private MedicoRepository medicoRepository;
    
    @Autowired
    private CitaRepository citaRepository;
    
    @Autowired
    private ItemRecetaRepository itemRecetaRepository;
    
    @Override
    public List<Tratamiento> listarTodos() {
        return tratamientoRepository.findAll();
    }
    
    @Override
    public Optional<Tratamiento> buscarPorId(Long id) {
        return tratamientoRepository.findById(id);
    }
    
    @Override
    public Tratamiento guardar(Tratamiento tratamiento) {
        return tratamientoRepository.save(tratamiento);
    }
    
    @Override
    public void eliminar(Long id) {
        tratamientoRepository.deleteById(id);
    }
    
    @Override
    public Tratamiento crearTratamientoCompleto(TratamientoDTO tratamientoDTO) {
        // Validar datos - Ahora citaId es opcional
        if (tratamientoDTO.getMedicoId() == null || tratamientoDTO.getPacienteId() == null) {
            throw new IllegalArgumentException("Datos incompletos para crear tratamiento");
        }
        
        // Buscar entidades relacionadas
        Medico medico = medicoRepository.findById(tratamientoDTO.getMedicoId())
            .orElseThrow(() -> new IllegalArgumentException("Médico no encontrado"));
        
        Paciente paciente = pacienteRepository.findById(tratamientoDTO.getPacienteId())
            .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado"));
        
        // Buscar cita si existe
        Cita cita = null;
        if (tratamientoDTO.getCitaId() != null) {
            cita = citaRepository.findById(tratamientoDTO.getCitaId())
                .orElse(null); // Opcional
        }
        
        // Crear tratamiento
        Tratamiento tratamiento = new Tratamiento();
        tratamiento.setCita(cita); // Puede ser null
        tratamiento.setMedico(medico);
        tratamiento.setPaciente(paciente);
        tratamiento.setDiagnostico(tratamientoDTO.getDiagnostico());
        tratamiento.setPlanTratamiento(tratamientoDTO.getDescripcion());
        tratamiento.setObservaciones(tratamientoDTO.getObservaciones());
        tratamiento.setFechaInicio(tratamientoDTO.getFechaInicio() != null ? 
                                  tratamientoDTO.getFechaInicio() : LocalDate.now());
        tratamiento.setFechaFin(tratamientoDTO.getFechaFin());
        tratamiento.setEstado(tratamientoDTO.getEstado() != null ? 
                             EstadoTratamiento.valueOf(tratamientoDTO.getEstado()) : 
                             EstadoTratamiento.ACTIVO);
        
        // Guardar tratamiento primero
        tratamiento = tratamientoRepository.save(tratamiento);
        
        // Crear receta si hay medicamentos
        if (tratamientoDTO.getMedicamentos() != null && !tratamientoDTO.getMedicamentos().isEmpty()) {
            crearRecetaDesdeMedicamentosDTO(tratamiento, tratamientoDTO.getMedicamentos(), cita, medico, paciente);
        }
        
        return tratamiento;
    }
    
    // NUEVO MÉTODO: Crear receta desde lista de ItemRecetaDTO
    private void crearRecetaDesdeMedicamentosDTO(Tratamiento tratamiento, 
                                               List<ItemRecetaDTO> medicamentosDTO,
                                               Cita cita, Medico medico, Paciente paciente) {
        // Crear receta
        Receta receta = new Receta();
        receta.setTratamiento(tratamiento);
        receta.setCita(cita);
        receta.setMedico(medico);
        receta.setPaciente(paciente);
        receta.setInstruccionesGenerales(tratamiento.getObservaciones());
        receta.setFechaEmision(LocalDate.now());
        receta.setFechaCaducidad(LocalDate.now().plusDays(30));
        receta.setEstado(EstadoReceta.ACTIVA);
        
        // Guardar receta
        receta = recetaRepository.save(receta);
        
        // Crear items de la receta desde medicamentosDTO
        for (ItemRecetaDTO medicamentoDTO : medicamentosDTO) {
            if (medicamentoDTO.getMedicamento() != null && !medicamentoDTO.getMedicamento().trim().isEmpty()) {
                ItemReceta item = new ItemReceta();
                item.setReceta(receta);
                item.setMedicamento(medicamentoDTO.getMedicamento());
                item.setDosis(medicamentoDTO.getDosis());
                item.setFrecuencia(medicamentoDTO.getFrecuencia());
                
                // Manejar duración
                String duracion = medicamentoDTO.getDuracion();
                if (duracion != null && !duracion.trim().isEmpty()) {
                    try {
                        // Extraer número de duración (ej: "7 días" -> 7)
                        String numero = duracion.replaceAll("\\D", "");
                        if (!numero.isEmpty()) {
                            item.setDuracionDias(Integer.parseInt(numero));
                        } else {
                            item.setDuracionDias(7); // Valor por defecto
                        }
                    } catch (NumberFormatException e) {
                        item.setDuracionDias(7); // Valor por defecto
                    }
                } else {
                    item.setDuracionDias(7); // Valor por defecto
                }
                
                item.setIndicaciones(medicamentoDTO.getIndicaciones());
                itemRecetaRepository.save(item);
            }
        }
        
        tratamiento.agregarReceta(receta);
        tratamientoRepository.save(tratamiento);
    }
    
    // MÉTODO CORREGIDO: Para usar RecetaDTO
    private Receta crearRecetaParaTratamiento(Tratamiento tratamiento, RecetaDTO recetaDTO, 
                                             Cita cita, Medico medico, Paciente paciente) {
        // Crear receta
        Receta receta = new Receta();
        receta.setTratamiento(tratamiento);
        receta.setCita(cita);
        receta.setMedico(medico);
        receta.setPaciente(paciente);
        receta.setInstruccionesGenerales(recetaDTO.getInstruccionesGenerales());
        receta.setFechaEmision(recetaDTO.getFechaEmision() != null ? 
                              recetaDTO.getFechaEmision() : LocalDate.now());
        receta.setFechaCaducidad(recetaDTO.getFechaCaducidad() != null ?
                                recetaDTO.getFechaCaducidad() : LocalDate.now().plusDays(30));
        receta.setEstado(EstadoReceta.ACTIVA);
        
        // Guardar receta
        receta = recetaRepository.save(receta);
        
        // Crear items de la receta
        if (recetaDTO.getItems() != null && !recetaDTO.getItems().isEmpty()) {
            for (ItemRecetaDTO itemDTO : recetaDTO.getItems()) {
                ItemReceta item = new ItemReceta();
                item.setReceta(receta);
                item.setMedicamento(itemDTO.getMedicamento());
                item.setDosis(itemDTO.getDosis());
                item.setFrecuencia(itemDTO.getFrecuencia());
                
                // Manejar duración
                String duracion = itemDTO.getDuracion();
                if (duracion != null && !duracion.trim().isEmpty()) {
                    try {
                        // Extraer número de duración (ej: "7 días" -> 7)
                        String numero = duracion.replaceAll("\\D", "");
                        if (!numero.isEmpty()) {
                            item.setDuracionDias(Integer.parseInt(numero));
                        } else {
                            item.setDuracionDias(7); // Valor por defecto
                        }
                    } catch (NumberFormatException e) {
                        item.setDuracionDias(7); // Valor por defecto
                    }
                } else {
                    item.setDuracionDias(7); // Valor por defecto
                }
                
                item.setIndicaciones(itemDTO.getIndicaciones());
                itemRecetaRepository.save(item);
            }
        }
        
        return receta;
    }
    
    
    @Override
    public Tratamiento agregarRecetaATratamiento(Long tratamientoId, RecetaDTO recetaDTO) {
        Tratamiento tratamiento = tratamientoRepository.findById(tratamientoId)
            .orElseThrow(() -> new IllegalArgumentException("Tratamiento no encontrado"));
        
        Receta receta = crearRecetaParaTratamiento(
            tratamiento, recetaDTO, 
            tratamiento.getCita(), tratamiento.getMedico(), tratamiento.getPaciente()
        );
        
        tratamiento.agregarReceta(receta);
        return tratamientoRepository.save(tratamiento);
    }
  
    
    private void crearRecetaDesdeMedicamentos(Tratamiento tratamiento, 
                                            List<ItemRecetaDTO> medicamentosDTO,
                                            Cita cita, Medico medico, Paciente paciente) {
        // Crear receta
        Receta receta = new Receta();
        receta.setTratamiento(tratamiento);
        receta.setCita(cita);
        receta.setMedico(medico);
        receta.setPaciente(paciente);
        receta.setInstruccionesGenerales(tratamiento.getObservaciones());
        receta.setFechaEmision(LocalDate.now());
        receta.setFechaCaducidad(LocalDate.now().plusDays(30));
        receta.setEstado(EstadoReceta.ACTIVA);
        
        // Guardar receta
        receta = recetaRepository.save(receta);
        
        // Crear items de la receta desde medicamentosDTO
        for (ItemRecetaDTO medicamentoDTO : medicamentosDTO) {
            if (medicamentoDTO.getMedicamento() != null && !medicamentoDTO.getMedicamento().trim().isEmpty()) {
                ItemReceta item = new ItemReceta();
                item.setReceta(receta);
                item.setMedicamento(medicamentoDTO.getMedicamento());
                item.setDosis(medicamentoDTO.getDosis());
                item.setFrecuencia(medicamentoDTO.getFrecuencia());
                
                // Convertir duración si es necesario
                String duracion = medicamentoDTO.getDuracion();
                if (duracion != null) {
                    try {
                        // Intenta extraer número de días
                        String[] partes = duracion.split(" ");
                        if (partes.length > 0) {
                            Integer dias = Integer.parseInt(partes[0].replaceAll("\\D", ""));
                            item.setDuracionDias(dias);
                        }
                    } catch (Exception e) {
                        item.setDuracionDias(7); // Valor por defecto
                    }
                }
                
                item.setIndicaciones(medicamentoDTO.getIndicaciones());
                itemRecetaRepository.save(item);
            }
        }
        
        tratamiento.agregarReceta(receta);
        tratamientoRepository.save(tratamiento);
    }
    
    
    @Override
    public List<Tratamiento> buscarPorPaciente(Long pacienteId) {
        return tratamientoRepository.findByPacienteIdOrderByFechaInicioDesc(pacienteId);
    }
    
    @Override
    public List<Tratamiento> buscarPorPacienteYEstado(Long pacienteId, EstadoTratamiento estado) {
        return tratamientoRepository.findByPacienteIdAndEstado(pacienteId, estado);
    }
    
    @Override
    public List<Tratamiento> buscarPorMedico(Long medicoId) {
        return tratamientoRepository.findByMedicoIdOrderByFechaCreacionDesc(medicoId);
    }
    
    @Override
    public Optional<Tratamiento> buscarPorCita(Long citaId) {
        return tratamientoRepository.findByCitaId(citaId);
    }
    
    @Override
    public Tratamiento actualizarEstado(Long tratamientoId, EstadoTratamiento estado, String observaciones) {
        Tratamiento tratamiento = tratamientoRepository.findById(tratamientoId)
            .orElseThrow(() -> new IllegalArgumentException("Tratamiento no encontrado"));
        
        tratamiento.setEstado(estado);
        if (observaciones != null && !observaciones.trim().isEmpty()) {
            tratamiento.setObservaciones(tratamiento.getObservaciones() != null ?
                tratamiento.getObservaciones() + "\n" + observaciones : observaciones);
        }
        
        return tratamientoRepository.save(tratamiento);
    }
    
    
    @Override
    public Tratamiento agregarSeguimiento(Long tratamientoId, String observaciones, Long medicoId) {
        Tratamiento tratamiento = tratamientoRepository.findById(tratamientoId)
            .orElseThrow(() -> new IllegalArgumentException("Tratamiento no encontrado"));
        
        Medico medico = medicoRepository.findById(medicoId)
            .orElseThrow(() -> new IllegalArgumentException("Médico no encontrado"));
        
        SeguimientoTratamiento seguimiento = new SeguimientoTratamiento();
        seguimiento.setTratamiento(tratamiento);
        seguimiento.setMedico(medico);
        seguimiento.setObservaciones(observaciones);
        seguimiento.setEstado(EstadoSeguimiento.REALIZADO);
        
        tratamiento.agregarSeguimiento(seguimiento);
        return tratamientoRepository.save(tratamiento);
    }
    
    @Override
    public long contarActivosPorMedico(Long medicoId) {
        return tratamientoRepository.countByMedicoIdAndEstado(medicoId, EstadoTratamiento.ACTIVO);
    }
    
    @Override
    public long contarCompletadosPorPaciente(Long pacienteId) {
        List<Tratamiento> tratamientos = tratamientoRepository
            .findByPacienteIdAndEstado(pacienteId, EstadoTratamiento.COMPLETADO);
        return tratamientos.size();
    }
    
    @Override
    public Optional<Tratamiento> obtenerTratamientoActivo(Long pacienteId) {
        return tratamientoRepository
            .findFirstByPacienteIdAndEstadoOrderByFechaInicioDesc(pacienteId, EstadoTratamiento.ACTIVO);
    }
}