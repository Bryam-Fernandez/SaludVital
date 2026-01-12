package com.example.demo.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.Entitys.Cita;
import com.example.demo.Entitys.Medico;
import com.example.demo.Entitys.Paciente;
import com.example.demo.Entitys.User;
import com.example.demo.Repository.CitaRepository;
import com.example.demo.Repository.MedicoRepository;
import com.example.demo.Repository.PacienteRepository;
import com.example.demo.Repository.UserRepository;
import com.example.demo.dto.CitaDTO;
import com.example.demo.enums.EstadoCita;
import com.example.demo.service.CitaService;



@Service
@Transactional
public class CitaServiceImpl implements CitaService {

	   private final CitaRepository citaRepository;
	    private final PacienteRepository pacienteRepository;
	    private final MedicoRepository medicoRepository;
	    private final UserRepository userRepository;

	    // INYECCIÓN POR CONSTRUCTOR (MEJOR PRÁCTICA)
	    public CitaServiceImpl(CitaRepository citaRepository,
	                          PacienteRepository pacienteRepository,
	                          MedicoRepository medicoRepository,
	                          UserRepository userRepository) {
	        this.citaRepository = citaRepository;
	        this.pacienteRepository = pacienteRepository;
	        this.medicoRepository = medicoRepository;
	        this.userRepository = userRepository;
	    }

    @Override
    public void guardarCita(CitaDTO citaDTO) {
        // Crear nueva entidad Cita a partir del DTO
        Cita cita = new Cita();
        
        // Buscar y asignar paciente
        Optional<Paciente> pacienteOpt = pacienteRepository.findById(citaDTO.getPacienteId());
        if (pacienteOpt.isEmpty()) {
            throw new IllegalArgumentException("Paciente no encontrado con ID: " + citaDTO.getPacienteId());
        }
        cita.setPaciente(pacienteOpt.get());
        
        // Buscar y asignar médico
        Optional<Medico> medicoOpt = medicoRepository.findById(citaDTO.getMedicoId());
        if (medicoOpt.isEmpty()) {
            throw new IllegalArgumentException("Médico no encontrado con ID: " + citaDTO.getMedicoId());
        }
        cita.setMedico(medicoOpt.get());
        
        // Asignar fecha y hora
        cita.setFechaHora(citaDTO.getFechaHora());
        
        // Asignar motivo
        cita.setMotivo(citaDTO.getMotivo());
        
        // Asignar tarifa
        cita.setTarifaAplicada(citaDTO.getTarifaAplicada());
        
        // CORRECCIÓN: Asignar estado (convertir String a enum)
        if (citaDTO.getEstado() != null) {
            try {
                // Convertir el string a mayúsculas para coincidir con el enum
                String estadoStr = citaDTO.getEstado().toUpperCase();
                
                // Mapear valores incorrectos si es necesario
                if (estadoStr.equals("CONFIRMED")) estadoStr = "CONFIRMADA";
                if (estadoStr.equals("PENDING")) estadoStr = "PENDIENTE";
                if (estadoStr.equals("CANCELLED")) estadoStr = "CANCELADA";
                if (estadoStr.equals("COMPLETED")) estadoStr = "COMPLETADA";
                
                EstadoCita estado = EstadoCita.valueOf(estadoStr);
                cita.setEstado(estado);
            } catch (IllegalArgumentException e) {
                // Si hay error en la conversión, usar valor por defecto
                cita.setEstado(EstadoCita.PROGRAMADA);
            }
        } else {
            cita.setEstado(EstadoCita.PROGRAMADA);
        }
        
        // Asignar notas (si existen)
        if (citaDTO.getNotas() != null && !citaDTO.getNotas().isEmpty()) {
            cita.setNotas(citaDTO.getNotas());
        }
        
        // Validar disponibilidad del médico
        if (!validarDisponibilidadMedico(citaDTO.getMedicoId(), citaDTO.getFechaHora())) {
            throw new IllegalArgumentException("El médico no está disponible en el horario seleccionado");
        }
        
        // Guardar la cita
        citaRepository.save(cita);
    }
    
