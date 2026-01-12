package com.example.demo.Repository;

import com.example.demo.Entitys.Tratamiento;
import com.example.demo.Entitys.Paciente;
import com.example.demo.Entitys.Medico;
import com.example.demo.Entitys.Cita;
import com.example.demo.enums.EstadoTratamiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TratamientoRepository extends JpaRepository<Tratamiento, Long> {
    
    // Buscar por paciente
    List<Tratamiento> findByPacienteIdOrderByFechaInicioDesc(Long pacienteId);
    
    // Buscar por médico
    List<Tratamiento> findByMedicoIdOrderByFechaCreacionDesc(Long medicoId);
    
    // Buscar por cita
    Optional<Tratamiento> findByCitaId(Long citaId);
    
    // Buscar tratamientos activos por paciente
    List<Tratamiento> findByPacienteIdAndEstado(Long pacienteId, EstadoTratamiento estado);
    
    // Buscar tratamientos por rango de fechas
    List<Tratamiento> findByFechaInicioBetween(LocalDate inicio, LocalDate fin);
    
    // Buscar tratamientos por paciente y estado
    @Query("SELECT t FROM Tratamiento t WHERE t.paciente = :paciente AND t.estado = :estado ORDER BY t.fechaInicio DESC")
    List<Tratamiento> findByPacienteAndEstado(@Param("paciente") Paciente paciente, 
                                             @Param("estado") EstadoTratamiento estado);
    
    // Contar tratamientos activos por médico
    long countByMedicoIdAndEstado(Long medicoId, EstadoTratamiento estado);
    
    // Obtener último tratamiento activo de un paciente
    Optional<Tratamiento> findFirstByPacienteIdAndEstadoOrderByFechaInicioDesc(Long pacienteId, EstadoTratamiento estado);
}