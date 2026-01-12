package com.example.demo.controller;

import com.example.demo.Entitys.*;
import com.example.demo.dto.*;
import com.example.demo.enums.*;
import com.example.demo.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/tratamientos")
public class TratamientoController {
    
    @Autowired
    private TratamientoService tratamientoService;
    
    @Autowired
    private RecetaService recetaService;
    
    @Autowired
    private PacienteService pacienteService;
    
    @Autowired
    private MedicoService medicoService;
    
    @Autowired
    private CitaService citaService;
    
    @Autowired
    private UserService userService;
    
    // ========== VISTAS PARA MÉDICO ==========
    
    // Formulario para crear tratamiento desde una cita
    @GetMapping("/nuevo/{citaId}")
    public String mostrarFormularioTratamiento(@PathVariable Long citaId, 
                                               Model model,
                                               Principal principal) {
        try {
            // Verificar que la cita existe
            Optional<Cita> citaOpt = citaService.buscarPorId(citaId);
            if (citaOpt.isEmpty()) {
                model.addAttribute("error", "Cita no encontrada");
                return "error";
            }
            
            Cita cita = citaOpt.get();
            
            // Verificar que el médico tiene acceso
            String email = principal.getName();
            Optional<User> usuario = userService.findByEmail(email);
            if (usuario.isEmpty()) {
                return "redirect:/login";
            }
            
            Optional<Medico> medicoOpt = medicoService.buscarPorUsuario(usuario.get());
            if (medicoOpt.isEmpty() || !medicoOpt.get().getId().equals(cita.getMedico().getId())) {
                model.addAttribute("error", "No tiene permisos para crear tratamiento para esta cita");
                return "error";
            }
            
            // Preparar modelo
            model.addAttribute("cita", cita);
            model.addAttribute("paciente", cita.getPaciente());
            model.addAttribute("medico", cita.getMedico());
            
            // DTO vacío para el formulario
            TratamientoDTO tratamientoDTO = new TratamientoDTO();
            tratamientoDTO.setCitaId(citaId);
            tratamientoDTO.setPacienteId(cita.getPaciente().getId());
            tratamientoDTO.setMedicoId(cita.getMedico().getId());
            tratamientoDTO.setFechaInicio(LocalDate.now());
            
            model.addAttribute("tratamientoDTO", tratamientoDTO);
            model.addAttribute("estadosTratamiento", EstadoTratamiento.values());
            
            return "medico/tratamientos/nuevo-tratamiento";
            
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar formulario: " + e.getMessage());
            return "error";
        }
    }
    
    // Crear tratamiento desde cita específica
    @PostMapping("/crear")
    public String crearTratamiento(@ModelAttribute TratamientoDTO tratamientoDTO,
                                  Principal principal,
                                  RedirectAttributes redirectAttrs,
                                  Model model) {
        try {
            // Crear tratamiento
            Tratamiento tratamiento = tratamientoService.crearTratamientoCompleto(tratamientoDTO);
            
            redirectAttrs.addFlashAttribute("success", 
                "Tratamiento creado exitosamente. Receta generada correctamente.");
            
            // Redirigir según el rol
            String email = principal.getName();
            Optional<User> usuario = userService.findByEmail(email);
            
            if (usuario.isPresent() && usuario.get().getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_MEDICO"))) {
                return "redirect:/tratamientos/medico/" + tratamiento.getId();
            } else {
                return "redirect:/tratamientos/" + tratamiento.getId();
            }
            
        } catch (Exception e) {
            model.addAttribute("error", "Error al crear tratamiento: " + e.getMessage());
            // Recargar datos para el formulario
            Optional<Cita> cita = citaService.buscarPorId(tratamientoDTO.getCitaId());
            if (cita.isPresent()) {
                model.addAttribute("cita", cita.get());
                model.addAttribute("paciente", cita.get().getPaciente());
                model.addAttribute("medico", cita.get().getMedico());
            }
            model.addAttribute("tratamientoDTO", tratamientoDTO);
            model.addAttribute("estadosTratamiento", EstadoTratamiento.values());
            return "medico/tratamientos/nuevo-tratamiento";
        }
    }
    
