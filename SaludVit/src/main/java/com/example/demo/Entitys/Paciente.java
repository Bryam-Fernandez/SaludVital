package com.example.demo.Entitys;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
public class Paciente {
    @Id @GeneratedValue
    private Long id;
    
    private String numeroIdentificacion;


    @NotBlank
    private String nombres;

    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
    public String getNumeroIdentificacion() {
        return numeroIdentificacion;
    }

    public void setNumeroIdentificacion(String numeroIdentificacion) {
        this.numeroIdentificacion = numeroIdentificacion;
    }

	public String getNombres() {
		return nombres;
	}

	public void setNombres(String nombres) {
		this.nombres = nombres;
	}

	public String getApellidos() {
		return apellidos;
	}

	public void setApellidos(String apellidos) {
		this.apellidos = apellidos;
	}

	public String getDocumento() {
		return documento;
	}

	public void setDocumento(String documento) {
		this.documento = documento;
	}

	public LocalDate getFechaNacimiento() {
		return fechaNacimiento;
	}

	public void setFechaNacimiento(LocalDate fechaNacimiento) {
		this.fechaNacimiento = fechaNacimiento;
	}

	public boolean isTieneAlergias() {
		return tieneAlergias;
	}

	public void setTieneAlergias(boolean tieneAlergias) {
		this.tieneAlergias = tieneAlergias;
	}

	public String getListaAlergias() {
		return listaAlergias;
	}

	public void setListaAlergias(String listaAlergias) {
		this.listaAlergias = listaAlergias;
	}

	public List<Cita> getCitas() {
		return citas;
	}

	public void setCitas(List<Cita> citas) {
		this.citas = citas;
	}

	public ExpedienteMedico getExpediente() {
		return expediente;
	}

	public void setExpediente(ExpedienteMedico expediente) {
		this.expediente = expediente;
	}

	@NotBlank
    private String apellidos;

    @NotBlank
    @Column(unique = true)
    private String documento;

    @NotNull
    private LocalDate fechaNacimiento;

    private boolean tieneAlergias;

    private String listaAlergias;

    // Relaciones
    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL)
    private List<Cita> citas;

    @OneToOne(mappedBy = "paciente", cascade = CascadeType.ALL)
    private ExpedienteMedico expediente;
}
