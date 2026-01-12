package com.example.demo.Entitys;

import jakarta.persistence.*;

@Entity
@Table(name = "items_receta")
public class ItemReceta {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String medicamento;
    
    @Column(nullable = false)
    private String dosis;
    
    @Column(nullable = false)
    private String frecuencia;
    
    private Integer duracionDias;
    private String indicaciones;
    
    @Column(name = "cantidad_total")
    private Integer cantidadTotal;
    
    @Column(name = "unidad_medida")
    private String unidadMedida;
    
    @Column(name = "via_administracion")
    private String viaAdministracion;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receta_id", nullable = false)
    private Receta receta;
    
    // Constructores, getters y setters...
    public ItemReceta() {}
    
    public ItemReceta(String medicamento, String dosis, String frecuencia) {
        this.medicamento = medicamento;
        this.dosis = dosis;
        this.frecuencia = frecuencia;
    }
    
    // Getters y Setters completos
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getMedicamento() { return medicamento; }
    public void setMedicamento(String medicamento) { this.medicamento = medicamento; }
    
    public String getDosis() { return dosis; }
    public void setDosis(String dosis) { this.dosis = dosis; }
    
    public String getFrecuencia() { return frecuencia; }
    public void setFrecuencia(String frecuencia) { this.frecuencia = frecuencia; }
    
    public Integer getDuracionDias() { return duracionDias; }
    public void setDuracionDias(Integer duracionDias) { this.duracionDias = duracionDias; }
    
    public String getIndicaciones() { return indicaciones; }
    public void setIndicaciones(String indicaciones) { this.indicaciones = indicaciones; }
    
    public Integer getCantidadTotal() { return cantidadTotal; }
    public void setCantidadTotal(Integer cantidadTotal) { this.cantidadTotal = cantidadTotal; }
    
    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }
    
    public String getViaAdministracion() { return viaAdministracion; }
    public void setViaAdministracion(String viaAdministracion) { this.viaAdministracion = viaAdministracion; }
    
    public Receta getReceta() { return receta; }
    public void setReceta(Receta receta) { this.receta = receta; }
}