    // NUEVO ENDPOINT PARA CREAR DESDE PANEL MÉDICO (FORMULARIO GENERAL)
    @PostMapping("/medico/crear")
    public String crearTratamientoDesdePanel(@ModelAttribute TratamientoDTO tratamientoDTO,
                                           Principal principal,
                                           RedirectAttributes redirectAttrs,
                                           Model model) {
        try {
            System.out.println("=== INICIANDO CREACIÓN DESDE PANEL MÉDICO ===");
            System.out.println("Tratamiento DTO recibido:");
            System.out.println("- Paciente ID: " + tratamientoDTO.getPacienteId());
            System.out.println("- Médico ID: " + tratamientoDTO.getMedicoId());
            System.out.println("- Diagnóstico: " + (tratamientoDTO.getDiagnostico() != null ? "Sí" : "No"));
            System.out.println("- Descripción: " + (tratamientoDTO.getDescripcion() != null ? "Sí" : "No"));
            System.out.println("- Fecha Inicio: " + tratamientoDTO.getFechaInicio());
            
            // Validar que el paciente fue seleccionado
            if (tratamientoDTO.getPacienteId() == null) {
                redirectAttrs.addFlashAttribute("error", "Debe seleccionar un paciente");
                return "redirect:/tratamientos/medico/nuevo";
            }
            
            // Verificar que el médico logueado es el que está creando el tratamiento
            String email = principal.getName();
            Optional<User> usuario = userService.findByEmail(email);
            
            if (usuario.isEmpty()) {
                return "redirect:/login";
            }
            
            Optional<Medico> medicoOpt = medicoService.buscarPorUsuario(usuario.get());
            if (medicoOpt.isEmpty()) {
                redirectAttrs.addFlashAttribute("error", "Perfil de médico no encontrado");
                return "redirect:/medico/panel";
            }
            
            // Asegurar que el tratamiento sea creado por el médico logueado
            tratamientoDTO.setMedicoId(medicoOpt.get().getId());
            
            // Crear tratamiento usando el método del servicio
            Tratamiento tratamiento = tratamientoService.crearTratamientoCompleto(tratamientoDTO);
            
            redirectAttrs.addFlashAttribute("success", 
                "Tratamiento creado exitosamente para el paciente");
            
            return "redirect:/tratamientos/medico/" + tratamiento.getId();
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttrs.addFlashAttribute("error", 
                "Error al crear tratamiento: " + e.getMessage());
            
            // Recargar datos para el formulario en caso de error
            try {
                String email = principal.getName();
                Optional<User> usuario = userService.findByEmail(email);
                if (usuario.isPresent()) {
                    Optional<Medico> medicoOpt = medicoService.buscarPorUsuario(usuario.get());
                    if (medicoOpt.isPresent()) {
                        List<Cita> citasActivas = citaService.buscarCitasActivasMedico(medicoOpt.get().getId());
                        model.addAttribute("citas", citasActivas);
                        model.addAttribute("medico", medicoOpt.get());
                    }
                }
            } catch (Exception ex) {
                // Ignorar errores en recarga
            }
            
            model.addAttribute("tratamientoDTO", tratamientoDTO);
            model.addAttribute("estadosTratamiento", EstadoTratamiento.values());
            return "medico/tratamientos/formulario-general";
        }
    }
    
