package com.example.demo.Repository;

import com.example.demo.Entitys.Paciente;
import com.example.demo.Entitys.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    Optional<Paciente> findByNumeroIdentificacion(String numeroIdentificacion);
    List<Paciente> findByNombreContainingIgnoreCase(String nombre);
    Optional<Paciente> findByUsuarioEmail(String email);

    @Query("SELECT p FROM Paciente p LEFT JOIN FETCH p.usuario")
    List<Paciente> findAllPacientesConUsuarioOpcional();
    Optional<Paciente> findByUsuario(User usuario);
    
    @Query("SELECT p FROM Paciente p WHERE " +
            "LOWER(p.numeroIdentificacion) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.nombre) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.apellido) LIKE LOWER(CONCAT('%', :query, '%'))")
     List<Paciente> buscarPorDocumentoONombre(@Param("query") String query);
    
    List<Paciente> findByNumeroIdentificacionContainingIgnoreCase(String numeroIdentificacion);
    List<Paciente> findByApellidoContainingIgnoreCase(String apellido);
    List<Paciente> findByEmailContainingIgnoreCase(String email);
    
}
