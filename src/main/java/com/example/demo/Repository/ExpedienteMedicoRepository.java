package com.example.demo.Repository;

import com.example.demo.Entitys.ExpedienteMedico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExpedienteMedicoRepository extends JpaRepository<ExpedienteMedico, Long> {
    
    // Búsqueda por documento del paciente
    List<ExpedienteMedico> findByPacienteNumeroIdentificacionContainingIgnoreCase(String numeroIdentificacion);
    
    // Búsqueda por nombre o apellido del paciente
    List<ExpedienteMedico> findByPacienteNombreContainingIgnoreCaseOrPacienteApellidoContainingIgnoreCase(String nombre, String apellido);
    
    // Búsqueda por ID del paciente (debe ser único)
    Optional<ExpedienteMedico> findByPacienteId(Long pacienteId);
    
    // Verificar existencia por ID del paciente
    boolean existsByPacienteId(Long pacienteId);
    
    // Búsqueda por estado
    List<ExpedienteMedico> findByEstado(String estado);
    
    // Búsqueda combinada (opcional)
    @Query("SELECT e FROM ExpedienteMedico e WHERE " +
           "LOWER(e.paciente.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
           "LOWER(e.paciente.apellido) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
           "LOWER(e.paciente.numeroIdentificacion) LIKE LOWER(CONCAT('%', :termino, '%'))")
    List<ExpedienteMedico> buscarPorTerminoGeneral(@Param("termino") String termino);
}