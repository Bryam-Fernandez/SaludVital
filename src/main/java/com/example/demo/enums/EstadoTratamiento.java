package com.example.demo.enums;

public enum EstadoTratamiento {
    ACTIVO("Activo"),
    SUSPENDIDO("Suspendido"),
    COMPLETADO("Completado"),
    CANCELADO("Cancelado");
    
    private final String descripcion;
    
    EstadoTratamiento(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
}