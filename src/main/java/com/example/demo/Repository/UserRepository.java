package com.example.demo.Repository;

import com.example.demo.Entitys.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    boolean existsByNumeroIdentificacion(String numeroIdentificacion);
    
    // MÉTODO CORREGIDO: Usar firstName y lastName en lugar de name
    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
           "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :busqueda, '%'))")
    List<User> buscarPorNombreOEmail(@Param("busqueda") String busqueda);
    
    // Método alternativo más simple
    @Query("SELECT u FROM User u WHERE " +
           "u.firstName LIKE %:busqueda% OR " +
           "u.lastName LIKE %:busqueda% OR " +
           "u.email LIKE %:busqueda%")
    List<User> buscar(@Param("busqueda") String busqueda);
    
    List<User> findByActivoTrue();

    
    // AÑADIR: Contar usuarios por rol
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = :rolNombre")
    long countByRoleName(@Param("rolNombre") String rolNombre);
    
    // Buscar por rol específico
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :rolNombre")
    List<User> findByRoleName(@Param("rolNombre") String rolNombre);
    
    // Contar usuarios activos
    long countByActivoTrue();
    
    // Buscar por estado
    List<User> findByActivo(Boolean activo);
}