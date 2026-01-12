package com.example.demo.Entitys;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.demo.enums.EstadoTratamiento;

@Entity
@Table(name = "tratamientos")
public class Tratamiento {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String diagnostico;
    
    @Column(columnDefinition = "TEXT")
    private String planTratamiento;
    
    private String observaciones;
    
    @Column(nullable = false)
    private LocalDate fechaInicio;
    
    private LocalDate fechaFin;
    
    @Enumerated(EnumType.STRING)
    private EstadoTratamiento estado;
    
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    
    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cita_id", nullable = false)
    private Cita cita;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;
    
    @OneToMany(mappedBy = "tratamiento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Receta> recetas = new ArrayList<>();
    
    @OneToMany(mappedBy = "tratamiento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SeguimientoTratamiento> seguimientos = new ArrayList<>();
    
    // Constructor
    public Tratamiento() {
        this.fechaCreacion = LocalDateTime.now();
        this.estado = EstadoTratamiento.ACTIVO;
    }
    
    @PreUpdate
    public void preUpdate() {
        this.fechaModificacion = LocalDateTime.now();
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }
    
    public String getPlanTratamiento() { return planTratamiento; }
    public void setPlanTratamiento(String planTratamiento) { this.planTratamiento = planTratamiento; }
    
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    
    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    
    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
    
    public EstadoTratamiento getEstado() { return estado; }
    public void setEstado(EstadoTratamiento estado) { this.estado = estado; }
    
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    
    public LocalDateTime getFechaModificacion() { return fechaModificacion; }
    public void setFechaModificacion(LocalDateTime fechaModificacion) { this.fechaModificacion = fechaModificacion; }
    
    public Cita getCita() { return cita; }
    public void setCita(Cita cita) { this.cita = cita; }
    
    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }
    
    public Medico getMedico() { return medico; }
    public void setMedico(Medico medico) { this.medico = medico; }
    
    public List<Receta> getRecetas() { return recetas; }
    public void setRecetas(List<Receta> recetas) { this.recetas = recetas; }
    
    public List<SeguimientoTratamiento> getSeguimientos() { return seguimientos; }
    public void setSeguimientos(List<SeguimientoTratamiento> seguimientos) { this.seguimientos = seguimientos; }
    
    // Métodos de utilidad
    public void agregarReceta(Receta receta) {
        receta.setTratamiento(this);
        this.recetas.add(receta);
    }
    
    public void agregarSeguimiento(SeguimientoTratamiento seguimiento) {
        seguimiento.setTratamiento(this);
        this.seguimientos.add(seguimiento);
    }
    
    public boolean isActivo() {
        return this.estado == EstadoTratamiento.ACTIVO;
    }
    
    public boolean isCompletado() {
        return this.estado == EstadoTratamiento.COMPLETADO;
    }
}