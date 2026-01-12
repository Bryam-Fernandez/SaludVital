package com.example.demo.Entitys;

import java.time.LocalTime;

import com.example.demo.enums.DiaSemana;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class HorarioAtencion {

    @Id
    private Long id;

    @Enumerated(EnumType.STRING)
    private DiaSemana dia;

    private LocalTime horaInicio;

    private LocalTime horaFin;

    @ManyToOne
    @JoinColumn(name = "medico_id")
    private Medico medico;

   
    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public DiaSemana getDia() { return dia; }

    public void setDia(DiaSemana dia) { this.dia = dia; }

    public LocalTime getHoraInicio() { return horaInicio; }

    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFin() { return horaFin; }

    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }

    public Medico getMedico() { return medico; }

    public void setMedico(Medico medico) { this.medico = medico; }
}
