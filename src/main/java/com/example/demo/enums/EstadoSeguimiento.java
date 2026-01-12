package com.example.demo.enums;

public enum EstadoSeguimiento {
    PENDIENTE("Pendiente"),
    REALIZADO("Realizado"),
    CANCELADO("Cancelado");
    
    private final String descripcion;
    
    EstadoSeguimiento(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
}