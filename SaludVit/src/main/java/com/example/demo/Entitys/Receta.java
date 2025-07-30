package com.example.demo.Entitys;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;

@Entity
public class Receta {
    @Id @GeneratedValue
    private Long id;

    private String numero;

    private LocalDate fechaEmision;

    private LocalDate fechaCaducidad;

    @ManyToOne
    private Medico medico;

    @ManyToOne
    
    
    
    private Paciente paciente;

    @ManyToMany
    private List<Medicamento> medicamentos;
}