    // Ver detalle de tratamiento (médico)
    @GetMapping("/medico/{id}")
    public String verTratamientoMedico(@PathVariable Long id,
                                       Model model,
                                       Principal principal) {
        try {
            Optional<Tratamiento> tratamientoOpt = tratamientoService.buscarPorId(id);
            if (tratamientoOpt.isEmpty()) {
                model.addAttribute("error", "Tratamiento no encontrado");
                return "error";
            }
            
            Tratamiento tratamiento = tratamientoOpt.get();
            
            // Verificar permisos del médico
            String email = principal.getName();
            Optional<User> usuario = userService.findByEmail(email);
            if (usuario.isPresent()) {
                Optional<Medico> medicoOpt = medicoService.buscarPorUsuario(usuario.get());
                if (medicoOpt.isPresent() && !medicoOpt.get().getId().equals(tratamiento.getMedico().getId())) {
                    model.addAttribute("error", "No tiene permisos para ver este tratamiento");
                    return "error";
                }
            }
            
            model.addAttribute("tratamiento", tratamiento);
            model.addAttribute("recetas", tratamiento.getRecetas());
            model.addAttribute("seguimientos", tratamiento.getSeguimientos());
            
            return "medico/tratamientos/detalle";
            
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar tratamiento: " + e.getMessage());
            return "error";
        }
    }
    
    // Listar tratamientos del médico
    @GetMapping("/medico")
    public String listarTratamientosMedico(Model model,
                                           Principal principal,
                                           @RequestParam(required = false) String estado) {
        try {
            String email = principal.getName();
            Optional<User> usuario = userService.findByEmail(email);
            
            if (usuario.isEmpty()) {
                return "redirect:/login";
            }
            
            Optional<Medico> medicoOpt = medicoService.buscarPorUsuario(usuario.get());
            if (medicoOpt.isEmpty()) {
                model.addAttribute("error", "Perfil de médico no encontrado");
                return "error";
            }
            
            Medico medico = medicoOpt.get();
            List<Tratamiento> tratamientos;
            
            if (estado != null && !estado.isEmpty()) {
                try {
                    EstadoTratamiento estadoEnum = EstadoTratamiento.valueOf(estado.toUpperCase());
                    tratamientos = tratamientoService.buscarPorMedico(medico.getId()).stream()
                        .filter(t -> t.getEstado() == estadoEnum)
                        .toList();
                } catch (IllegalArgumentException e) {
                    tratamientos = tratamientoService.buscarPorMedico(medico.getId());
                }
            } else {
                tratamientos = tratamientoService.buscarPorMedico(medico.getId());
            }
            
            // Estadísticas
            long total = tratamientos.size();
            long activos = tratamientos.stream().filter(t -> t.getEstado() == EstadoTratamiento.ACTIVO).count();
            long completados = tratamientos.stream().filter(t -> t.getEstado() == EstadoTratamiento.COMPLETADO).count();
            
            model.addAttribute("medico", medico);
            model.addAttribute("tratamientos", tratamientos);
            model.addAttribute("estado", estado);
            model.addAttribute("estadosTratamiento", EstadoTratamiento.values());
            model.addAttribute("total", total);
            model.addAttribute("activos", activos);
            model.addAttribute("completados", completados);
            
            return "medico/tratamientos/lista";
            
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar tratamientos: " + e.getMessage());
            return "error";
        }
    }
    
    // ========== VISTAS PARA PACIENTE ==========
    
    // Ver tratamiento del paciente
    @GetMapping("/{id}")
    public String verTratamientoPaciente(@PathVariable Long id,
                                         Model model,
                                         Principal principal) {
        try {
            Optional<Tratamiento> tratamientoOpt = tratamientoService.buscarPorId(id);
            if (tratamientoOpt.isEmpty()) {
                model.addAttribute("error", "Tratamiento no encontrado");
                return "error";
            }
            
            Tratamiento tratamiento = tratamientoOpt.get();
            
            // Verificar que el paciente tiene acceso
            String email = principal.getName();
            Optional<User> usuario = userService.findByEmail(email);
            if (usuario.isPresent()) {
                Optional<Paciente> pacienteOpt = pacienteService.buscarPorUsuario(usuario.get());
                if (pacienteOpt.isPresent() && !pacienteOpt.get().getId().equals(tratamiento.getPaciente().getId())) {
                    model.addAttribute("error", "No tiene permisos para ver este tratamiento");
                    return "error";
                }
            }
            
            model.addAttribute("tratamiento", tratamiento);
            model.addAttribute("recetas", tratamiento.getRecetas());
            
            return "pacientes/tratamientos/detalle";
            
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar tratamiento: " + e.getMessage());
            return "error";
        }
    }
    
