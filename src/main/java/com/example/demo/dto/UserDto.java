package com.example.demo.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public class UserDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String numeroIdentificacion;
    private LocalDate fechaNacimiento;
    private Boolean activo = true;
    private LocalDateTime createdAt;
    private List<String> roles;
    private String selectedRole; // Campo adicional para Thymeleaf
    
    // Constructores
    public UserDto() {}
    
    public UserDto(String firstName, String lastName, String email, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.activo = true;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    // Método para nombre completo
    public String getName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getNumeroIdentificacion() { return numeroIdentificacion; }
    public void setNumeroIdentificacion(String numeroIdentificacion) { 
        this.numeroIdentificacion = numeroIdentificacion; 
    }
    
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    
    public Boolean getActivo() { 
        return activo != null ? activo : true;
    }
    
    public void setActivo(Boolean activo) { 
        this.activo = activo != null ? activo : true;
    }
    
    // Para compatibilidad con el HTML
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    // Para compatibilidad con Date antiguo
    public Date getFechaRegistro() { 
        return createdAt != null ? 
            java.sql.Timestamp.valueOf(createdAt) : null; 
    }
    
    public void setFechaRegistro(Date fechaRegistro) { 
        if (fechaRegistro != null) {
            this.createdAt = fechaRegistro.toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDateTime();
        }
    }
    
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { 
        this.roles = roles;
        // Actualizar selectedRole automáticamente
        updateSelectedRole();
    }
    
    // Campo para Thymeleaf
    public String getSelectedRole() {
        return selectedRole;
    }
    
    public void setSelectedRole(String selectedRole) {
        this.selectedRole = selectedRole;
    }
    
    // Método para actualizar selectedRole basado en roles
    private void updateSelectedRole() {
        if (roles != null && !roles.isEmpty()) {
            String role = roles.get(0);
            if (role.equals("ROLE_ADMIN")) this.selectedRole = "ADMIN";
            else if (role.equals("ROLE_MEDICO")) this.selectedRole = "DOCTOR";
            else if (role.equals("ROLE_PACIENTE")) this.selectedRole = "PATIENT";
            else if (role.equals("ROLE_STAFF")) this.selectedRole = "STAFF";
            else this.selectedRole = role.replace("ROLE_", "");
        } else {
            this.selectedRole = null;
        }
    }
    
    // Métodos auxiliares para verificar roles
    public boolean isAdmin() {
        return roles != null && roles.contains("ROLE_ADMIN");
    }
    
    public boolean isMedico() {
        return roles != null && roles.contains("ROLE_MEDICO");
    }
    
    public boolean isPaciente() {
        return roles != null && roles.contains("ROLE_PACIENTE");
    }
    
    public boolean isStaff() {
        return roles != null && roles.contains("ROLE_STAFF");
    }
    
    // Para compatibilidad con thymeleaf (activo como boolean primitivo)
    public boolean isActivo() {
        return activo != null ? activo : true;
    }
}