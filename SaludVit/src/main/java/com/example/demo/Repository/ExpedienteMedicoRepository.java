package com.example.demo.Repository;

import com.example.demo.Entitys.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExpedienteMedicoRepository extends JpaRepository<ExpedienteMedico, Long> {
    Optional<ExpedienteMedico> findByPacienteId(Long pacienteId);
}
