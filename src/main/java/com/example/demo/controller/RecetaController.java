package com.example.demo.controller;

import com.example.demo.Entitys.*;
import com.example.demo.dto.ItemRecetaDTO;
import com.example.demo.dto.RecetaDTO;
import com.example.demo.enums.EstadoReceta;
import com.example.demo.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/recetas")
public class RecetaController {
    
    @Autowired
    private RecetaService recetaService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private MedicoService medicoService;
    
    @Autowired
    private PacienteService pacienteService;
    
    @Autowired
    private CitaService citaService;
    
    // ========== VISTAS PARA MÉDICO ==========
    
    // Listar recetas del médico
    @GetMapping("/medico/recetas")
    public String listarRecetasMedico(Model model,
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
            List<Receta> recetas;
            
            if (estado != null && !estado.isEmpty()) {
                try {
                    EstadoReceta estadoEnum = EstadoReceta.valueOf(estado.toUpperCase());
                    recetas = recetaService.buscarPorMedico(medico.getId()).stream()
                        .filter(r -> r.getEstado() == estadoEnum)
                        .toList();
                } catch (IllegalArgumentException e) {
                    recetas = recetaService.buscarPorMedico(medico.getId());
                }
            } else {
                recetas = recetaService.buscarPorMedico(medico.getId());
            }
            
            // Estadísticas
            long total = recetas.size();
            long activas = recetas.stream()
                .filter(r -> r.getEstado() == EstadoReceta.ACTIVA || 
                            r.getEstado() == EstadoReceta.DISPENSADA)
                .count();
            long vencidas = recetas.stream()
                .filter(r -> r.getEstado() == EstadoReceta.VENCIDA)
                .count();
            
            model.addAttribute("medico", medico);
            model.addAttribute("recetas", recetas);
            model.addAttribute("estado", estado);
            model.addAttribute("estadosReceta", EstadoReceta.values());
            model.addAttribute("total", total);
            model.addAttribute("activas", activas);
            model.addAttribute("vencidas", vencidas);
            
            return "medico/recetas/lista";
            
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar recetas: " + e.getMessage());
            return "error";
        }
    }
    
    // Formulario para nueva receta (sin cita específica)
    @GetMapping("/nueva")
    public String nuevaReceta(Model model, Principal principal) {
        try {
            String email = principal.getName();
            Optional<User> usuario = userService.findByEmail(email);
            
            if (usuario.isEmpty()) {
                return "redirect:/login";
            }
            
            Optional<Medico> medicoOpt = medicoService.buscarPorUsuario(usuario.get());
            if (medicoOpt.isEmpty()) {
                model.addAttribute("error", "Debe ser médico para crear recetas");
                return "error";
            }
            
            Medico medico = medicoOpt.get();
            List<Paciente> pacientes = pacienteService.listarTodos();
            
            RecetaDTO recetaDTO = new RecetaDTO();
            recetaDTO.setMedicoId(medico.getId());
            recetaDTO.setFechaEmision(LocalDate.now());
            recetaDTO.setFechaCaducidad(LocalDate.now().plusDays(30));
            
            model.addAttribute("medico", medico);
            model.addAttribute("pacientes", pacientes);
            model.addAttribute("recetaDTO", recetaDTO);
            model.addAttribute("medicamentosComunes", getMedicamentosComunes());
            
            return "medico/recetas/nueva";
            
        } catch (Exception e) {
            model.addAttribute("error", "Error: " + e.getMessage());
            return "error";
        }
    }
    
    @PostMapping("/guardar")
    public String guardarReceta(@ModelAttribute RecetaDTO recetaDTO,
                               Principal principal,
                               RedirectAttributes redirectAttrs) {
        try {
            // Debug: Verificar datos recibidos
            System.out.println("=== DATOS RECETA RECIBIDOS ===");
            System.out.println("Medico ID: " + recetaDTO.getMedicoId());
            System.out.println("Paciente ID: " + recetaDTO.getPacienteId());
            System.out.println("Fecha Emision: " + recetaDTO.getFechaEmision());
            System.out.println("Fecha Caducidad: " + recetaDTO.getFechaCaducidad());
            System.out.println("Electronica: " + recetaDTO.getElectronica());
            System.out.println("Instrucciones Generales: " + recetaDTO.getInstruccionesGenerales());
            
            if (recetaDTO.getItems() != null) {
                System.out.println("Items: " + recetaDTO.getItems().size());
                for (int i = 0; i < recetaDTO.getItems().size(); i++) {
                    ItemRecetaDTO item = recetaDTO.getItems().get(i);
                    System.out.println("Item " + i + " - Medicamento: " + item.getMedicamento() + 
                                     ", Cantidad: " + item.getCantidadTotal() + 
                                     ", Dosis: " + item.getDosis() + 
                                     ", Frecuencia: " + item.getFrecuencia() + 
                                     ", Indicaciones: " + item.getIndicaciones());
                }
            }
            
            
            
            // Verificar que el médico es quien dice ser
            String email = principal.getName();
            Optional<User> usuario = userService.findByEmail(email);
            
            if (usuario.isEmpty()) {
                return "redirect:/login";
            }
            
            Optional<Medico> medicoOpt = medicoService.buscarPorUsuario(usuario.get());
            if (medicoOpt.isEmpty() || !medicoOpt.get().getId().equals(recetaDTO.getMedicoId())) {
                redirectAttrs.addFlashAttribute("error", "No tiene permisos para crear recetas");
                // CORREGIDO: Redirige al endpoint correcto
                return "redirect:/recetas/medico/recetas";
            }
            
            // Crear la receta
            Receta receta = recetaService.crearRecetaConItems(recetaDTO);
            
            redirectAttrs.addFlashAttribute("success", 
                "Receta creada exitosamente. Número: " + receta.getNumeroReceta());
            
            // CORREGIDO: Redirige al endpoint correcto
            return "redirect:/recetas/medico/recetas";
            
        } catch (Exception e) {
            e.printStackTrace(); // Para ver el error completo
            redirectAttrs.addFlashAttribute("error", "Error al crear receta: " + e.getMessage());
            // CORREGIDO: Redirige al endpoint correcto
            return "redirect:/recetas/nueva";
        }
    }
    
    // Ver detalle de receta
    @GetMapping("/{id}")
    public String verReceta(@PathVariable Long id,
                           Model model,
                           Principal principal) {
        try {
            Optional<Receta> recetaOpt = recetaService.buscarPorId(id);
            if (recetaOpt.isEmpty()) {
                model.addAttribute("error", "Receta no encontrada");
                return "error";
            }
            
            Receta receta = recetaOpt.get();
            
            // Verificar permisos
            String email = principal.getName();
            Optional<User> usuario = userService.findByEmail(email);
            
            if (usuario.isPresent()) {
                Optional<Medico> medicoOpt = medicoService.buscarPorUsuario(usuario.get());
                boolean esMedicoReceta = medicoOpt.isPresent() && 
                                       medicoOpt.get().getId().equals(receta.getMedico().getId());
                
                Optional<Paciente> pacienteOpt = pacienteService.buscarPorUsuario(usuario.get());
                boolean esPacienteReceta = pacienteOpt.isPresent() && 
                                         pacienteOpt.get().getId().equals(receta.getPaciente().getId());
                
                if (!esMedicoReceta && !esPacienteReceta) {
                    model.addAttribute("error", "No tiene permisos para ver esta receta");
                    return "error";
                }
                
                model.addAttribute("esMedico", esMedicoReceta);
            }
            
            model.addAttribute("receta", receta);
            
            // Determinar qué vista usar
            if (model.containsAttribute("esMedico") && (Boolean) model.getAttribute("esMedico")) {
                return "medico/recetas/detalle";
            } else {
                return "paciente/recetas/detalle";
            }
            
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar receta: " + e.getMessage());
            return "error";
        }
    }
    
 // ========== VISTAS PARA PACIENTE ==========

 // Listar recetas del paciente
 @GetMapping("/paciente/mis-recetas")
 public String listarMisRecetas(Model model,
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
         List<Receta> recetas;
         
         if (estado != null && !estado.isEmpty()) {
             try {
                 EstadoReceta estadoEnum = EstadoReceta.valueOf(estado.toUpperCase());
                 recetas = recetaService.buscarPorPacienteYEstado(paciente.getId(), estadoEnum);
             } catch (IllegalArgumentException e) {
                 recetas = recetaService.buscarPorPaciente(paciente.getId());
             }
         } else {
             recetas = recetaService.buscarPorPaciente(paciente.getId());
         }
         
         // Estadísticas
         long total = recetas.size();
         long activas = recetas.stream()
             .filter(r -> r.getEstado() == EstadoReceta.ACTIVA || 
                         r.getEstado() == EstadoReceta.DISPENSADA)
             .count();
         long vencidas = recetas.stream()
             .filter(r -> r.getEstado() == EstadoReceta.VENCIDA)
             .count();
         
         model.addAttribute("paciente", paciente);
         model.addAttribute("recetas", recetas);
         model.addAttribute("estado", estado);
         model.addAttribute("estadosReceta", EstadoReceta.values());
         model.addAttribute("total", total);
         model.addAttribute("activas", activas);
         model.addAttribute("vencidas", vencidas);
         
         return "pacientes/recetas/lista";
         
     } catch (Exception e) {
         model.addAttribute("error", "Error al cargar recetas: " + e.getMessage());
         return "error";
     }
 }
    
//Marcar receta como dispensada
 @GetMapping("/{id}/dispensar")
 public String dispensarReceta(@PathVariable Long id, 
                              RedirectAttributes redirectAttrs,
                              Principal principal) {
     try {
         Receta receta = recetaService.marcarComoDispensada(id);
         redirectAttrs.addFlashAttribute("success", 
             "Receta marcada como dispensada. Número: " + receta.getNumeroReceta());
         return "redirect:/recetas/medico/recetas"; // CORREGIDO
     } catch (Exception e) {
         redirectAttrs.addFlashAttribute("error", "Error: " + e.getMessage());
         return "redirect:/recetas/medico/recetas"; // CORREGIDO
     }
 }
    
    private List<String> getMedicamentosComunes() {
        return List.of(
            "Paracetamol", "Ibuprofeno", "Amoxicilina", "Omeprazol",
            "Losartán", "Atorvastatina", "Metformina", "Salbutamol",
            "Aspirina", "Diclofenaco", "Loratadina", "Prednisona"
        );
    }
}