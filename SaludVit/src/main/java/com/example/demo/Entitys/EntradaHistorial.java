package com.example.demo.Entitys;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class EntradaHistorial {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private ExpedienteMedico expediente;

    private LocalDateTime fechaHora;

    private String diagnostico;

    private String tratamiento;
}
