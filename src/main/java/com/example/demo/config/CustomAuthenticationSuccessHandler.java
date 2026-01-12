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

	        String target = "/home";               
	        if (roles.contains("ROLE_ADMIN")) {
	            target = "/panel";                  
	        } else if (roles.contains("ROLE_MEDICO")) {
	            target = "/medico/panel";
	        } else if (roles.contains("ROLE_PACIENTE")) {
	                target = "/pacientes/panel"; 
	            }

	        response.sendRedirect(target);
	    }
	}