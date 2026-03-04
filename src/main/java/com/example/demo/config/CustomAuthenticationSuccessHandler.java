package com.example.demo.config;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        Set<String> roles = authentication.getAuthorities()
                .stream().map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        System.err.println("========== LOGIN EXITOSO ==========");
        System.err.println("Usuario: " + authentication.getName());
        System.err.println("Roles RAW: " + roles);

        String target = "/pacientes/panel";               
        
        // Verificar con y sin prefijo ROLE_
        boolean esAdmin = roles.contains("ROLE_ADMIN") || roles.contains("ADMIN");
        boolean esMedico = roles.contains("ROLE_MEDICO") || roles.contains("MEDICO");
        boolean esPaciente = roles.contains("ROLE_PACIENTE") || roles.contains("PACIENTE");
        
        System.err.println("esAdmin: " + esAdmin);
        System.err.println("esMedico: " + esMedico);
        System.err.println("esPaciente: " + esPaciente);
        
        if (esAdmin) {
            target = "/panel";                  
        } else if (esMedico) {
            target = "/medico/panel";
        } else if (esPaciente) {
            target = "/pacientes/panel"; 
        }

        System.err.println("Redirigiendo a: " + target);
        response.sendRedirect(target);
    }
}