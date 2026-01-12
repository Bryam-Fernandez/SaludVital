package com.example.demo.Entitys;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class EntradaHistorial {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expediente_id", nullable = false)
    private ExpedienteMedico expediente;


    private String titulo;
    private String descripcion;
    private String tipo;
    private String notas;
    private LocalDateTime fechaCreacion;

    // Constructores
    public EntradaHistorial() {
        this.fechaCreacion = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public ExpedienteMedico getExpediente() { return expediente; }
    public void setExpediente(ExpedienteMedico expediente) { this.expediente = expediente; }
    
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    
    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }
    
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}