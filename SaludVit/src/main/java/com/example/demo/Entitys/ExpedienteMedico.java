package com.example.demo.Entitys;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

@Entity
public class ExpedienteMedico {
    @Id @GeneratedValue
    private Long id;

    @OneToOne
    private Paciente paciente;

    @OneToMany(mappedBy = "expediente", cascade = CascadeType.ALL)
    private List<EntradaHistorial> historial;
}
