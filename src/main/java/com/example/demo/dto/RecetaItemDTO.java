package com.example.demo.dto;

public class RecetaItemDTO {
    private String medicamentoNombre;
    private Integer cantidad;
    private String instrucciones;
    
    // Getters y Setters
    public String getMedicamentoNombre() { return medicamentoNombre; }
    public void setMedicamentoNombre(String medicamentoNombre) { this.medicamentoNombre = medicamentoNombre; }
    
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    
    public String getInstrucciones() { return instrucciones; }
    public void setInstrucciones(String instrucciones) { this.instrucciones = instrucciones; }
}