package com.example.demo.controller;

import com.example.demo.Entitys.ExpedienteMedico;
import com.example.demo.Entitys.Medico;
import com.example.demo.Entitys.Paciente;
import com.example.demo.Repository.ExpedienteMedicoRepository;
import com.example.demo.Repository.PacienteRepository;
import com.example.demo.enums.Especialidad;
import com.example.demo.service.ExpedienteMedicoService;
import com.example.demo.service.PacienteService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/expedientes")
public class ExpedienteMedicoController {
    
    @Autowired
    private PacienteService pacienteService;

    @Autowired
    private ExpedienteMedicoService expedienteService;
    
    @Autowired
    private ExpedienteMedicoRepository expedienteRepository;
    
    @Autowired
    private PacienteRepository pacienteRepository;

    
    @GetMapping("/api/verificar/{pacienteId}")
    @ResponseBody
    public boolean verificarExpedienteApi(@PathVariable Long pacienteId) {
        return expedienteService.existeExpedienteParaPaciente(pacienteId);
    }

    // Ver expediente de paciente (para pacientes)
    @GetMapping("/mi-expediente")
    @PreAuthorize("hasRole('PACIENTE')")
    public String miExpediente(Model model, Principal principal) {
        String email = principal.getName();
        Optional<Paciente> pacienteOpt = pacienteService.buscarPorUsuarioEmail(email);
        
        if (pacienteOpt.isEmpty()) {
            return "redirect:/login?error=Paciente no encontrado";
        }
        
        Paciente paciente = pacienteOpt.get();
        Optional<ExpedienteMedico> expedienteOpt = expedienteService.buscarPorPacienteId(paciente.getId());
        
        if (expedienteOpt.isEmpty()) {
            return "redirect:/expedientes/solicitar";
        }
        
        model.addAttribute("expediente", expedienteOpt.get());
        model.addAttribute("paciente", paciente);
        
        return "expedientes/mi-expediente";
    }

