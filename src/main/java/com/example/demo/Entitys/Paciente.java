package com.example.demo.Entitys;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "paciente") // Cambié a plural (mejor práctica)
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;	

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 100)
    private String apellido; // Campo NUEVO

    @Column(name = "numero_identificacion", unique = true, nullable = false, length = 20)
    private String numeroIdentificacion;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(length = 20)
    private String genero; // Campo NUEVO

    @Column(length = 20)
    private String telefono; // Campo NUEVO

    @Column(columnDefinition = "TEXT")
    private String direccion; // Campo NUEVO

    @Column(length = 100)
    private String email; // Campo NUEVO

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", referencedColumnName = "id") 
    private User usuario;

    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Alergia> alergias = new ArrayList<>();

    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Enfermedad> enfermedades = new ArrayList<>();
    
    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Cita> citas = new ArrayList<>();

    @OneToOne(mappedBy = "paciente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonBackReference
    private ExpedienteMedico expediente; // Campo NUEVO - relación con expediente

    // Constructor vacío
    public Paciente() {}

    // Constructor completo
    public Paciente(String nombre, String apellido, String numeroIdentificacion, 
                   LocalDate fechaNacimiento, String genero, String telefono, 
                   String direccion, String email, User usuario) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.numeroIdentificacion = numeroIdentificacion;
        this.fechaNacimiento = fechaNacimiento;
        this.genero = genero;
        this.telefono = telefono;
        this.direccion = direccion;
        this.email = email;
        this.usuario = usuario;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    // NUEVOS getters y setters
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getNumeroIdentificacion() { return numeroIdentificacion; }
    public void setNumeroIdentificacion(String numeroIdentificacion) { 
        this.numeroIdentificacion = numeroIdentificacion; 
    }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { 
        this.fechaNacimiento = fechaNacimiento; 
    }

    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public User getUsuario() { return usuario; }
    public void setUsuario(User usuario) { this.usuario = usuario; }

    public List<Alergia> getAlergias() { return alergias; }
    public void setAlergias(List<Alergia> alergias) { this.alergias = alergias; }

    public List<Enfermedad> getEnfermedades() { return enfermedades; }
    public void setEnfermedades(List<Enfermedad> enfermedades) { 
        this.enfermedades = enfermedades; 
    }

    public List<Cita> getCitas() { return citas; }
    public void setCitas(List<Cita> citas) { this.citas = citas; }

    public ExpedienteMedico getExpediente() { return expediente; }
    public void setExpediente(ExpedienteMedico expediente) { 
        this.expediente = expediente; 
    }

    // Métodos de conveniencia
    public void agregarAlergia(Alergia alergia) {
        alergias.add(alergia);
        alergia.setPaciente(this);
    }

    public void agregarEnfermedad(Enfermedad enfermedad) {
        enfermedades.add(enfermedad);
        enfermedad.setPaciente(this);
    }

    public void agregarCita(Cita cita) {
        citas.add(cita);
        cita.setPaciente(this);
    }

    // Método para obtener nombre completo
    public String getNombreCompleto() {
        return nombre + " " + (apellido != null ? apellido : "");
    }

    // Método para calcular edad
    public int getEdad() {
        if (fechaNacimiento == null) return 0;
        return LocalDate.now().getYear() - fechaNacimiento.getYear();
    }
}