package com.example.demo.controller;

import com.example.demo.Entitys.*;
import com.example.demo.dto.CitaDTO;
import com.example.demo.dto.RecetaDTO;
import com.example.demo.enums.EstadoCita;
import com.example.demo.service.CitaService;
import com.example.demo.service.MedicoService;
import com.example.demo.service.PacienteService;
import com.example.demo.service.TratamientoService;
import com.example.demo.service.UserService;

import java.util.Arrays;
import java.util.Collections;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/citas")
public class CitaController {

    @Autowired
    private CitaService citaService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private TratamientoService tratamientoService;
    
    @Autowired
    private PacienteService pacienteService;
    
    @Autowired
    private MedicoService medicoService;

    // ==================== FORMULARIO NUEVA CITA ====================
    @GetMapping("/nueva")
    public String mostrarFormularioNuevaCita(Model model, 
                                            Authentication authentication,
                                            Principal principal) {
        
        try {
            // Siempre necesitamos médicos para todos los roles
            List<Medico> medicos = medicoService.listarTodos();
            model.addAttribute("medicos", medicos);
            
            // Agregar el DTO vacío al modelo
            model.addAttribute("citaDTO", new CitaDTO());
            
            if (authentication != null) {
                // Verificar si es PACIENTE
                boolean esPaciente = authentication.getAuthorities()
                    .stream().anyMatch(a -> a.getAuthority().equals("ROLE_PACIENTE"));
                
                // Verificar si es ADMIN
                boolean esAdmin = authentication.getAuthorities()
                    .stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                
                if (esPaciente) {
                    // ===== PARA PACIENTES =====
                    Optional<User> usuarioOpt = userService.findByEmail(principal.getName());
                    if (usuarioOpt.isEmpty()) {
                        return "redirect:/login?error=Usuario no encontrado";
                    }
                    
                    Optional<Paciente> pacienteOpt = pacienteService.buscarPorUsuario(usuarioOpt.get());
                    if (pacienteOpt.isEmpty()) {
                        return "redirect:/perfil/completar?error=Complete su perfil de paciente primero";
                    }
                    
                    Paciente paciente = pacienteOpt.get();
                    model.addAttribute("pacienteActual", paciente);
                    
                } else if (esAdmin) {
                    // ===== PARA ADMINISTRADORES =====
                    List<Paciente> pacientes = pacienteService.listarTodos();
                    model.addAttribute("pacientes", pacientes);
                    
                } else {
                    // ===== PARA OTROS ROLES (médico, recepcionista) =====
                    List<Paciente> pacientes = pacienteService.listarTodos();
                    model.addAttribute("pacientes", pacientes);
                }
                
            } else {
                // Usuario no autenticado
                return "redirect:/login";
            }
            
            return "citas/nueva-cita";
            
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar el formulario: " + e.getMessage());
            return "error";
        }
    }

