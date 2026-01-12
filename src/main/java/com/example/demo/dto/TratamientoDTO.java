package com.example.demo.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TratamientoDTO {
    private Long id;
    private Long pacienteId;
    private Long medicoId;
    private Long citaId; // Opcional
    private String diagnostico;
    private String descripcion;
    private String duracion; 
    private String observaciones;
    private String instrucciones;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String estado;
    private List<ItemRecetaDTO> medicamentos = new ArrayList<>(); // CAMBIADO A ItemRecetaDTO
    
    // Getters y Setters (actualizados)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getPacienteId() { return pacienteId; }
    public void setPacienteId(Long pacienteId) { this.pacienteId = pacienteId; }
    
 // AGREGAR ESTOS MÉTODOS:
    public String getDuracion() { return duracion; }
    public void setDuracion(String duracion) { this.duracion = duracion; }
    
    // Para compatibilidad con getDuracionDias() si se necesita
    public String getDuracionDias() { 
        if (duracion != null) {
            // Intenta extraer solo los números
            return duracion.replaceAll("\\D", "");
        }
        return "7"; // Valor por defecto
    }
    public void setDuracionDias(String duracionDias) { 
        this.duracion = duracionDias + " días"; 
    }
    
    public Long getMedicoId() { return medicoId; }
    public void setMedicoId(Long medicoId) { this.medicoId = medicoId; }
    
    public Long getCitaId() { return citaId; }
    public void setCitaId(Long citaId) { this.citaId = citaId; }
    
    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    // Para compatibilidad con el servicio que usa getPlanTratamiento()
    public String getPlanTratamiento() { return this.descripcion; }
    public void setPlanTratamiento(String planTratamiento) { this.descripcion = planTratamiento; }
    
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    
    public String getInstrucciones() { return instrucciones; }
    public void setInstrucciones(String instrucciones) { this.instrucciones = instrucciones; }
    
    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    
    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
    // Getter para medicamentos (ItemRecetaDTO)
    public List<ItemRecetaDTO> getMedicamentos() { return medicamentos; }
    public void setMedicamentos(List<ItemRecetaDTO> medicamentos) { this.medicamentos = medicamentos; }
}