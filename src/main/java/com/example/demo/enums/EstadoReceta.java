package com.example.demo.enums;

public enum EstadoReceta {
    ACTIVA("Activa", "La receta está vigente y puede ser utilizada"),
    DISPENSADA("Dispensada", "La receta ha sido entregada en farmacia"),
    COMPLETADA("Completada", "Todos los medicamentos han sido administrados"),
    SUSPENDIDA("Suspendida", "La receta ha sido suspendida por el médico"),
    CANCELADA("Cancelada", "La receta ha sido cancelada"),
    VENCIDA("Vencida", "La receta ha expirado su fecha de caducidad");
    
    private final String descripcion;
    private final String significado;
    
    EstadoReceta(String descripcion, String significado) {
        this.descripcion = descripcion;
        this.significado = significado;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public String getSignificado() {
        return significado;
    }
    
    // Método para verificar si está activa
    public boolean isActiva() {
        return this == ACTIVA || this == DISPENSADA;
    }
    
    // Método para verificar si está vencida o cancelada
    public boolean isInactiva() {
        return this == VENCIDA || this == CANCELADA || this == SUSPENDIDA;
    }
}