 // ==================== GUARDAR CITA ====================
    @PostMapping("/guardar")
    public String guardarCita(@Valid @ModelAttribute("citaDTO") CitaDTO citaDTO,
                             BindingResult result,
                             Authentication authentication,
                             Principal principal,
                             RedirectAttributes redirectAttrs,
                             Model model) {
        
        try {
            // Si hay errores de validación, volver al formulario
            if (result.hasErrors()) {
                // Recargar datos necesarios para el formulario
                List<Medico> medicos = medicoService.listarTodos();
                model.addAttribute("medicos", medicos);
                
                if (authentication != null) {
                    boolean esPaciente = authentication.getAuthorities()
                        .stream().anyMatch(a -> a.getAuthority().equals("ROLE_PACIENTE"));
                    
                    if (esPaciente) {
                        Optional<User> usuarioOpt = userService.findByEmail(principal.getName());
                        if (usuarioOpt.isPresent()) {
                            Optional<Paciente> pacienteOpt = pacienteService.buscarPorUsuario(usuarioOpt.get());
                            pacienteOpt.ifPresent(paciente -> model.addAttribute("pacienteActual", paciente));
                        }
                    } else {
                        List<Paciente> pacientes = pacienteService.listarTodos();
                        model.addAttribute("pacientes", pacientes);
                    }
                }
                
                return "citas/nueva-cita";
            }
            
            // Validar datos básicos
            if (citaDTO.getMedicoId() == null || citaDTO.getFechaHora() == null) {
                redirectAttrs.addFlashAttribute("error", "Datos incompletos");
                return "redirect:/citas/nueva";
            }
            
            // 1. Obtener médico
            Optional<Medico> medicoOpt = medicoService.buscarPorId(citaDTO.getMedicoId());
            if (medicoOpt.isEmpty()) {
                redirectAttrs.addFlashAttribute("error", "Médico no encontrado");
                return "redirect:/citas/nueva";
            }
            Medico medico = medicoOpt.get();
            
            // 2. Manejar paciente según rol
            if (authentication != null && authentication.getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_PACIENTE"))) {
                
                // Para PACIENTE: obtener su ID automáticamente
                Optional<User> usuarioOpt = userService.findByEmail(principal.getName());
                if (usuarioOpt.isPresent()) {
                    Optional<Paciente> pacienteOpt = pacienteService.buscarPorUsuario(usuarioOpt.get());
                    if (pacienteOpt.isPresent()) {
                        // Sobrescribir el pacienteId con el ID real del paciente autenticado
                        citaDTO.setPacienteId(pacienteOpt.get().getId());
                    } else {
                        redirectAttrs.addFlashAttribute("error", "Paciente no encontrado");
                        return "redirect:/citas/nueva";
                    }
                }
            }
            
            // 3. Validar que pacienteId no sea nulo
            if (citaDTO.getPacienteId() == null) {
                redirectAttrs.addFlashAttribute("error", "Debe seleccionar un paciente");
                return "redirect:/citas/nueva";
            }
            
            // 4. Validar tarifa (si es null o menor a la del médico, usar tarifa del médico)
            if (citaDTO.getTarifaAplicada() == null || 
                citaDTO.getTarifaAplicada().compareTo(medico.getTarifaConsulta()) < 0) {
                citaDTO.setTarifaAplicada(medico.getTarifaConsulta());
            }
            
            // 5. CORRECCIÓN: Validar y corregir el estado según tu enum
            if (citaDTO.getEstado() == null || citaDTO.getEstado().isEmpty()) {
                citaDTO.setEstado("PROGRAMADA");
            } else {
                // Mapear valores incorrectos a valores correctos del enum
                String estado = citaDTO.getEstado();
                switch (estado.toUpperCase()) {
                    case "CONFIRMED":
                        citaDTO.setEstado("CONFIRMADA");
                        break;
                    case "PENDING":
                        citaDTO.setEstado("PENDIENTE");
                        break;
                    case "CANCELLED":
                        citaDTO.setEstado("CANCELADA");
                        break;
                    case "COMPLETED":
                        citaDTO.setEstado("COMPLETADA");
                        break;
                    // Si ya está correcto, dejarlo como está
                }
            }
            
            // 6. Validar disponibilidad del médico
            if (!citaService.validarDisponibilidadMedico(citaDTO.getMedicoId(), citaDTO.getFechaHora())) {
                redirectAttrs.addFlashAttribute("error", "El médico no está disponible en ese horario");
                return "redirect:/citas/nueva";
            }
            
            // 7. Guardar la cita
            citaService.guardarCita(citaDTO);
            
            // 8. Redirigir según rol
            if (authentication != null && authentication.getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_PACIENTE"))) {
                redirectAttrs.addFlashAttribute("success", "¡Cita agendada exitosamente!");
                return "redirect:/citas/mis-citas";
            } else {
                redirectAttrs.addFlashAttribute("success", "Cita creada exitosamente");
                return "redirect:/citas";
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttrs.addFlashAttribute("error", "Error al guardar la cita: " + e.getMessage());
            return "redirect:/citas/nueva";
        }
    }

    @GetMapping("/mis-citas")
    public String mostrarMisCitas(Model model, Principal principal,
                                 @RequestParam(value = "estado", required = false) String estado) {
        
        // 1. Obtener paciente
        String email = principal.getName();
        User usuario = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        Paciente paciente = pacienteService.buscarPorUsuario(usuario)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
        
        // 2. Obtener citas (filtradas si hay estado)
        List<Cita> citas;
        if (estado != null && !estado.isEmpty()) {
            citas = citaService.obtenerCitasPorPacienteYEstado(paciente, estado);
        } else {
            citas = citaService.obtenerCitasPorPaciente(paciente);
        }
        
        // 3. Calcular estadísticas
        long totalCitas = citas.size();
        long confirmadas = citas.stream().filter(c -> "CONFIRMADA".equals(c.getEstado())).count();
        long pendientes = citas.stream().filter(c -> "PROGRAMADA".equals(c.getEstado()) || "PENDIENTE".equals(c.getEstado())).count();
        long canceladas = citas.stream().filter(c -> "CANCELADA".equals(c.getEstado())).count();
        
        // 4. Pasar datos al modelo
        model.addAttribute("paciente", paciente);
        model.addAttribute("citas", citas);
        model.addAttribute("totalCitas", totalCitas);
        model.addAttribute("confirmadas", confirmadas);
        model.addAttribute("pendientes", pendientes);
        model.addAttribute("canceladas", canceladas);
        model.addAttribute("estado", estado);
        model.addAttribute("estadosCita", Arrays.asList("CONFIRMADA", "PROGRAMADA", "PENDIENTE", "CANCELADA", "COMPLETADA"));
        
        // IMPORTANTE: Cambia la ruta aquí
        return "AccionesPaciente/mis-citas";  // ← Esta es la corrección
    }
    
 // En CitaController.java, agrega estos métodos:

 // Ver detalle de cita con opciones de tratamiento
 @GetMapping("/{id}/detalle")
 public String verDetalleCita(@PathVariable Long id, Model model, Principal principal) {
     try {
         Optional<Cita> citaOpt = citaService.buscarPorId(id);
         if (citaOpt.isEmpty()) {
             return "redirect:/citas?error=Cita no encontrada";
         }
         
         Cita cita = citaOpt.get();
         
         // Verificar que el médico tiene acceso
         String email = principal.getName();
         Optional<User> usuario = userService.findByEmail(email);
         if (usuario.isEmpty()) {
             return "redirect:/login";
         }
         
         // Verificar si es médico de esta cita
         Optional<Medico> medicoOpt = medicoService.buscarPorUsuario(usuario.get());
         boolean esMedicoDeCita = medicoOpt.isPresent() && 
                                 medicoOpt.get().getId().equals(cita.getMedico().getId());
         
         if (!esMedicoDeCita) {
             model.addAttribute("error", "No tiene permisos para ver esta cita");
             return "error";
         }
         
         // Buscar si ya existe tratamiento para esta cita
         Optional<Tratamiento> tratamientoOpt = tratamientoService.buscarPorCita(id);
         
         model.addAttribute("cita", cita);
         model.addAttribute("tratamiento", tratamientoOpt.orElse(null));
         model.addAttribute("medico", medicoOpt.get());
         
         return "citas/detalle-medico";
         
     } catch (Exception e) {
         model.addAttribute("error", "Error al cargar cita: " + e.getMessage());
         return "error";
     }
 }

 // Formulario rápido para receta
 @GetMapping("/{id}/receta-rapida")
 public String recetaRapida(@PathVariable Long id, Model model, Principal principal) {
     try {
         Optional<Cita> citaOpt = citaService.buscarPorId(id);
         if (citaOpt.isEmpty()) {
             return "redirect:/citas?error=Cita no encontrada";
         }
         
         Cita cita = citaOpt.get();
         
         // Verificar que el médico tiene acceso
         String email = principal.getName();
         Optional<User> usuario = userService.findByEmail(email);
         if (usuario.isEmpty()) {
             return "redirect:/login";
         }
         
         Optional<Medico> medicoOpt = medicoService.buscarPorUsuario(usuario.get());
         boolean esMedicoDeCita = medicoOpt.isPresent() && 
                                 medicoOpt.get().getId().equals(cita.getMedico().getId());
         
         if (!esMedicoDeCita) {
             model.addAttribute("error", "No tiene permisos para crear receta");
             return "error";
         }
         
         // Preparar DTO para receta
         RecetaDTO recetaDTO = new RecetaDTO();
         recetaDTO.setCitaId(id);
         recetaDTO.setPacienteId(cita.getPaciente().getId());
         recetaDTO.setMedicoId(cita.getMedico().getId());
         
         // Verificar si hay tratamiento existente
         Optional<Tratamiento> tratamientoOpt = tratamientoService.buscarPorCita(id);
         tratamientoOpt.ifPresent(t -> recetaDTO.setTratamientoId(t.getId()));
         
         model.addAttribute("cita", cita);
         model.addAttribute("recetaDTO", recetaDTO);
         model.addAttribute("medicamentosComunes", getMedicamentosComunes());
         
         return "medico/recetas/rapida";
         
     } catch (Exception e) {
         model.addAttribute("error", "Error: " + e.getMessage());
         return "error";
     }
 }

 private List<String> getMedicamentosComunes() {
     return Arrays.asList(
         "Paracetamol", "Ibuprofeno", "Amoxicilina", "Omeprazol",
         "Losartán", "Atorvastatina", "Metformina", "Salbutamol",
         "Aspirina", "Diclofenaco", "Loratadina", "Prednisona"
     );
 }

    // ==================== LISTADO GENERAL (ADMIN) ====================
    @GetMapping
    public String listarCitas(
            @RequestParam(required = false) LocalDate fecha,
            @RequestParam(required = false) Long medicoId,
            @RequestParam(required = false) EstadoCita estado,
            Model model) {
        
        try {
            List<Cita> citas;
            if (fecha != null || medicoId != null || estado != null) {
                citas = citaService.filtrarCitas(fecha, medicoId, estado);
            } else {
                citas = citaService.listarTodas();
            }
            
            // Calcular estadísticas
            long total = citas.size();
            long hoy = citas.stream()
                .filter(c -> c.getFechaHora().toLocalDate().equals(LocalDate.now()))
                .count();
            long pendientes = citas.stream()
                .filter(c -> c.getEstado() == EstadoCita.PROGRAMADA || 
                            c.getEstado() == EstadoCita.PENDIENTE)
                .count();
            long canceladas = citas.stream()
                .filter(c -> c.getEstado() == EstadoCita.CANCELADA)
                .count();
            
            model.addAttribute("citas", citas);
            model.addAttribute("medicos", medicoService.listarTodos());
            model.addAttribute("fecha", fecha);
            model.addAttribute("medicoId", medicoId);
            model.addAttribute("estado", estado);
            model.addAttribute("estadosCita", EstadoCita.values());
            model.addAttribute("totalCitas", total);
            model.addAttribute("citasHoy", hoy);
            model.addAttribute("pendientes", pendientes);
            model.addAttribute("canceladas", canceladas);
            
            return "citas/lista";
            
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar las citas: " + e.getMessage());
            return "error";
        }
    }
    
    @GetMapping("/medico/panel")
    public String medicoPanel(Model model, Principal principal) {
        try {
            String email = principal.getName();
            
            // Buscar médico
            Optional<User> usuarioOpt = userService.findByEmail(email);
            Optional<Medico> medicoOpt = Optional.empty();
            
            if (usuarioOpt.isPresent()) {
                medicoOpt = medicoService.buscarPorUsuario(usuarioOpt.get());
            }
            
            if (!medicoOpt.isPresent()) {
                medicoOpt = medicoService.obtenerPorEmail(email);
            }
            
            if (!medicoOpt.isPresent()) {
                return "redirect:/login?error=Médico no encontrado";
            }
            
            Medico medico = medicoOpt.get();
            
            // Obtener estadísticas
            long citasHoy = citaService.obtenerCitasHoyPorMedico(medico.getId()).size();
            long totalPacientes = citaService.contarPacientesUnicosPorMedico(medico.getId());
            
            // Obtener ingresos del mes actual
            LocalDate now = LocalDate.now();
            BigDecimal ingresosMes = citaService.calcularIngresosMesPorMedico(
                medico.getId(), now.getYear(), now.getMonthValue());
            
            // Obtener citas de hoy
            List<Cita> citasHoyLista = citaService.obtenerCitasHoyPorMedico(medico.getId());
            
            // Obtener pacientes recientes
            List<Paciente> pacientesRecientes = citaService.obtenerPacientesRecientesPorMedico(medico.getId());
            
            // Pasar datos al modelo
            model.addAttribute("medico", medico);
            model.addAttribute("citasHoy", citasHoy);
            model.addAttribute("totalPacientes", totalPacientes);
            model.addAttribute("ingresosMes", ingresosMes != null ? ingresosMes : BigDecimal.ZERO);
            model.addAttribute("citasHoyLista", citasHoyLista);
            model.addAttribute("pacientesRecientes", pacientesRecientes);
            
            return "medico/panel";
            
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar el panel");
            return "error";
        }
    }

    // ==================== CITAS DEL MÉDICO ====================
    @GetMapping("/medico")
    public String citasMedico(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            Model model, 
            Principal principal,
            Authentication authentication) {
        
        try {
            System.out.println("=== CITAS DEL MÉDICO - INICIO ===");
            
            String email = principal.getName();
            System.out.println("Email autenticado: " + email);
            
            // PRIMERO: Buscar usuario en BD
            Optional<User> usuarioOpt = userService.findByEmail(email);
            if (usuarioOpt.isEmpty()) {
                System.out.println("ERROR: Usuario no encontrado en BD");
                model.addAttribute("error", "Usuario no encontrado");
                return "error";
            }
            
            User user = usuarioOpt.get();
            System.out.println("Usuario BD ID: " + user.getId());
            
            // SEGUNDO: Buscar médico (múltiples métodos)
            Optional<Medico> medicoOpt = null;
            
            // Método 1: Por usuario
            medicoOpt = medicoService.buscarPorUsuario(user);
            if (medicoOpt.isPresent()) {
                System.out.println("Médico encontrado por usuario");
            } else {
                // Método 2: Por email
                System.out.println("Buscando médico por email: " + email);
                medicoOpt = medicoService.obtenerPorEmail(email);
                
                if (medicoOpt.isPresent()) {
                    System.out.println("Médico encontrado por email");
                    // IMPORTANTE: Asociar el usuario al médico si no está asociado
                    Medico medico = medicoOpt.get();
                    if (medico.getUsuario() == null) {
                        System.out.println("Asociando usuario al médico...");
                        medico.setUsuario(user);
                        medicoService.guardar(medico);
                        System.out.println("Usuario asociado exitosamente");
                    }
                } else {
                    // Método 3: Por coincidencia de nombre/email
                    System.out.println("Buscando médico por coincidencia...");
                    List<Medico> todosMedicos = medicoService.listarTodos();
                    for (Medico m : todosMedicos) {
                        if (m.getEmail().equalsIgnoreCase(email)) {
                            medicoOpt = Optional.of(m);
                            System.out.println("Médico encontrado por coincidencia de email");
                            break;
                        }
                    }
                }
            }
            
            if (medicoOpt.isEmpty()) {
                System.out.println("ERROR: No se encontró médico para: " + email);
                model.addAttribute("error", "No se encontró un perfil de médico asociado a su cuenta. Contacte al administrador.");
                return "error";
            }
            
            Medico medico = medicoOpt.get();
            System.out.println("MÉDICO SELECCIONADO: ID=" + medico.getId() + ", Nombre=" + medico.getNombre());
            
            // TERCERO: Obtener citas
            LocalDate fechaConsulta = fecha != null ? fecha : LocalDate.now();
            System.out.println("Fecha consulta: " + fechaConsulta);
            
            List<Cita> citas = citaService.obtenerCitasPorMedicoYFecha(medico.getId(), fechaConsulta);
            System.out.println("Citas encontradas: " + citas.size());
            
            // CUARTO: Verificar todas las citas del médico
            List<Cita> todasCitas = citaService.listarTodas();
            System.out.println("=== TODAS LAS CITAS EN EL SISTEMA ===");
            int contadorCitasMedico = 0;
            for (Cita c : todasCitas) {
                if (c.getMedico() != null && c.getMedico().getId().equals(medico.getId())) {
                    contadorCitasMedico++;
                    System.out.println("Cita ID: " + c.getId() + 
                                     ", Paciente: " + c.getPaciente().getNombre() +
                                     ", Fecha: " + c.getFechaHora() +
                                     ", Estado: " + c.getEstado());
                }
            }
            System.out.println("Total citas de este médico en sistema: " + contadorCitasMedico);
            
            // QUINTO: Pasar datos al modelo
            model.addAttribute("citas", citas);
            model.addAttribute("medico", medico);
            model.addAttribute("fecha", fechaConsulta);
            model.addAttribute("totalTodasCitas", contadorCitasMedico);
            
            System.out.println("=== FIN ===");
            return "citas/medico";
            
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error: " + e.getMessage());
            return "error";
        }
    }
    @GetMapping("/debug/medico")
    @ResponseBody
    public String debugMedico(Principal principal) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>Debug - Información Médico</h1>");
        
        try {
            // 1. Info del usuario autenticado
            sb.append("<h2>1. Usuario Autenticado</h2>");
            String email = principal.getName();
            sb.append("<p>Email autenticado: ").append(email).append("</p>");
            
            // 2. Buscar usuario en BD
            Optional<User> usuarioOpt = userService.findByEmail(email);
            if (usuarioOpt.isPresent()) {
                User user = usuarioOpt.get();
                sb.append("<p>Usuario encontrado en BD:</p>");
                sb.append("<ul>");
                sb.append("<li>ID: ").append(user.getId()).append("</li>");
                sb.append("<li>Email: ").append(user.getEmail()).append("</li>");
                sb.append("<li>Roles: ").append(user.getRoles()).append("</li>");
                sb.append("</ul>");
                
                // 3. Buscar médico por usuario
                sb.append("<h2>2. Buscar Médico por Usuario</h2>");
                Optional<Medico> medicoOpt = medicoService.buscarPorUsuario(user);
                
                if (medicoOpt.isPresent()) {
                    Medico medico = medicoOpt.get();
                    sb.append("<p>¡MÉDICO ENCONTRADO!</p>");
                    sb.append("<ul>");
                    sb.append("<li>ID Médico: ").append(medico.getId()).append("</li>");
                    sb.append("<li>Nombre: ").append(medico.getNombre()).append(" ").append(medico.getApellido()).append("</li>");
                    sb.append("<li>Email: ").append(medico.getEmail()).append("</li>");
                    sb.append("</ul>");
                    
                    // 4. Buscar citas de este médico
                    sb.append("<h2>3. Citas del Médico</h2>");
                    List<Cita> citasMedico = citaService.obtenerCitasPorMedicoId(medico.getId());
                    sb.append("<p>Total citas encontradas: ").append(citasMedico.size()).append("</p>");
                    
                    if (!citasMedico.isEmpty()) {
                        sb.append("<table border='1'><tr><th>ID Cita</th><th>Fecha</th><th>Paciente</th><th>Estado</th></tr>");
                        for (Cita cita : citasMedico) {
                            sb.append("<tr>");
                            sb.append("<td>").append(cita.getId()).append("</td>");
                            sb.append("<td>").append(cita.getFechaHora()).append("</td>");
                            sb.append("<td>").append(cita.getPaciente().getNombre()).append(" ").append(cita.getPaciente().getApellido()).append("</td>");
                            sb.append("<td>").append(cita.getEstado()).append("</td>");
                            sb.append("</tr>");
                        }
                        sb.append("</table>");
                    }
                } else {
                    sb.append("<p style='color:red'>NO se encontró médico para este usuario</p>");
                    
                    // 5. Buscar médico por email
                    sb.append("<h2>3. Buscar Médico por Email (").append(email).append(")</h2>");
                    Optional<Medico> medicoPorEmail = medicoService.obtenerPorEmail(email);
                    
                    if (medicoPorEmail.isPresent()) {
                        sb.append("<p>¡MÉDICO ENCONTRADO POR EMAIL!</p>");
                        Medico medico = medicoPorEmail.get();
                        sb.append("<ul>");
                        sb.append("<li>ID Médico: ").append(medico.getId()).append("</li>");
                        sb.append("<li>Nombre: ").append(medico.getNombre()).append(" ").append(medico.getApellido()).append("</li>");
                        sb.append("<li>Email: ").append(medico.getEmail()).append("</li>");
                        sb.append("<li>Usuario asociado: ").append(medico.getUsuario() != null ? medico.getUsuario().getEmail() : "NULL").append("</li>");
                        sb.append("</ul>");
                    } else {
                        sb.append("<p style='color:red'>No hay médico con ese email</p>");
                        
                        // 6. Mostrar todos los médicos disponibles
                        sb.append("<h2>4. Todos los Médicos en el Sistema</h2>");
                        List<Medico> todosMedicos = medicoService.listarTodos();
                        sb.append("<p>Total médicos: ").append(todosMedicos.size()).append("</p>");
                        
                        sb.append("<table border='1'><tr><th>ID</th><th>Nombre</th><th>Email</th><th>Usuario ID</th><th>Usuario Email</th></tr>");
                        for (Medico m : todosMedicos) {
                            sb.append("<tr>");
                            sb.append("<td>").append(m.getId()).append("</td>");
                            sb.append("<td>").append(m.getNombre()).append(" ").append(m.getApellido()).append("</td>");
                            sb.append("<td>").append(m.getEmail()).append("</td>");
                            sb.append("<td>").append(m.getUsuario() != null ? m.getUsuario().getId() : "null").append("</td>");
                            sb.append("<td>").append(m.getUsuario() != null ? m.getUsuario().getEmail() : "null").append("</td>");
                            sb.append("</tr>");
                        }
                        sb.append("</table>");
                    }
                }
            } else {
                sb.append("<p style='color:red'>Usuario no encontrado en BD: ").append(email).append("</p>");
            }
            
            return sb.toString();
            
        } catch (Exception e) {
            return "<h1>Error</h1><pre>" + e.getMessage() + "</pre>";
        }
    }
 // En tu CitaController.java - Agrega este método:

