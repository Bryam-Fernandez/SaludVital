package com.example.demo.Repository;

import com.example.demo.Entitys.*;
import com.example.demo.enums.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CitaRepository extends JpaRepository<Cita, Long> {
    
    // Métodos CORREGIDOS:
    List<Cita> findByPacienteIdAndFechaHoraBetween(Long pacienteId, LocalDateTime inicio, LocalDateTime fin);
    List<Cita> findByMedicoIdAndFechaHora(Long medicoId, LocalDateTime fechaHora);
    List<Cita> findByMedicoAndFechaHora(Medico medico, LocalDateTime fechaHora);
    boolean existsByMedicoIdAndFechaHora(Long medicoId, LocalDateTime fechaHora);
    boolean existsByPacienteIdAndFechaHoraBetween(Long pacienteId, LocalDateTime inicio, LocalDateTime fin);
    List<Cita> findByPacienteId(Long pacienteId);
    Optional<Cita> findByPacienteNumeroIdentificacion(String numeroIdentificacion);
    List<Cita> findByMedicoId(Long medicoId);
    
    // CORREGIDO: Usar fechaHora en lugar de fecha y hora separados
    List<Cita> findByPacienteOrderByFechaHoraDesc(Paciente paciente);
    
    List<Cita> findByPacienteAndEstadoIn(Paciente paciente, List<EstadoCita> estados);
    
    // CORREGIDO: Este método ya está bien
    Optional<Cita> findTopByPacienteOrderByFechaHoraDesc(Paciente paciente);
    
    // CORREGIDO: Método con @Query
    @Query("SELECT c FROM Cita c WHERE c.paciente = :paciente AND DATE(c.fechaHora) BETWEEN :inicio AND :fin ORDER BY c.fechaHora ASC")
    List<Cita> findByPacienteAndFechaBetweenOrderByFechaHoraAsc(
        @Param("paciente") Paciente paciente,
        @Param("inicio") LocalDate inicio,
        @Param("fin") LocalDate fin);

    // Nuevos métodos necesarios
    List<Cita> findByPacienteIdAndEstado(Long pacienteId, EstadoCita estado);
    List<Cita> findByMedicoIdAndFechaHoraBetween(Long medicoId, LocalDateTime inicio, LocalDateTime fin);
    
    // También puedes agregar consultas más específicas
    @Query("SELECT c FROM Cita c WHERE c.medico.id = :medicoId AND DATE(c.fechaHora) = :fecha")
    List<Cita> findByMedicoIdAndFecha(@Param("medicoId") Long medicoId, @Param("fecha") LocalDate fecha);
    
    
    // 3. Obtener la última cita de un paciente (ya está bien)
    Optional<Cita> findFirstByPacienteOrderByFechaHoraDesc(Paciente paciente);
    
    // 4. Obtener citas entre fechas para un paciente (ya está bien)
    List<Cita> findByPacienteAndFechaHoraBetweenOrderByFechaHoraAsc(
        Paciente paciente, 
        LocalDateTime fechaInicio, 
        LocalDateTime fechaFin
    );
    
    List<Cita> findByMedicoIdAndFechaHoraAfterAndEstadoNot(
            Long medicoId, 
            LocalDateTime fechaHora, 
            EstadoCita estado);
    
    // Método para buscar citas por médico y estado
    List<Cita> findByMedicoIdAndEstado(Long medicoId, EstadoCita estado);
    
    // Método para buscar citas recientes
    List<Cita> findByMedicoIdAndFechaHoraAfter(Long medicoId, LocalDateTime fechaLimite);
    
    List<Cita> findByPacienteAndEstado(Paciente paciente, EstadoCita estado);
    
    
    // 5. Método alternativo con query personalizada (ya está bien)
    @Query("SELECT c FROM Cita c WHERE c.paciente = :paciente AND c.fechaHora BETWEEN :inicio AND :fin ORDER BY c.fechaHora ASC")
    List<Cita> findCitasProximasPorPaciente(
        @Param("paciente") Paciente paciente,
        @Param("inicio") LocalDateTime inicio,
        @Param("fin") LocalDateTime fin
    );
    

    
    // 6. Obtener citas activas (PROGRAMADA y CONFIRMADA, fecha futura) (ya está bien)
    @Query("SELECT c FROM Cita c WHERE c.paciente = :paciente AND c.estado IN ('PROGRAMADA', 'CONFIRMADA') AND c.fechaHora >= CURRENT_TIMESTAMP ORDER BY c.fechaHora ASC")
    List<Cita> findCitasActivas(@Param("paciente") Paciente paciente);
}