 // En CitaServiceImpl.java - Agrega este método:   
    @Override
    public List<Cita> obtenerCitasPorMedicoId(Long medicoId) {
        try {
            // Si tu repositorio tiene el método findByMedicoId, úsalo
            return citaRepository.findByMedicoId(medicoId);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    public List<Cita> obtenerCitasPorPaciente(Paciente paciente) {
    	  return citaRepository.findByPacienteOrderByFechaHoraDesc(paciente);
    }
    public LocalDate getFecha(Cita cita) {
        return cita.getFechaHora().toLocalDate();
    }
    
    public LocalTime getHora(Cita cita) {
        return cita.getFechaHora().toLocalTime();
    }
    
    public List<Cita> obtenerCitasPendientesPorPaciente(Paciente paciente) {
        return citaRepository.findByPacienteAndEstadoIn(paciente, 
            Arrays.asList(EstadoCita.PENDIENTE, EstadoCita.CONFIRMADA));
    }
    
    public Optional<Cita> obtenerUltimaCita(Paciente paciente) {
    	   return citaRepository.findTopByPacienteOrderByFechaHoraDesc(paciente);
    }
    
    public List<Cita> obtenerCitasProximas(Paciente paciente, int dias) {
        LocalDate hoy = LocalDate.now();
        LocalDate fechaLimite = hoy.plusDays(dias);
        return citaRepository.findByPacienteAndFechaBetweenOrderByFechaHoraAsc(
                paciente, hoy, fechaLimite);
    }
    
    // ===== MÉTODOS PARA EL PANEL MÉDICO =====
    
    @Override
    public long contarPacientesUnicosPorMedico(Long medicoId) {
        try {
            List<Cita> citas = citaRepository.findByMedicoId(medicoId);
            Set<Long> pacientesIds = new HashSet<>();
            
            for (Cita cita : citas) {
                if (cita.getPaciente() != null && cita.getPaciente().getId() != null) {
                    pacientesIds.add(cita.getPaciente().getId());
                }
            }
            
            return pacientesIds.size();
                
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }
    
    @Override
    public BigDecimal calcularIngresosMesPorMedico(Long medicoId, int year, int month) {
        try {
            LocalDate startDate = LocalDate.of(year, month, 1);
            LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
            
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
            
            // Obtener citas del médico en el rango de fechas
            List<Cita> citas = citaRepository.findByMedicoIdAndFechaHoraBetween(
                medicoId, startDateTime, endDateTime);
            
            BigDecimal total = BigDecimal.ZERO;
            for (Cita cita : citas) {
                if (cita.getTarifaAplicada() != null && 
                    (cita.getEstado() == EstadoCita.CONFIRMADA || 
                     cita.getEstado() == EstadoCita.COMPLETADA)) {
                    total = total.add(cita.getTarifaAplicada());
                }
            }
            
            return total;
            
        } catch (Exception e) {
            e.printStackTrace();
            return BigDecimal.ZERO;
        }
    }
    
    @Override
    public List<Cita> obtenerCitasHoyPorMedico(Long medicoId) {
        try {
            LocalDate hoy = LocalDate.now();
            LocalDateTime inicioDia = hoy.atStartOfDay();
            LocalDateTime finDia = hoy.atTime(23, 59, 59);
            
            return citaRepository.findByMedicoIdAndFechaHoraBetween(
                medicoId, inicioDia, finDia);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<Paciente> obtenerPacientesRecientesPorMedico(Long medicoId) {
        try {
            // Obtener citas de los últimos 30 días
            LocalDate fechaLimite = LocalDate.now().minusDays(30);
            LocalDateTime inicio = fechaLimite.atStartOfDay();
            LocalDateTime fin = LocalDateTime.now();
            
            List<Cita> citasRecientes = citaRepository.findByMedicoIdAndFechaHoraBetween(
                medicoId, inicio, fin);
            
            // Obtener pacientes únicos ordenados por fecha de cita más reciente
            Set<Long> pacientesIds = new HashSet<>();
            List<Paciente> pacientesRecientes = new ArrayList<>();
            
            // Ordenar citas por fecha descendente (más reciente primero)
            citasRecientes.sort((c1, c2) -> c2.getFechaHora().compareTo(c1.getFechaHora()));
            
            for (Cita cita : citasRecientes) {
                if (cita.getPaciente() != null && 
                    cita.getPaciente().getId() != null && 
                    !pacientesIds.contains(cita.getPaciente().getId())) {
                    
                    pacientesIds.add(cita.getPaciente().getId());
                    pacientesRecientes.add(cita.getPaciente());
                    
                    // Limitar a 5 pacientes
                    if (pacientesRecientes.size() >= 5) {
                        break;
                    }
                }
            }
            
            return pacientesRecientes;
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    // Métodos existentes...
    @Override
    public List<Cita> listarCitas() {
        return citaRepository.findAll();
    }

    @Override
    public Optional<Cita> obtenerPorId(Long id) {
        return citaRepository.findById(id);
    }

    @Override
    public Cita guardarCita(Cita cita) {
        return citaRepository.save(cita);
    }

    @Override
    public void eliminarCita(Long id) {	
        citaRepository.deleteById(id);
    }
    


    // En CitaServiceImpl
    public List<Cita> obtenerCitasPorPacienteYEstado(Paciente paciente, String estadoStr) {
        try {
            EstadoCita estado = EstadoCita.valueOf(estadoStr.toUpperCase());
            return citaRepository.findByPacienteAndEstado(paciente, estado);
        } catch (IllegalArgumentException e) {
            return obtenerCitasPorPaciente(paciente);
        }
    }

    @Override
    public List<Cita> obtenerCitasPorUsuario(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return Collections.emptyList();
        }
        User user = userOpt.get();

        Optional<Paciente> pacienteOpt = pacienteRepository.findByUsuario(user);
        if (pacienteOpt.isEmpty()) {
            return Collections.emptyList();
        }
        Paciente paciente = pacienteOpt.get();

        return citaRepository.findByPacienteId(paciente.getId());
    }

    @Override
    public List<Cita> listarTodas() {
        return citaRepository.findAll();
    }

    @Override
    public List<Cita> listarPorPaciente(Long idPaciente) {
        return citaRepository.findByPacienteId(idPaciente);
    }

    @Override
    public Cita guardarConValidacion(Cita cita) {
        // Lógica de validación aquí
        return citaRepository.save(cita);
    }

    @Override
    public void cancelarCita(Long idCita) {
        citaRepository.findById(idCita).ifPresent(cita -> {
            cita.setEstado(EstadoCita.CANCELADA);
            citaRepository.save(cita);
        });
    }

    @Override
    public List<Cita> obtenerCitasPorPacienteId(Long pacienteId) {
        return citaRepository.findByPacienteId(pacienteId);
    }

    @Override
    public List<Cita> filtrarCitas(LocalDate fecha, Long medicoId, EstadoCita estado) {
        List<Cita> citas = citaRepository.findAll();
        
        return citas.stream()
            .filter(cita -> 
                (fecha == null || cita.getFechaHora().toLocalDate().equals(fecha)) &&
                (medicoId == null || cita.getMedico().getId().equals(medicoId)) &&
                (estado == null || cita.getEstado() == estado)
            )
            .collect(Collectors.toList());
    }

    @Override
    public List<Cita> obtenerCitasPorPacienteYEstado(Long pacienteId, EstadoCita estado) {
        return citaRepository.findByPacienteIdAndEstado(pacienteId, estado);
    }

    @Override
    public List<Cita> obtenerCitasPorMedicoYFecha(Long medicoId, LocalDate fecha) {
        return citaRepository.findByMedicoIdAndFechaHoraBetween(
            medicoId, 
            fecha.atStartOfDay(), 
            fecha.atTime(LocalTime.MAX)
        );
    }

    @Override
    public boolean validarDisponibilidadMedico(Long medicoId, LocalDateTime fechaHora) {
        // Verificar si el médico existe
        Optional<Medico> medicoOpt = medicoRepository.findById(medicoId);
        if (medicoOpt.isEmpty()) {
            return false;
        }
        
        // Verificar si el médico está disponible
        Medico medico = medicoOpt.get();
        if (!medico.isDisponible()) {
            return false;
        }
        
        // Verificar si el médico ya tiene una cita en ese horario
        LocalDateTime inicio = fechaHora.minusMinutes(29); // 30 minutos antes
        LocalDateTime fin = fechaHora.plusMinutes(29); // 30 minutos después
        
        List<Cita> citasExistentes = citaRepository.findByMedicoIdAndFechaHoraBetween(
            medicoId, inicio, fin);
        
        return citasExistentes.isEmpty();
    }

    @Override
    public List<LocalDateTime> obtenerHorariosDisponibles(Long medicoId, LocalDate fecha) {
        List<LocalDateTime> horariosDisponibles = new ArrayList<>();
        LocalDateTime inicioDia = fecha.atTime(8, 0); // Empieza a las 8 AM
        LocalDateTime finDia = fecha.atTime(18, 0); // Termina a las 6 PM
        
        // Generar horarios cada 30 minutos
        LocalDateTime horario = inicioDia;
        while (horario.isBefore(finDia)) {
            if (validarDisponibilidadMedico(medicoId, horario)) {
                horariosDisponibles.add(horario);
            }
            horario = horario.plusMinutes(30);
        }
        
        return horariosDisponibles;
    }

    @Override
    public Optional<Cita> buscarPorId(Long id) {
        return citaRepository.findById(id);
    }

    @Override
    public Cita guardar(Cita cita) {
        return citaRepository.save(cita);
    }

    @Override
    public void actualizar(Long id, Cita citaActualizada) {
        citaRepository.findById(id).ifPresent(cita -> {
            cita.setFechaHora(citaActualizada.getFechaHora());
            cita.setMedico(citaActualizada.getMedico());
            cita.setEstado(citaActualizada.getEstado());
            cita.setMotivo(citaActualizada.getMotivo());
            cita.setNotas(citaActualizada.getNotas());
            citaRepository.save(cita);
        });
    }

    @Override
    public void confirmarCita(Long id) {
        citaRepository.findById(id).ifPresent(cita -> {
            cita.setEstado(EstadoCita.CONFIRMADA);
            citaRepository.save(cita);
        });
    }

    @Override
    public void completarCita(Long id) {
        citaRepository.findById(id).ifPresent(cita -> {
            cita.setEstado(EstadoCita.COMPLETADA);
            citaRepository.save(cita);
        });
    }

    @Override
    public void eliminar(Long id) {
        citaRepository.deleteById(id);
    }

 // En CitaServiceImpl.java, al final de la clase

    @Override
    public List<Cita> buscarCitasActivasMedico(Long medicoId) {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime semanaAtras = ahora.minusDays(7);
        
        // Buscar citas de los últimos 7 días y futuras que no estén canceladas
        return citaRepository.findByMedicoIdAndFechaHoraAfterAndEstadoNot(
            medicoId, 
            semanaAtras, 
            EstadoCita.CANCELADA
        );
    }

    @Override
    public List<Cita> buscarCitasPorMedicoYEstado(Long medicoId, EstadoCita estado) {
        return citaRepository.findByMedicoIdAndEstado(medicoId, estado);
    }

    @Override
    public List<Cita> buscarCitasRecientesMedico(Long medicoId, int dias) {
        LocalDateTime desde = LocalDateTime.now().minusDays(dias);
        return citaRepository.findByMedicoIdAndFechaHoraAfter(medicoId, desde);
    }
}