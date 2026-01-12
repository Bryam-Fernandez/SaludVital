package com.example.demo.Repository;

import com.example.demo.Entitys.Receta;
import com.example.demo.Entitys.Paciente;
import com.example.demo.Entitys.Medico;
import com.example.demo.enums.EstadoReceta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RecetaRepository extends JpaRepository<Receta, Long> {
    
    // Buscar por paciente
    List<Receta> findByPacienteIdOrderByFechaEmisionDesc(Long pacienteId);
    
    // Buscar por médico
    List<Receta> findByMedicoIdOrderByFechaEmisionDesc(Long medicoId);
    
    // Buscar por tratamiento
    List<Receta> findByTratamientoIdOrderByFechaEmisionDesc(Long tratamientoId);
    
    // Buscar recetas activas por paciente
    List<Receta> findByPacienteIdAndEstado(Long pacienteId, EstadoReceta estado);
    
    // Buscar recetas vencidas
    @Query("SELECT r FROM Receta r WHERE r.paciente.id = :pacienteId AND r.fechaCaducidad < :hoy")
    List<Receta> findVencidasByPaciente(@Param("pacienteId") Long pacienteId, 
                                       @Param("hoy") LocalDate hoy);
    
    // Buscar recetas por número
    Optional<Receta> findByNumeroReceta(String numeroReceta);
    
    // Recetas próximas a vencer (últimos 7 días)
    @Query("SELECT r FROM Receta r WHERE r.paciente.id = :pacienteId " +
           "AND r.estado IN ('ACTIVA', 'DISPENSADA') " +
           "AND r.fechaCaducidad BETWEEN :hoy AND :fin")
    List<Receta> findProximasAVencer(@Param("pacienteId") Long pacienteId,
                                    @Param("hoy") LocalDate hoy,
                                    @Param("fin") LocalDate fin);
    
    // Contar recetas activas por paciente
    long countByPacienteIdAndEstadoIn(Long pacienteId, List<EstadoReceta> estados);
}