    // Ver detalle de expediente específico
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'PACIENTE')")
    public String verExpediente(@PathVariable Long id, Model model, Principal principal) {
        Optional<ExpedienteMedico> expedienteOpt = expedienteService.obtenerPorId(id);
        
        if (expedienteOpt.isEmpty()) {
            return "redirect:/expedientes?error=Expediente no encontrado";
        }
        
        ExpedienteMedico expediente = expedienteOpt.get();
        
        // Verificar permisos
        String email = principal.getName();
        if (principal != null && pacienteService.buscarPorUsuarioEmail(email).isPresent()) {
            Paciente paciente = pacienteService.buscarPorUsuarioEmail(email).get();
            if (!expediente.getPaciente().getId().equals(paciente.getId())) {
                return "redirect:/expedientes/mi-expediente?error=No tiene permisos";
            }
        }
        
        model.addAttribute("expediente", expediente);
        
        return "expedientes/detalle";
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    public String mostrarFormularioExpediente(
            @RequestParam Long pacienteId,
            Model model,
            Principal principal,
            HttpServletRequest request) {
        
        // CORRECCIÓN: Usar buscarPorId en lugar de obtenerPorId
        Paciente paciente = pacienteService.buscarPorId(pacienteId)
            .orElseThrow(() -> new RuntimeException("Paciente no encontrado con ID: " + pacienteId));
        
        ExpedienteMedico expediente = new ExpedienteMedico();
        expediente.setPaciente(paciente);
        
        // Obtener médico
        Medico medico = obtenerMedicoDesdeSession(request, principal);
        
        model.addAttribute("expediente", expediente);
        model.addAttribute("paciente", paciente);
        model.addAttribute("medico", medico);
        model.addAttribute("tiposSangre", List.of("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"));
        
        return "expedientes/formulario";
    }
    
    private Medico obtenerMedicoDesdeSession(HttpServletRequest request, Principal principal) {
        Medico medico = (Medico) request.getSession().getAttribute("medico");
        
        if (medico == null) {
            medico = new Medico();
            medico.setNombre("Dr. Médico");
            medico.setApellido("Actual");
            medico.setEspecialidad(Especialidad.MEDICINA_GENERAL);
        }
        
        return medico;
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    public String listarExpedientes1(
            @RequestParam(required = false) String documento,
            @RequestParam(required = false) String nombre,
            Model model) {
        
        List<ExpedienteMedico> expedientes;
        if (documento != null && !documento.isEmpty()) {
            expedientes = expedienteService.buscarPorDocumentoPaciente(documento);
        } else if (nombre != null && !nombre.isEmpty()) {
            expedientes = expedienteService.buscarPorNombrePaciente(nombre);
        } else {
            expedientes = expedienteService.listarTodos();
        }
        
        // Calcular contadores en el Controller
        long activos = expedientes.stream()
            .filter(e -> "ACTIVO".equals(e.getEstado()))
            .count();
        
        long pendientes = expedientes.stream()
            .filter(e -> "PENDIENTE".equals(e.getEstado()))
            .count();
        
        model.addAttribute("listaExpedientes", expedientes);
        model.addAttribute("documento", documento);
        model.addAttribute("nombre", nombre);
        model.addAttribute("totalExpedientes", expedientes.size());
        model.addAttribute("contadorActivos", activos);
        model.addAttribute("contadorPendientes", pendientes);
        
        return "expedientes/lista";
    }

    @PostMapping("/guardar")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    public String guardarExpediente(
            @RequestParam("pacienteId") Long pacienteId,
            @RequestParam("tipoSangre") String tipoSangre,
            @RequestParam(value = "alergias", required = false) String alergias,
            @RequestParam(value = "medicamentos", required = false) String medicamentos,
            @RequestParam(value = "enfermedadesCronicas", required = false) String enfermedadesCronicas,
            @RequestParam(value = "observaciones", required = false) String observaciones,
            @RequestParam("estado") String estado,
            RedirectAttributes redirectAttrs) {
        
        System.out.println("=== DEBUG - Creando expediente para paciente ID: " + pacienteId + " ===");
        
        try {
            // 1. Verificar que el paciente existe
            if (!pacienteService.existePorId(pacienteId)) {
                System.err.println("ERROR: Paciente no existe - ID: " + pacienteId);
                redirectAttrs.addFlashAttribute("error", "Paciente no encontrado");
                return "redirect:/expedientes/nuevo?pacienteId=" + pacienteId;
            }
            
            // 2. Verificar si ya tiene expediente
            if (expedienteService.existeExpedienteParaPaciente(pacienteId)) {
                redirectAttrs.addFlashAttribute("error", "El paciente ya tiene expediente");
                return "redirect:/expedientes/nuevo?pacienteId=" + pacienteId;
            }
            
            // 3. Obtener referencia del paciente
            Paciente paciente = pacienteRepository.getReferenceById(pacienteId);
            System.out.println("Paciente referencia obtenida - ID: " + paciente.getId());
            
            // 4. Crear expediente
            ExpedienteMedico expediente = new ExpedienteMedico();
            expediente.setPaciente(paciente);
            expediente.setTipoSangre(tipoSangre);
            expediente.setAlergias(alergias);
            expediente.setMedicamentos(medicamentos);
            expediente.setEnfermedadesCronicas(enfermedadesCronicas);
            expediente.setObservaciones(observaciones);
            expediente.setEstado(estado != null && !estado.isEmpty() ? estado : "ACTIVO");
            expediente.setFechaCreacion(LocalDateTime.now());
            expediente.setFechaActualizacion(LocalDateTime.now());
            
            // 5. Guardar
            System.out.println("Guardando expediente...");
            ExpedienteMedico expedienteGuardado = expedienteRepository.save(expediente);
            
            System.out.println("ÉXITO: Expediente guardado con ID: " + expedienteGuardado.getId());
            
            redirectAttrs.addFlashAttribute("success", "Expediente creado exitosamente");
            return "redirect:/expedientes";
            
        } catch (EntityNotFoundException e) {
            System.err.println("ERROR: Paciente no encontrado - " + e.getMessage());
            redirectAttrs.addFlashAttribute("error", "Paciente no encontrado");
            return "redirect:/expedientes/nuevo?pacienteId=" + pacienteId;
        } catch (Exception e) {
            System.err.println("ERROR GENERAL: " + e.getMessage());
            e.printStackTrace();
            redirectAttrs.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/expedientes/nuevo?pacienteId=" + pacienteId;
        }
    }

    // Formulario para editar expediente
    @GetMapping("/editar/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    public String editarExpediente(@PathVariable Long id, Model model) {
        ExpedienteMedico expediente = expedienteService.obtenerPorId(id)
            .orElseThrow(() -> new RuntimeException("Expediente no encontrado"));
        
        model.addAttribute("expediente", expediente);
        model.addAttribute("tiposSangre", List.of("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"));
        
        return "expedientes/editar";
    }

    // Actualizar expediente
    @PostMapping("/actualizar/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    public String actualizarExpediente(
            @PathVariable Long id,
            @ModelAttribute("expediente") @Valid ExpedienteMedico expediente,
            BindingResult result,
            RedirectAttributes redirectAttrs) {
        
        if (result.hasErrors()) {
            redirectAttrs.addFlashAttribute("org.springframework.validation.BindingResult.expediente", result);
            redirectAttrs.addFlashAttribute("expediente", expediente);
            return "redirect:/expedientes/editar/" + id;
        }
        
        try {
            expedienteService.actualizar(id, expediente);
            redirectAttrs.addFlashAttribute("success", "Expediente actualizado");
            
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
            return "redirect:/expedientes/editar/" + id;
        }
        
        return "redirect:/expedientes/" + id;
    }

    // Eliminar expediente
    @GetMapping("/eliminar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String eliminarExpediente(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            expedienteService.eliminar(id);
            redirectAttrs.addFlashAttribute("success", "Expediente eliminado");
            
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/expedientes";
    }

    // Solicitar creación de expediente
    @GetMapping("/solicitar")
    @PreAuthorize("hasRole('PACIENTE')")
    public String solicitarExpediente(Model model, Principal principal) {
        String email = principal.getName();
        Paciente paciente = pacienteService.buscarPorUsuarioEmail(email)
            .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
        
        if (expedienteService.existeExpedienteParaPaciente(paciente.getId())) {
            return "redirect:/expedientes/mi-expediente";
        }
        
        ExpedienteMedico expediente = new ExpedienteMedico();
        expediente.setPaciente(paciente);
        
        model.addAttribute("expediente", expediente);
        model.addAttribute("paciente", paciente);
        model.addAttribute("tiposSangre", List.of("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"));
        
        return "expedientes/solicitud";
    }

    // Procesar solicitud
    @PostMapping("/solicitar")
    @PreAuthorize("hasRole('PACIENTE')")
    public String procesarSolicitud(
            @ModelAttribute("expediente") @Valid ExpedienteMedico expediente,
            BindingResult result,
            Principal principal,
            RedirectAttributes redirectAttrs) {
        
        if (result.hasErrors()) {
            redirectAttrs.addFlashAttribute("org.springframework.validation.BindingResult.expediente", result);
            redirectAttrs.addFlashAttribute("expediente", expediente);
            return "redirect:/expedientes/solicitar";
        }
        
        try {
            String email = principal.getName();
            Paciente paciente = pacienteService.buscarPorUsuarioEmail(email)
                    .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
            
            if (expedienteService.existeExpedienteParaPaciente(paciente.getId())) {
                return "redirect:/expedientes/mi-expediente";
            }
            
            expediente.setPaciente(paciente);
            expediente.setEstado("PENDIENTE");
            expediente.setFechaCreacion(LocalDateTime.now());
            
            expedienteService.guardar(expediente);
            redirectAttrs.addFlashAttribute("success", "Solicitud enviada");
            
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
            return "redirect:/expedientes/solicitar";
        }
        
        return "redirect:/expedientes/mi-expediente";
    }

    // Aprobar solicitud
    @GetMapping("/aprobar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String aprobarExpediente(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            expedienteService.aprobarExpediente(id);
            redirectAttrs.addFlashAttribute("success", "Expediente aprobado");
            
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/expedientes";
    }
}