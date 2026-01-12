package com.example.demo.Entitys;

import com.example.demo.enums.EstadoReceta;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recetas")
public class Receta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate fechaEmision;
    private LocalDate fechaCaducidad;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoReceta estado;
    
    private String instruccionesGenerales;
    private String firmaMedico;
    private String codigoQR; // Para validación
    private String numeroReceta; // Número único de receta

    // Relación con Tratamiento
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tratamiento_id")
    private Tratamiento tratamiento;
    
    // Relaciones existentes
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cita_id")
    private Cita cita; // Cita donde se generó la receta

    @OneToMany(mappedBy = "receta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemReceta> items = new ArrayList<>();
    
    // Constructor
    public Receta() {
        this.fechaEmision = LocalDate.now();
        this.estado = EstadoReceta.ACTIVA;
        // Caducidad por defecto: 30 días
        this.fechaCaducidad = LocalDate.now().plusDays(30);
    }
    
    @PrePersist
    public void prePersist() {
        if (this.numeroReceta == null) {
            // Generar número de receta único (puedes personalizar esta lógica)
            this.numeroReceta = "REC-" + System.currentTimeMillis() + "-" + 
                               (int)(Math.random() * 1000);
        }
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDate fechaEmision) { this.fechaEmision = fechaEmision; }

    public LocalDate getFechaCaducidad() { return fechaCaducidad; }
    public void setFechaCaducidad(LocalDate fechaCaducidad) { this.fechaCaducidad = fechaCaducidad; }
    
    public EstadoReceta getEstado() { return estado; }
    public void setEstado(EstadoReceta estado) { this.estado = estado; }
    
    public String getInstruccionesGenerales() { return instruccionesGenerales; }
    public void setInstruccionesGenerales(String instruccionesGenerales) { this.instruccionesGenerales = instruccionesGenerales; }
    
    public String getFirmaMedico() { return firmaMedico; }
    public void setFirmaMedico(String firmaMedico) { this.firmaMedico = firmaMedico; }
    
    public String getCodigoQR() { return codigoQR; }
    public void setCodigoQR(String codigoQR) { this.codigoQR = codigoQR; }
    
    public String getNumeroReceta() { return numeroReceta; }
    public void setNumeroReceta(String numeroReceta) { this.numeroReceta = numeroReceta; }

    public Tratamiento getTratamiento() { return tratamiento; }
    public void setTratamiento(Tratamiento tratamiento) { this.tratamiento = tratamiento; }
    
    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }

    public Medico getMedico() { return medico; }
    public void setMedico(Medico medico) { this.medico = medico; }
    
    public Cita getCita() { return cita; }
    public void setCita(Cita cita) { this.cita = cita; }

    public List<ItemReceta> getItems() { return items; }
    public void setItems(List<ItemReceta> items) { this.items = items; }
    
    // Métodos de utilidad
    public boolean isVigente() {
        return LocalDate.now().isBefore(fechaCaducidad) && estado.isActiva();
    }
    
    public boolean isVencida() {
        return LocalDate.now().isAfter(fechaCaducidad) || estado == EstadoReceta.VENCIDA;
    }
    
    public void agregarItem(ItemReceta item) {
        item.setReceta(this);
        this.items.add(item);
    }
    
    public void marcarComoDispensada() {
        this.estado = EstadoReceta.DISPENSADA;
    }
    
    public void marcarComoCompletada() {
        this.estado = EstadoReceta.COMPLETADA;
    }
    
    public void marcarComoVencida() {
        this.estado = EstadoReceta.VENCIDA;
    }
    
    // Calcular días restantes para caducidad
    public long getDiasRestantes() {
        return LocalDate.now().until(fechaCaducidad).getDays();
    }
}