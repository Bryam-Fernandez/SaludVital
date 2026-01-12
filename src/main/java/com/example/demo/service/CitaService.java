package com.example.demo.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.example.demo.Entitys.Cita;
import com.example.demo.Entitys.Paciente;
import com.example.demo.dto.CitaDTO;
import com.example.demo.enums.EstadoCita;

public interface CitaService {
    // Métodos existentes que ya tienes...
    List<Cita> listarCitas(); 
    Optional<Cita> obtenerPorId(Long id);
    Cita guardarCita(Cita cita);
    void eliminarCita(Long id);
    List<Cita> listarTodas();
    List<Cita> listarPorPaciente(Long idPaciente);
    Cita guardarConValidacion(Cita cita);
    void cancelarCita(Long idCita);
    List<Cita> obtenerCitasPorPacienteId(Long pacienteId);
    List<Cita> obtenerCitasPorUsuario(String email);
    
    // === NUEVOS MÉTODOS ===
    // Filtrado
    List<Cita> filtrarCitas(LocalDate fecha, Long medicoId, EstadoCita estado);
    List<Cita> obtenerCitasPorPacienteYEstado(Long pacienteId, EstadoCita estado);
    List<Cita> obtenerCitasPorMedicoYFecha(Long medicoId, LocalDate fecha);
    
    // Disponibilidad
    boolean validarDisponibilidadMedico(Long medicoId, LocalDateTime fechaHora);
    List<LocalDateTime> obtenerHorariosDisponibles(Long medicoId, LocalDate fecha);
    
    // CRUD adicional
    Optional<Cita> buscarPorId(Long id);
    Cita guardar(Cita cita);
    void actualizar(Long id, Cita cita);
    void confirmarCita(Long id);
    void completarCita(Long id);
    void eliminar(Long id);
    void guardarCita(CitaDTO citaDTO);
    
    // En CitaService (interfaz)
    List<Cita> obtenerCitasPorPacienteYEstado(Paciente paciente, String estadoStr);
    List<Cita> buscarCitasActivasMedico(Long medicoId);
    
    List<Cita> buscarCitasPorMedicoYEstado(Long medicoId, EstadoCita estado);
    
    List<Cita> buscarCitasRecientesMedico(Long medicoId, int dias);

    
    // === MÉTODOS NUEVOS PARA EL PANEL MÉDICO ===
    long contarPacientesUnicosPorMedico(Long medicoId);
    BigDecimal calcularIngresosMesPorMedico(Long medicoId, int year, int month);
    List<Cita> obtenerCitasHoyPorMedico(Long medicoId);
    List<Paciente> obtenerPacientesRecientesPorMedico(Long medicoId);
    List<Cita> obtenerCitasPorMedicoId(Long medicoId); 
    List<Cita> obtenerCitasPorPaciente(Paciente paciente);
    List<Cita> obtenerCitasPendientesPorPaciente(Paciente paciente);
    Optional<Cita> obtenerUltimaCita(Paciente paciente);
    List<Cita> obtenerCitasProximas(Paciente paciente, int dias);
}