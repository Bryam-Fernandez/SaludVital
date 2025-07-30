package com.example.demo.Repository;

import com.example.demo.Entitys.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CitaRepository extends JpaRepository<Cita, Long> {
    List<Cita> findByPacienteIdAndFechaHoraBetween(Long pacienteId, LocalDateTime inicio, LocalDateTime fin);
    List<Cita> findByMedicoIdAndFechaHora(Long medicoId, LocalDateTime fechaHora);
    List<Cita> findByPaciente(Paciente paciente);
    List<Cita> findByMedicoAndFechaHora(Medico medico, LocalDateTime fechaHora);
    boolean existsByMedicoIdAndFechaHora(Long medicoId, LocalDateTime fechaHora);
    boolean existsByPacienteIdAndFechaHoraBetween(Long pacienteId, LocalDateTime inicio, LocalDateTime fin);
}

