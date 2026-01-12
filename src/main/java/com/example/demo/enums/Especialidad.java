package com.example.demo.enums;

public enum Especialidad {
    MEDICINA_GENERAL,
    CARDIOLOGIA,
    PEDIATRIA,
    DERMATOLOGIA,
    NEUROLOGIA,
    ORTOPEDIA,
    GINECOLOGIA,
    PSIQUIATRIA,
    OFTALMOLOGIA,
    ODONTOLOGIA;
    
    // Opcional: método para mostrar nombre formateado
    public String getDisplayName() {
        return this.name().replace("_", " ").toLowerCase();
    }
}