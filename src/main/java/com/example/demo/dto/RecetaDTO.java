package com.example.demo.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RecetaDTO {
    private Long medicoId;
    private Long pacienteId;
    private Long citaId;
    private Long tratamientoId;
    private String instruccionesGenerales;
    private LocalDate fechaEmision;
    private LocalDate fechaCaducidad;
    private Boolean electronica;
    private List<ItemRecetaDTO> items; // CAMBIADO: ItemRecetaDTO en lugar de RecetaItemDTO
    
    // Constructor
    public RecetaDTO() {
        this.items = new ArrayList<>();
        // Agregar un item por defecto con ItemRecetaDTO
        ItemRecetaDTO item = new ItemRecetaDTO();
        item.setMedicamento("");
        item.setCantidadTotal(1);
        item.setDosis("");
        item.setFrecuencia("");
        item.setIndicaciones("Tomar según indicaciones del médico");
        this.items.add(item);
        this.electronica = false;
    }
    
    // Getters y Setters
    public Long getMedicoId() { return medicoId; }
    public void setMedicoId(Long medicoId) { this.medicoId = medicoId; }
    
    public Long getPacienteId() { return pacienteId; }
    public void setPacienteId(Long pacienteId) { this.pacienteId = pacienteId; }
    
    public Long getCitaId() { return citaId; }
    public void setCitaId(Long citaId) { this.citaId = citaId; }
    
    public Long getTratamientoId() { return tratamientoId; }
    public void setTratamientoId(Long tratamientoId) { this.tratamientoId = tratamientoId; }
    
    public String getInstruccionesGenerales() { return instruccionesGenerales; }
    public void setInstruccionesGenerales(String instruccionesGenerales) { 
        this.instruccionesGenerales = instruccionesGenerales; 
    }
    
    // Para compatibilidad si hay código que usa observaciones
    public String getObservaciones() { 
        return this.instruccionesGenerales; 
    }
    public void setObservaciones(String observaciones) { 
        this.instruccionesGenerales = observaciones; 
    }
    
    public LocalDate getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDate fechaEmision) { this.fechaEmision = fechaEmision; }
    
    public LocalDate getFechaCaducidad() { return fechaCaducidad; }
    public void setFechaCaducidad(LocalDate fechaCaducidad) { this.fechaCaducidad = fechaCaducidad; }
    
    public Boolean getElectronica() { return electronica; }
    public void setElectronica(Boolean electronica) { this.electronica = electronica; }
    
    public List<ItemRecetaDTO> getItems() { return items; } // CAMBIADO
    public void setItems(List<ItemRecetaDTO> items) { this.items = items; } // CAMBIADO
    
    // Elimina los métodos de conversión que ya no necesitas
    // private List<RecetaItemDTO> convertirMedicamentosAItems() { ... }
}