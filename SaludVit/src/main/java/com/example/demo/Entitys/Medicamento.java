package com.example.demo.Entitys;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Medicamento {
    @Id @GeneratedValue
    private Long id;

    private String nombre;

    private String dosis;

    private String frecuencia;
}