    // Listar tratamientos del paciente
    @GetMapping("/paciente/mis-tratamientos")
    public String listarMisTratamientos(Model model,
                                        Principal principal,
                                        @RequestParam(required = false) String estado) {
        try {
            String email = principal.getName();
            Optional<User> usuario = userService.findByEmail(email);
            
            if (usuario.isEmpty()) {
                return "redirect:/login";
            }
            
            Optional<Paciente> pacienteOpt = pacienteService.buscarPorUsuario(usuario.get());
            if (pacienteOpt.isEmpty()) {
                model.addAttribute("error", "Perfil de paciente no encontrado");
                return "error";
            }
            
            Paciente paciente = pacienteOpt.get();
            List<Tratamiento> tratamientos;
            
            if (estado != null && !estado.isEmpty()) {
                try {
                    EstadoTratamiento estadoEnum = EstadoTratamiento.valueOf(estado.toUpperCase());
                    tratamientos = tratamientoService.buscarPorPacienteYEstado(paciente.getId(), estadoEnum);
                } catch (IllegalArgumentException e) {
                    tratamientos = tratamientoService.buscarPorPaciente(paciente.getId());
                }
            } else {
                tratamientos = tratamientoService.buscarPorPaciente(paciente.getId());
            }
            
            // Obtener tratamiento activo actual
            Optional<Tratamiento> tratamientoActivo = tratamientoService.obtenerTratamientoActivo(paciente.getId());
            
            model.addAttribute("paciente", paciente);
            model.addAttribute("tratamientos", tratamientos);
            model.addAttribute("tratamientoActivo", tratamientoActivo.orElse(null));
            model.addAttribute("estado", estado);
            model.addAttribute("estadosTratamiento", EstadoTratamiento.values());
            
            return "pacientes/tratamientos/lista";
            
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar tratamientos: " + e.getMessage());
            return "error";
        }
    }
    
    // ========== NUEVO ENDPOINT - Crear tratamiento desde panel médico ==========

    @GetMapping("/medico/nuevo")
    public String mostrarFormularioNuevoTratamiento(Model model, Principal principal) {
        try {
            String email = principal.getName();
            System.out.println("=== FORMULARIO TRATAMIENTO ===");
            
            Optional<User> usuario = userService.findByEmail(email);
            
            if (usuario.isEmpty()) {
                return "redirect:/login";
            }
            
            Optional<Medico> medicoOpt = medicoService.buscarPorUsuario(usuario.get());
            if (medicoOpt.isEmpty()) {
                model.addAttribute("error", "Perfil de médico no encontrado");
                return "error";
            }
            
            Medico medico = medicoOpt.get();
            
            // NO PASAR lista de pacientes - se buscará por DNI
            List<Cita> citasActivas = citaService.buscarCitasActivasMedico(medico.getId());
            
            TratamientoDTO tratamientoDTO = new TratamientoDTO();
            tratamientoDTO.setMedicoId(medico.getId());
            tratamientoDTO.setFechaInicio(LocalDate.now());
            
            model.addAttribute("tratamientoDTO", tratamientoDTO);
            model.addAttribute("medico", medico);
            model.addAttribute("citas", citasActivas);
            model.addAttribute("estadosTratamiento", EstadoTratamiento.values());
            model.addAttribute("modo", "nuevo");
            
            return "medico/tratamientos/formulario-general";
            
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar formulario: " + e.getMessage());
            return "error";
        }
    }
    
    // ========== ACCIONES ==========
    
