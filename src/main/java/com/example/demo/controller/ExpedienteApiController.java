package com.example.demo.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.demo.service.ExpedienteMedicoService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/expedientes")
public class ExpedienteApiController {

    @Autowired
    private ExpedienteMedicoService expedienteService;

    // Endpoint para verificar si un paciente ya tiene expediente
    @GetMapping("/verificar/{pacienteId}")
    public ResponseEntity<?> verificarExpediente(@PathVariable Long pacienteId) {
        try {
            boolean tieneExpediente = expedienteService.existeExpedienteParaPaciente(pacienteId);
            
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("tieneExpediente", tieneExpediente);
            respuesta.put("pacienteId", pacienteId);
            
            return ResponseEntity.ok(respuesta);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al verificar expediente: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}