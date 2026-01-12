package com.example.demo.controller;

import java.security.Principal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.Entitys.Cita;
import com.example.demo.Entitys.ExpedienteMedico;
import com.example.demo.Entitys.Paciente;
import com.example.demo.Entitys.User;
import com.example.demo.enums.Especialidad;
import com.example.demo.service.CitaService;
import com.example.demo.service.ExpedienteMedicoService;
import com.example.demo.service.PacienteService;
import com.example.demo.service.UserService;

@Controller
@RequestMapping("/pacientes")
public class PanelPacienteController {

    @Autowired
    private PacienteService pacienteService;

    @Autowired
    private UserService userService;
    
    @Autowired
    private ExpedienteMedicoService expedienteService;
    
    @Autowired
    private CitaService citaService;

    @GetMapping("/panel")
    public String mostrarPanel(Model model, Principal principal) {
    	System.out.println("=== DEBUG INICIO ===");
        System.out.println("Email del usuario logueado: " + principal.getName());
        // Obtener el usuario logueado por email
        String email = principal.getName();
        User usuario = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        System.out.println("Usuario encontrado: ID=" + usuario.getId() + ", Email=" + usuario.getEmail());

        // Buscar el paciente asociado al usuario
        Paciente paciente = pacienteService.buscarPorUsuario(usuario)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
        
        System.out.println("Paciente encontrado: ID=" + paciente.getId() + 
                ", Nombre=" + paciente.getNombre() + 
                ", Apellido=" + paciente.getApellido());

        // Buscar expediente del paciente (si existe)
        Optional<ExpedienteMedico> expedienteOpt = expedienteService.buscarPorPacienteId(paciente.getId());
        
        // Obtener las citas del paciente
        List<Cita> citasPaciente = citaService.obtenerCitasPorPaciente(paciente);
        List<Cita> citasPendientes = citaService.obtenerCitasPendientesPorPaciente(paciente);
        Optional<Cita> ultimaCitaOpt = citaService.obtenerUltimaCita(paciente);
        
        // Obtener citas para el calendario (próximos 30 días)
        List<Cita> citasProximas = citaService.obtenerCitasProximas(paciente, 30);
        
        // Convertir citas a formato para FullCalendar
        List<Map<String, Object>> eventosCalendario = convertirCitasAEventos(citasProximas);
        
        // Pasar datos al modelo
        model.addAttribute("paciente", paciente);
        model.addAttribute("expediente", expedienteOpt.orElse(null));
        model.addAttribute("citasPendientes", citasPendientes.size());
        model.addAttribute("proximasCitas", citasPaciente); // Para la tabla
        model.addAttribute("eventosCalendario", eventosCalendario); // Para el calendario
        model.addAttribute("ultimaCita", ultimaCitaOpt.orElse(null));
        
        // Si el paciente tiene expediente, pasar datos específicos
        if (expedienteOpt.isPresent()) {
            ExpedienteMedico expediente = expedienteOpt.get();
            model.addAttribute("tipoSangre", expediente.getTipoSangre());
            model.addAttribute("estadoExpediente", expediente.getEstado());
        } else {
            model.addAttribute("tipoSangre", "No especificado");
            model.addAttribute("estadoExpediente", "Sin expediente");
        }
        
        // Datos de ejemplo para otras estadísticas
        model.addAttribute("totalExpedientes", 1); // Puedes calcular esto
        model.addAttribute("tratamientosActivos", 3); // Datos de ejemplo
        model.addAttribute("recetasActivas", 2); // Datos de ejemplo
        
        model.addAttribute("title", "Panel Paciente");
        
        System.out.println("=== DEBUG FIN ===");
        return "AccionesPaciente/panelPaciente";
    }
    
    private List<Map<String, Object>> convertirCitasAEventos(List<Cita> citas) {
        List<Map<String, Object>> eventos = new ArrayList<>();
        
        for (Cita cita : citas) {
            Map<String, Object> evento = new HashMap<>();
            
            // Obtener nombre del médico
            String medicoNombre = (cita.getMedico() != null && cita.getMedico().getNombre() != null) 
                ? cita.getMedico().getNombre() 
                : "Médico";
            
            // Obtener especialidad del médico (ENUM)
            String especialidad = "Consulta";
            if (cita.getMedico() != null && cita.getMedico().getEspecialidad() != null) {
                // Convierte el enum a un nombre legible
                especialidad = convertirEspecialidadALegible(cita.getMedico().getEspecialidad());
            }
            
            evento.put("title", especialidad + " - Dr. " + medicoNombre);
            evento.put("start", cita.getFechaHora().toString());
            
            // Color según estado
            String color = "#0077cc"; // Azul por defecto
            if (cita.getEstado() != null) {
                switch (cita.getEstado()) {
                    case CONFIRMADA:
                        color = "#28a745"; // Verde
                        break;
                    case PROGRAMADA:
                        color = "#ffc107"; // Amarillo
                        break;
                    case CANCELADA:
                        color = "#dc3545"; // Rojo
                        break;
                    case COMPLETADA:
                        color = "#6f42c1"; // Púrpura
                        break;
                    default:
                        color = "#0077cc"; // Azul
                }
            }
            
            evento.put("backgroundColor", color);
            evento.put("borderColor", color);
            
            // Datos adicionales para mostrar en el tooltip
            evento.put("extendedProps", Map.of(
                "medico", medicoNombre,
                "especialidad", especialidad,
                "motivo", cita.getMotivo() != null ? cita.getMotivo() : "Consulta",
                "estado", cita.getEstado() != null ? cita.getEstado().toString() : "PROGRAMADA",
                "fechaFormateada", cita.getFechaHora().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            ));
            
            eventos.add(evento);
        }
        
        return eventos;
    }

    // Método auxiliar para convertir el enum a texto legible
    private String convertirEspecialidadALegible(Especialidad especialidad) {
        if (especialidad == null) {
            return "Consulta General";
        }
        
        // Usa el método getDisplayName() si existe, o convierte manualmente
        try {
            // Si tu enum tiene el método getDisplayName()
            return especialidad.getDisplayName();
        } catch (Exception e) {
            // Conversión manual
            String nombre = especialidad.name();
            // Convierte MEDICINA_GENERAL a "Medicina General"
            nombre = nombre.toLowerCase().replace("_", " ");
            // Capitaliza cada palabra
            String[] palabras = nombre.split(" ");
            StringBuilder resultado = new StringBuilder();
            for (String palabra : palabras) {
                if (!palabra.isEmpty()) {
                    resultado.append(Character.toUpperCase(palabra.charAt(0)))
                             .append(palabra.substring(1))
                             .append(" ");
                }
            }
            return resultado.toString().trim();
        }
    }
}