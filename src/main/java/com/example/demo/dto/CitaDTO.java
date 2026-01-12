package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CitaDTO {
    private Long pacienteId;
    
    @NotNull(message = "Debe seleccionar un médico")
    private Long medicoId;
    
    @NotNull(message = "Debe seleccionar fecha y hora")
    private LocalDateTime fechaHora;
    
    @NotBlank(message = "El motivo es obligatorio")
    private String motivo;
    
    private BigDecimal tarifaAplicada;
    
    private String estado;
    
    private String notas;
    
    // Getters y setters
    public Long getPacienteId() { return pacienteId; }
    public void setPacienteId(Long pacienteId) { this.pacienteId = pacienteId; }
    
    public Long getMedicoId() { return medicoId; }
    public void setMedicoId(Long medicoId) { this.medicoId = medicoId; }
    
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    
    public BigDecimal getTarifaAplicada() { return tarifaAplicada; }
    public void setTarifaAplicada(BigDecimal tarifaAplicada) { this.tarifaAplicada = tarifaAplicada; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }
}