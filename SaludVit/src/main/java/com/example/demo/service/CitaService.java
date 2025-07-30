package com.example.demo.service;

import com.example.demo.Entitys.*;
import com.example.demo.Repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CitaService {

    private final CitaRepository citaRepository;

    public CitaService(CitaRepository citaRepository) {
        this.citaRepository = citaRepository;
    }

    public List<Cita> listarTodas() {
        return citaRepository.findAll();
    }

    public void eliminar(Long id) {
        citaRepository.deleteById(id);
    }

    public String programarCita(Cita cita) {
        LocalDateTime hora = cita.getFechaHora();

        if (hora == null) {
            return "Debe ingresar una fecha y hora para la cita.";
        }

        // 1. Validar horario de atención: Lunes a Viernes, 8 AM a 5 PM
        if (hora.getHour() < 8 || hora.getHour() >= 17 || hora.getDayOfWeek().getValue() > 5) {
            return "La cita debe estar dentro del horario laboral (Lunes a Viernes, 8:00 a 17:00)";
        }
        
        // (resto del método sin cambios...)

        // 2. Validar al menos 2 horas de anticipación
        if (hora.isBefore(LocalDateTime.now().plusHours(2))) {
            return "Las citas deben programarse con al menos 2 horas de anticipación.";
        }

        // 3. Validar que el paciente no tenga otra cita el mismo día
        List<Cita> citasPaciente = citaRepository.findByPaciente(cita.getPaciente());
        for (Cita c : citasPaciente) {
            if (c.getFechaHora().toLocalDate().equals(hora.toLocalDate())) {
                return "El paciente ya tiene una cita programada ese día.";
            }
        }

        // 4. Validar que el médico no tenga otra cita a la misma hora
        List<Cita> citasMedico = citaRepository.findByMedicoAndFechaHora(cita.getMedico(), hora);
        if (!citasMedico.isEmpty()) {
            return "El médico ya tiene una cita en ese horario.";
        }

        // Todo correcto, guardar
        citaRepository.save(cita);
        return null; // null significa que no hay error
    }
    public void cancelarCita(Long id) {
        citaRepository.deleteById(id);
    }


}