    @GetMapping("/api/citas/calendario/medico")
    @ResponseBody
    public List<Map<String, Object>> getCitasCalendarioMedico(Principal principal) {
        try {
            // 1. Obtener el médico autenticado
            String email = principal.getName();
            Optional<User> usuarioOpt = userService.findByEmail(email);
            if (usuarioOpt.isEmpty()) {
                return List.of();
            }
            
            // 2. Buscar médico por usuario o email
            Optional<Medico> medicoOpt = medicoService.buscarPorUsuario(usuarioOpt.get());
            if (!medicoOpt.isPresent()) {
                medicoOpt = medicoService.obtenerPorEmail(email);
            }
            
            if (medicoOpt.isEmpty()) {
                return List.of();
            }
            
            Medico medico = medicoOpt.get();
            
            // 3. Obtener citas del médico (último mes y próximo mes)
            LocalDateTime inicio = LocalDateTime.now().minusMonths(1);
            LocalDateTime fin = LocalDateTime.now().plusMonths(2);
            
            List<Cita> citas = citaService.listarTodas().stream()
                .filter(cita -> cita.getMedico() != null && 
                               cita.getMedico().getId().equals(medico.getId()) &&
                               cita.getFechaHora().isAfter(inicio) &&
                               cita.getFechaHora().isBefore(fin))
                .collect(Collectors.toList());
            
            // 4. Convertir a formato de calendario
            return citas.stream().map(cita -> {
                Map<String, Object> event = new HashMap<>();
                event.put("id", cita.getId());
                event.put("title", cita.getPaciente().getNombre() + " " + cita.getPaciente().getApellido());
                event.put("start", cita.getFechaHora().toString());
                event.put("end", cita.getFechaHora().plusHours(1).toString()); // Asumiendo 1 hora por cita
                event.put("extendedProps", Map.of(
                    "motivo", cita.getMotivo() != null ? cita.getMotivo() : "Consulta",
                    "estado", cita.getEstado().toString(),
                    "tarifa", cita.getTarifaAplicada() != null ? cita.getTarifaAplicada().toString() : "0"
                ));
                
                // Color según estado
                switch (cita.getEstado()) {
                    case CONFIRMADA:
                        event.put("backgroundColor", "#28a745");
                        event.put("borderColor", "#218838");
                        break;
                    case PROGRAMADA:
                        event.put("backgroundColor", "#007bff");
                        event.put("borderColor", "#0069d9");
                        break;
                    case PENDIENTE:
                        event.put("backgroundColor", "#ffc107");
                        event.put("borderColor", "#e0a800");
                        break;
                    case CANCELADA:
                        event.put("backgroundColor", "#dc3545");
                        event.put("borderColor", "#c82333");
                        break;
                    case COMPLETADA:
                        event.put("backgroundColor", "#6f42c1");
                        event.put("borderColor", "#5a32a3");
                        break;
                    default:
                        event.put("backgroundColor", "#6c757d");
                        event.put("borderColor", "#545b62");
                }
                
                return event;
            }).collect(Collectors.toList());
            
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }
    private String getColorByEstado(EstadoCita estado) {
        switch (estado) {
            case CONFIRMADA: return "#28a745";
            case PROGRAMADA: return "#007bff";
            case PENDIENTE: return "#ffc107";
            case CANCELADA: return "#dc3545";
            case COMPLETADA: return "#6f42c1";
            default: return "#6c757d";
        }
    }

    // ==================== OTRAS OPERACIONES ====================
    // (mantén tus métodos existentes para ver, editar, cancelar, etc.)
    
    // Detalle de cita
    @GetMapping("/{id}")
    public String verCita(@PathVariable Long id, Model model) {
        Optional<Cita> citaOpt = citaService.buscarPorId(id);
        if (citaOpt.isEmpty()) {
            return "redirect:/citas?error=Cita no encontrada";
        }
        
        model.addAttribute("cita", citaOpt.get());
        return "citas/detalle";
    }

    // Formulario para editar cita
    @GetMapping("/editar/{id}")
    public String editarCita(@PathVariable Long id, Model model) {
        Optional<Cita> citaOpt = citaService.buscarPorId(id);
        if (citaOpt.isEmpty()) {
            return "redirect:/citas?error=Cita no encontrada";
        }
        
        model.addAttribute("cita", citaOpt.get());
        model.addAttribute("medicos", medicoService.listarTodos());
        model.addAttribute("estadosCita", EstadoCita.values());
        
        return "citas/editar";
    }

    // Cancelar cita
    @GetMapping("/cancelar/{id}")
    public String cancelarCita(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            citaService.cancelarCita(id);
            redirectAttrs.addFlashAttribute("success", "Cita cancelada correctamente");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/citas";
    }

    // Confirmar cita
    @GetMapping("/confirmar/{id}")
    public String confirmarCita(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            citaService.confirmarCita(id);
            redirectAttrs.addFlashAttribute("success", "Cita confirmada correctamente");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/citas";
    }
}