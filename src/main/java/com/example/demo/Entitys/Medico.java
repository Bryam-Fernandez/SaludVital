package com.example.demo.Entitys;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;

import com.example.demo.enums.Especialidad;
import com.example.demo.enums.EstadoDoctor;

@Entity
public class Medico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    @Column(unique = true, nullable = false)
    private String numeroLicencia;

    private String telefono;

    @Column(unique = true, nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    private Especialidad especialidad;

    @Enumerated(EnumType.STRING)
    private EstadoDoctor estado; 

    private boolean disponible;

    private BigDecimal tarifaConsulta;
    
    private boolean activo = true;

    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private User usuario;
    
    public Medico() {
        this.activo = true;
    }

    @OneToMany(mappedBy = "medico", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HorarioAtencion> horarios;

   
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getNumeroLicencia() { return numeroLicencia; }
    public void setNumeroLicencia(String numeroLicencia) { this.numeroLicencia = numeroLicencia; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Especialidad getEspecialidad() { return especialidad; }
    public void setEspecialidad(Especialidad especialidad) { this.especialidad = especialidad; }

    public EstadoDoctor getEstado() { return estado; }
    public void setEstado(EstadoDoctor estado) { this.estado = estado; }
    
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }

    public BigDecimal getTarifaConsulta() { return tarifaConsulta; }
    public void setTarifaConsulta(BigDecimal tarifaConsulta) { this.tarifaConsulta = tarifaConsulta; }

    public User getUsuario() { return usuario; }
    public void setUsuario(User usuario) { this.usuario = usuario; }

    public List<HorarioAtencion> getHorarios() { return horarios; }
    public void setHorarios(List<HorarioAtencion> horarios) { this.horarios = horarios; }
    
    @Transient
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

}
