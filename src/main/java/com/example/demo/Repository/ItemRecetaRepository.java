package com.example.demo.Repository;

import com.example.demo.Entitys.ItemReceta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ItemRecetaRepository extends JpaRepository<ItemReceta, Long> {
    
    // Buscar items por receta
    List<ItemReceta> findByRecetaId(Long recetaId);
    
    // Buscar items por medicamento
    List<ItemReceta> findByMedicamentoContainingIgnoreCase(String medicamento);
    
    // Buscar items por paciente (a través de receta)
    @Query("SELECT i FROM ItemReceta i WHERE i.receta.paciente.id = :pacienteId")
    List<ItemReceta> findByPacienteId(@Param("pacienteId") Long pacienteId);
    
    // Buscar items activos por paciente
    @Query("SELECT i FROM ItemReceta i WHERE i.receta.paciente.id = :pacienteId " +
           "AND i.receta.estado IN ('ACTIVA', 'DISPENSADA') " +
           "AND CURRENT_DATE <= i.receta.fechaCaducidad")
    List<ItemReceta> findActivosByPacienteId(@Param("pacienteId") Long pacienteId);
    
    // Contar items por receta
    long countByRecetaId(Long recetaId);
}