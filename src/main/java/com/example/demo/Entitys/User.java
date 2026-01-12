package com.example.demo.Entitys;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "users")
public class User {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "numero_identificacion", nullable = false, unique = true)
    private String numeroIdentificacion;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(name = "activo")
    private Boolean activo = true;

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt = new Date();

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "users_roles",
        joinColumns = @JoinColumn(name = "USER_ID", referencedColumnName = "ID"),
        inverseJoinColumns = @JoinColumn(name = "ROLE_ID", referencedColumnName = "ID")
    )
    private List<Role> roles = new ArrayList<>();

    // Constructores
    public User() {}

    public User(String firstName, String lastName, String email, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.activo = true;
        this.createdAt = new Date();
    }

    public User(String firstName, String lastName, String email, String password, 
                String numeroIdentificacion, LocalDate fechaNacimiento) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.numeroIdentificacion = numeroIdentificacion;
        this.fechaNacimiento = fechaNacimiento;
        this.activo = true;
        this.createdAt = new Date();
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    // Método para compatibilidad hacia atrás (para código que use getName())
    public String getName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }

    // Setter para compatibilidad hacia atrás
    public void setName(String name) {
        if (name != null && name.contains(" ")) {
            String[] parts = name.split(" ", 2);
            this.firstName = parts[0];
            this.lastName = parts[1];
        } else {
            this.firstName = name;
            this.lastName = "";
        }
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNumeroIdentificacion() {
        return numeroIdentificacion;
    }

    public void setNumeroIdentificacion(String numeroIdentificacion) {
        this.numeroIdentificacion = numeroIdentificacion;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public Boolean isActivo() {
        return activo != null ? activo : true;
    }

    public Boolean getActivo() {
        return activo != null ? activo : true;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo != null ? activo : true;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    // Método para obtener createdAt como LocalDateTime (para compatibilidad)
    public LocalDateTime getCreatedAtAsLocalDateTime() {
        return createdAt != null ? 
            createdAt.toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDateTime() : 
            null;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    // Métodos auxiliares
    public void addRole(Role role) {
        if (this.roles == null) {
            this.roles = new ArrayList<>();
        }
        if (!this.roles.contains(role)) {
            this.roles.add(role);
        }
    }

    public void removeRole(Role role) {
        if (this.roles != null) {
            this.roles.remove(role);
        }
    }

    public String getRolPrincipal() {
        if (roles != null && !roles.isEmpty()) {
            return roles.get(0).getName();
        }
        return "SIN_ROL";
    }

    public boolean tieneRol(String rolNombre) {
        if (roles == null) return false;
        return roles.stream()
                .anyMatch(rol -> rol.getName().equals(rolNombre));
    }

    public boolean isAdmin() {
        return tieneRol("ROLE_ADMIN");
    }

    public boolean isMedico() {
        return tieneRol("ROLE_MEDICO");
    }

    public boolean isPaciente() {
        return tieneRol("ROLE_PACIENTE");
    }

    // Métodos de utilidad
    public String getNombreCompleto() {
        return getName();
    }

    public int getEdad() {
        if (fechaNacimiento == null) return 0;
        return java.time.Period.between(fechaNacimiento, LocalDate.now()).getYears();
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", activo=" + activo +
                ", roles=" + (roles != null ? roles.stream().map(Role::getName).toList() : "[]") +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id != null && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}