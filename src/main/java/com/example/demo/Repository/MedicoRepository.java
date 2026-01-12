package com.example.demo.Repository;

import com.example.demo.Entitys.Medico;
import com.example.demo.Entitys.User;
import com.example.demo.enums.EstadoDoctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicoRepository extends JpaRepository<Medico, Long> {
    
    // Métodos básicos
    Optional<Medico> findByEmail(String email);
    Optional<Medico> findByNumeroLicencia(String numeroLicencia);
    Optional<Medico> findByUsuario(User usuario);
    
    // Métodos con ordenamiento
    List<Medico> findAllByOrderByApellidoAscNombreAsc();
    
    // Métodos por estado
    List<Medico> findByEstado(EstadoDoctor estado);
    
    // Métodos combinados
    List<Medico> findByEspecialidadAndEstado(String especialidad, EstadoDoctor estado);
    
    // Método para médicos activos y disponibles
    @Query("SELECT m FROM Medico m WHERE m.estado = 'ACTIVO' AND m.disponible = true")
    List<Medico> findMedicosActivosYDisponibles();
    
    // Método para médicos disponibles por especialidad
    @Query("SELECT m FROM Medico m WHERE m.especialidad = ?1 AND m.estado = 'ACTIVO' AND m.disponible = true")
    List<Medico> findMedicosDisponiblesPorEspecialidad(String especialidad);
}