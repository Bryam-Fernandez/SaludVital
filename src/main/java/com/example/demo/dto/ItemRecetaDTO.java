package com.example.demo.dto;

public class ItemRecetaDTO {
    private String medicamento;
    private String dosis;
    private String frecuencia;
    private Integer duracionDias; // Mantén este como Integer
    private String indicaciones;
    private Integer cantidadTotal;
    private String duracionTexto; // Cambia este nombre para evitar confusión
    private String unidadMedida;
    private String viaAdministracion;
    
    // Getters y Setters
    public String getMedicamento() {
        return medicamento;
    }
    
    public void setMedicamento(String medicamento) {
        this.medicamento = medicamento;
    }
    
    public String getDosis() {
        return dosis;
    }
    
    public void setDosis(String dosis) {
        this.dosis = dosis;
    }
    
    public String getFrecuencia() {
        return frecuencia;
    }
    
    public void setFrecuencia(String frecuencia) {
        this.frecuencia = frecuencia;
    }
    
    // Para duración en días (Integer) - PRINCIPAL
    public Integer getDuracionDias() {
        return duracionDias;
    }
    
    public void setDuracionDias(Integer duracionDias) {
        this.duracionDias = duracionDias;
    }
    
    // Para duración en texto (String) - AUXILIAR
    public String getDuracionTexto() {
        return duracionTexto;
    }
    
    public void setDuracionTexto(String duracionTexto) {
        this.duracionTexto = duracionTexto;
    }
    
    // Método de compatibilidad - si alguien llama getDuracion() devuelve texto
    public String getDuracion() {
        if (duracionTexto != null) {
            return duracionTexto;
        } else if (duracionDias != null) {
            return duracionDias + " días";
        }
        return null;
    }
    
    public void setDuracion(String duracion) {
        this.duracionTexto = duracion;
        // Intenta extraer días del texto
        if (duracion != null) {
            try {
                String numero = duracion.replaceAll("\\D", "");
                if (!numero.isEmpty()) {
                    this.duracionDias = Integer.parseInt(numero);
                }
            } catch (NumberFormatException e) {
                // Si no se puede parsear, dejar duracionDias como null
            }
        }
    }
    
    public String getIndicaciones() {
        return indicaciones;
    }
    
    public void setIndicaciones(String indicaciones) {
        this.indicaciones = indicaciones;
    }
    
    public Integer getCantidadTotal() {
        return cantidadTotal;
    }
    
    public void setCantidadTotal(Integer cantidadTotal) {
        this.cantidadTotal = cantidadTotal;
    }
    
    public String getUnidadMedida() {
        return unidadMedida;
    }
    
    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }
    
    public String getViaAdministracion() {
        return viaAdministracion;
    }
    
    public void setViaAdministracion(String viaAdministracion) {
        this.viaAdministracion = viaAdministracion;
    }
    
    // Constructor por defecto
    public ItemRecetaDTO() {}
    
    // Constructor con campos principales
    public ItemRecetaDTO(String medicamento, String dosis, String frecuencia) {
        this.medicamento = medicamento;
        this.dosis = dosis;
        this.frecuencia = frecuencia;
    }
}