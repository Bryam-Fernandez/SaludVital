package com.example.demo.Entitys;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.example.demo.enums.EstadoCita;

@Entity
@Table(name = "cita")
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fechaHora;

    @ManyToOne
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCita estado = EstadoCita.PROGRAMADA; 

    @Column(nullable = false, length = 200)
    private String motivo;

    @Column(name = "tarifa_aplicada", precision = 10, scale = 2)
    private BigDecimal tarifaAplicada;
    
    @Column(length = 500)
    private String notas; 

    public Cita() {}
    
    public String getNotas() {
        return notas;
    }
    
    public void setNotas(String notas) {
        this.notas = notas;
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalDateTime getFechaHora() {
		return fechaHora;
	}

	public void setFechaHora(LocalDateTime fechaHora) {
		this.fechaHora = fechaHora;
	}

	public Paciente getPaciente() {
		return paciente;
	}

	public void setPaciente(Paciente paciente) {
		this.paciente = paciente;
	}

	public Medico getMedico() {
		return medico;
	}

	public void setMedico(Medico medico) {
		this.medico = medico;
	}

	public EstadoCita getEstado() {
		return estado;
	}

	public void setEstado(EstadoCita estado) {
		this.estado = estado;
	}

	public String getMotivo() {
		return motivo;
	}

	public void setMotivo(String motivo) {
		this.motivo = motivo;
	}

	public BigDecimal getTarifaAplicada() {
		return tarifaAplicada;
	}

	public void setTarifaAplicada(BigDecimal tarifaAplicada) {
		this.tarifaAplicada = tarifaAplicada;
	}

	public Cita(Long id, LocalDateTime fechaHora, Paciente paciente, Medico medico, EstadoCita estado, String motivo,
			BigDecimal tarifaAplicada) {
		super();
		this.id = id;
		this.fechaHora = fechaHora;
		this.paciente = paciente;
		this.medico = medico;
		this.estado = estado;
		this.motivo = motivo;
		this.tarifaAplicada = tarifaAplicada;
	}

 
}
