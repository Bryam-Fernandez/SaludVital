package com.example.demo.Entitys;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "expedientes_medicos")
public class ExpedienteMedico {
    
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "paciente_id", unique = true)
    @JsonIgnoreProperties({"expediente", "citas", "alergias", "enfermedades"})
    private Paciente paciente;

    @OneToMany(mappedBy = "expediente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EntradaHistorial> historial = new ArrayList<>();

    // Campos adicionales necesarios
    private String tipoSangre;
    
    @Column(columnDefinition = "TEXT")
    private String alergias;
    
    @Column(columnDefinition = "TEXT")
    private String medicamentos;
    
    @Column(columnDefinition = "TEXT")
    private String enfermedadesCronicas;
    
    @Column(columnDefinition = "TEXT")
    private String observaciones;
    
    private String estado = "ACTIVO"; // ACTIVO, INACTIVO, PENDIENTE, APROBADO
    
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
    
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
    
    // Constructor
    public ExpedienteMedico() {
        this.fechaCreacion = LocalDateTime.now();
        this.estado = "ACTIVO";
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }

    public List<EntradaHistorial> getHistorial() { return historial; }
    public void setHistorial(List<EntradaHistorial> historial) { this.historial = historial; }
    
    // Para compatibilidad con el Controller (linea 88)
    public List<EntradaHistorial> getEntradasHistorial() {
        return historial;
    }
    
    public void setEntradasHistorial(List<EntradaHistorial> entradasHistorial) {
        this.historial = entradasHistorial;
    }

    public String getTipoSangre() { return tipoSangre; }
    public void setTipoSangre(String tipoSangre) { this.tipoSangre = tipoSangre; }

    public String getAlergias() { return alergias; }
    public void setAlergias(String alergias) { this.alergias = alergias; }

    public String getMedicamentos() { return medicamentos; }
    public void setMedicamentos(String medicamentos) { this.medicamentos = medicamentos; }

    public String getEnfermedadesCronicas() { return enfermedadesCronicas; }
    public void setEnfermedadesCronicas(String enfermedadesCronicas) { 
        this.enfermedadesCronicas = enfermedadesCronicas; 
    }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { 
        this.fechaCreacion = fechaCreacion; 
    }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { 
        this.fechaActualizacion = fechaActualizacion; 
    }
    
    // Método para auditoría
    @PreUpdate
    protected void onUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
    
    // Método de conveniencia
    public void agregarEntradaHistorial(EntradaHistorial entrada) {
        entrada.setExpediente(this);
        this.historial.add(entrada);
    }
}