    // Agregar receta a tratamiento existente
    @GetMapping("/{tratamientoId}/nueva-receta")
    public String formularioNuevaReceta(@PathVariable Long tratamientoId,
                                       Model model,
                                       Principal principal) {
        try {
            Optional<Tratamiento> tratamientoOpt = tratamientoService.buscarPorId(tratamientoId);
            if (tratamientoOpt.isEmpty()) {
                model.addAttribute("error", "Tratamiento no encontrado");
                return "error";
            }
            
            Tratamiento tratamiento = tratamientoOpt.get();
            
            // Verificar permisos
            String email = principal.getName();
            Optional<User> usuario = userService.findByEmail(email);
            if (usuario.isPresent()) {
                Optional<Medico> medicoOpt = medicoService.buscarPorUsuario(usuario.get());
                if (medicoOpt.isEmpty() || !medicoOpt.get().getId().equals(tratamiento.getMedico().getId())) {
                    model.addAttribute("error", "No tiene permisos para agregar recetas a este tratamiento");
                    return "error";
                }
            }
            
            // Preparar DTO
            RecetaDTO recetaDTO = new RecetaDTO();
            recetaDTO.setTratamientoId(tratamientoId);
            recetaDTO.setPacienteId(tratamiento.getPaciente().getId());
            recetaDTO.setMedicoId(tratamiento.getMedico().getId());
            recetaDTO.setCitaId(tratamiento.getCita().getId());
            recetaDTO.setFechaEmision(LocalDate.now());
            recetaDTO.setFechaCaducidad(LocalDate.now().plusDays(30));
            
            model.addAttribute("tratamiento", tratamiento);
            model.addAttribute("recetaDTO", recetaDTO);
            
            return "medico/tratamientos/nueva-receta";
            
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar formulario: " + e.getMessage());
            return "error";
        }
    }
    
    // Cambiar estado de tratamiento
    @PostMapping("/{id}/cambiar-estado")
    public String cambiarEstado(@PathVariable Long id,
                               @RequestParam EstadoTratamiento estado,
                               @RequestParam(required = false) String observaciones,
                               RedirectAttributes redirectAttrs,
                               Principal principal) {
        try {
            Tratamiento tratamiento = tratamientoService.actualizarEstado(id, estado, observaciones);
            
            redirectAttrs.addFlashAttribute("success", 
                "Estado del tratamiento actualizado a: " + estado.getDescripcion());
            
            // Redirigir según el rol
            String email = principal.getName();
            Optional<User> usuario = userService.findByEmail(email);
            
            if (usuario.isPresent() && usuario.get().getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_MEDICO"))) {
                return "redirect:/tratamientos/medico/" + id;
            } else {
                return "redirect:/tratamientos/" + id;
            }
            
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Error al cambiar estado: " + e.getMessage());
            return "redirect:/tratamientos/" + id;
        }
    }
    
    // Agregar seguimiento
    @PostMapping("/{id}/agregar-seguimiento")
    public String agregarSeguimiento(@PathVariable Long id,
                                    @RequestParam String observaciones,
                                    RedirectAttributes redirectAttrs,
                                    Principal principal) {
        try {
            // Obtener médico del usuario
            String email = principal.getName();
            Optional<User> usuario = userService.findByEmail(email);
            if (usuario.isEmpty()) {
                return "redirect:/login";
            }
            
            Optional<Medico> medicoOpt = medicoService.buscarPorUsuario(usuario.get());
            if (medicoOpt.isEmpty()) {
                redirectAttrs.addFlashAttribute("error", "Debe ser médico para agregar seguimientos");
                return "redirect:/tratamientos/" + id;
            }
            
            Tratamiento tratamiento = tratamientoService.agregarSeguimiento(id, observaciones, medicoOpt.get().getId());
            
            redirectAttrs.addFlashAttribute("success", "Seguimiento agregado correctamente");
            return "redirect:/tratamientos/medico/" + id;
            
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Error al agregar seguimiento: " + e.getMessage());
            return "redirect:/tratamientos/" + id;
        }
    }
}