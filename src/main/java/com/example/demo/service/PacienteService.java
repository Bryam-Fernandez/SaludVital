package com.example.demo.service;

import com.example.demo.Entitys.*;
import com.example.demo.Repository.*;
import com.example.demo.dto.PacienteDTO;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


	public interface PacienteService {
	    List<Paciente> listarTodos();
	    Optional<Paciente> buscarPorId(Long id);
	    Paciente guardar(Paciente paciente);
	    Paciente actualizar(Long id, Paciente paciente);
	    void eliminar(Long id);
	    List<PacienteDTO> listarTodosDTO();
	    Paciente buscarPorDocumento(String numeroIdentificacion);
	    void actualizarPaciente(Paciente paciente);
	    Optional<Paciente> buscarPorUsuario(User user);
	    Optional<Paciente> buscarPorUsuarioEmail(String email);
	    List<Paciente> listarPacientesSinExpediente();
	    List<Paciente> buscarPorDocumentoONombre(String query);
	    boolean existePorId(Long id);
	    List<Paciente> findAll(); // Si no existe, agrégalo
	    
	    // O si no tienes findAll, crea:
	    List<Paciente> buscarTodos();
	    
	    // Y el método específico:
	    List<Paciente> buscarPorMedico(Long medicoId);
	}