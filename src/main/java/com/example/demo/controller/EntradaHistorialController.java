package com.example.demo.controller;

import com.example.demo.Entitys.EntradaHistorial;
import com.example.demo.Entitys.ExpedienteMedico;
import com.example.demo.service.EntradaHistorialService;
import com.example.demo.service.ExpedienteMedicoService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/historial")
public class EntradaHistorialController {

    @Autowired
    private ExpedienteMedicoService expedienteService;

    @Autowired
    private EntradaHistorialService entradaService;

    // Listar todas las entradas de historial (solo admin/médicos)
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    public String listarEntradas(
            @RequestParam(required = false) Long expedienteId,
            @RequestParam(required = false) String tipo,
            Model model) {
        
        List<EntradaHistorial> entradas;
        if (expedienteId != null) {
            entradas = entradaService.obtenerPorExpedienteId(expedienteId);
        } else if (tipo != null && !tipo.isEmpty()) {
            entradas = entradaService.obtenerPorTipo(tipo);
        } else {
            entradas = entradaService.listarTodas();
        }
        
        model.addAttribute("entradas", entradas);
        model.addAttribute("expedientes", expedienteService.listarTodos());
        model.addAttribute("expedienteId", expedienteId);
        model.addAttribute("tipo", tipo);
        
        return "historial/lista";
    }

    // Formulario para nueva entrada
    @GetMapping("/nueva")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    public String mostrarFormularioHistorial(
            @RequestParam Long expedienteId,
            Model model,
            Principal principal) {
        
        ExpedienteMedico expediente = expedienteService.obtenerPorId(expedienteId)
                .orElseThrow(() -> new RuntimeException("Expediente no encontrado"));
        
        EntradaHistorial entrada = new EntradaHistorial();
        entrada.setExpediente(expediente);
        entrada.setFechaCreacion(LocalDateTime.now());
        
        model.addAttribute("entrada", entrada);
        model.addAttribute("tiposEntrada", List.of("CONSULTA", "DIAGNÓSTICO", "TRATAMIENTO", "EXAMEN", "PROCEDIMIENTO", "OBSERVACIÓN"));
        
        return "historial/formulario";
    }

    // Guardar nueva entrada
    @PostMapping("/guardar")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    public String guardarEntrada(
            @ModelAttribute("entrada") @Valid EntradaHistorial entrada,
            BindingResult result,
            Principal principal,
            RedirectAttributes redirectAttrs) {
        
        if (result.hasErrors()) {
            redirectAttrs.addFlashAttribute("org.springframework.validation.BindingResult.entrada", result);
            redirectAttrs.addFlashAttribute("entrada", entrada);
            return "redirect:/historial/nueva?expedienteId=" + entrada.getExpediente().getId();
        }
        
        try {
            // Establecer fecha de creación y médico responsable
            entrada.setFechaCreacion(LocalDateTime.now());
            
            // Aquí podrías obtener el médico logueado si es necesario
            // Médico medico = obtenerMedicoLogueado(principal);
            // entrada.setMedico(medico);
            
            entradaService.guardar(entrada);
            redirectAttrs.addFlashAttribute("success", "Entrada guardada correctamente");
            
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
            return "redirect:/historial/nueva?expedienteId=" + entrada.getExpediente().getId();
        }
        
        return "redirect:/expedientes/" + entrada.getExpediente().getId();
    }

    // Ver detalle de entrada
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'PACIENTE')")
    public String verEntrada(@PathVariable Long id, Model model) {
        EntradaHistorial entrada = entradaService.obtenerPorId(id)
                .orElseThrow(() -> new RuntimeException("Entrada no encontrada"));
        
        model.addAttribute("entrada", entrada);
        return "historial/detalle";
    }

    // Formulario para editar entrada
    @GetMapping("/editar/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    public String editarEntrada(@PathVariable Long id, Model model) {
        EntradaHistorial entrada = entradaService.obtenerPorId(id)
                .orElseThrow(() -> new RuntimeException("Entrada no encontrada"));
        
        model.addAttribute("entrada", entrada);
        model.addAttribute("tiposEntrada", List.of("CONSULTA", "DIAGNÓSTICO", "TRATAMIENTO", "EXAMEN", "PROCEDIMIENTO", "OBSERVACIÓN"));
        
        return "historial/editar";
    }

    // Actualizar entrada
    @PostMapping("/actualizar/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    public String actualizarEntrada(
            @PathVariable Long id,
            @ModelAttribute("entrada") @Valid EntradaHistorial entrada,
            BindingResult result,
            RedirectAttributes redirectAttrs) {
        
        if (result.hasErrors()) {
            redirectAttrs.addFlashAttribute("org.springframework.validation.BindingResult.entrada", result);
            redirectAttrs.addFlashAttribute("entrada", entrada);
            return "redirect:/historial/editar/" + id;
        }
        
        try {
            entradaService.actualizar(id, entrada);
            redirectAttrs.addFlashAttribute("success", "Entrada actualizada correctamente");
            
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
            return "redirect:/historial/editar/" + id;
        }
        
        return "redirect:/expedientes/" + entrada.getExpediente().getId();
    }

    // Eliminar entrada
    @GetMapping("/eliminar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String eliminarEntrada(
            @PathVariable Long id,
            RedirectAttributes redirectAttrs) {
        
        try {
            EntradaHistorial entrada = entradaService.obtenerPorId(id)
                    .orElseThrow(() -> new RuntimeException("Entrada no encontrada"));
            
            Long expedienteId = entrada.getExpediente().getId();
            entradaService.eliminar(id);
            redirectAttrs.addFlashAttribute("success", "Entrada eliminada correctamente");
            
            return "redirect:/expedientes/" + expedienteId;
            
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
            return "redirect:/historial";
        }
    }

    // API para obtener entradas de un expediente específico
    @GetMapping("/api/expediente/{expedienteId}")
    @ResponseBody
    public List<EntradaHistorial> obtenerEntradasPorExpediente(@PathVariable Long expedienteId) {
        return entradaService.obtenerPorExpedienteId(expedienteId);
    }

    // API para obtener entradas por tipo
    @GetMapping("/api/tipo/{tipo}")
    @ResponseBody
    public List<EntradaHistorial> obtenerEntradasPorTipo(@PathVariable String tipo) {
        return entradaService.obtenerPorTipo(tipo);
    }
}