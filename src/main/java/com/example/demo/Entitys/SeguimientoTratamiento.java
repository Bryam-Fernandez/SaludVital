package com.example.demo.Entitys;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.example.demo.enums.EstadoSeguimiento;

@Entity
@Table(name = "seguimientos_tratamiento")
public class SeguimientoTratamiento {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String observaciones;
    
    private LocalDateTime fechaSeguimiento;
    
    @Enumerated(EnumType.STRING)
    private EstadoSeguimiento estado;
    
    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tratamiento_id", nullable = false)
    private Tratamiento tratamiento;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;
    
    // Constructor
    public SeguimientoTratamiento() {
        this.fechaSeguimiento = LocalDateTime.now();
        this.estado = EstadoSeguimiento.PENDIENTE;
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    
    public LocalDateTime getFechaSeguimiento() { return fechaSeguimiento; }
    public void setFechaSeguimiento(LocalDateTime fechaSeguimiento) { this.fechaSeguimiento = fechaSeguimiento; }
    
    public EstadoSeguimiento getEstado() { return estado; }
    public void setEstado(EstadoSeguimiento estado) { this.estado = estado; }
    
    public Tratamiento getTratamiento() { return tratamiento; }
    public void setTratamiento(Tratamiento tratamiento) { this.tratamiento = tratamiento; }
    
    public Medico getMedico() { return medico; }
    public void setMedico(Medico medico) { this.medico = medico; }
}