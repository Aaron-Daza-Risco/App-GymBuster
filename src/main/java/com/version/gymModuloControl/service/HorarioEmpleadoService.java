package com.version.gymModuloControl.service;

import com.version.gymModuloControl.dto.HorarioEmpleadoInfoDTO;
import com.version.gymModuloControl.model.Empleado;
import com.version.gymModuloControl.model.HorarioEmpleado;
import com.version.gymModuloControl.repository.EmpleadoRepository;
import com.version.gymModuloControl.repository.HorarioEmpleadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HorarioEmpleadoService {

    @Autowired
    private HorarioEmpleadoRepository horarioEmpleadoRepository;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Transactional
    public HorarioEmpleado agregarHorario(Integer empleadoId, HorarioEmpleado horarioEmpleado) {
        Empleado empleado = empleadoRepository.findById(empleadoId)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));
        horarioEmpleado.setEmpleado(empleado);
        return horarioEmpleadoRepository.save(horarioEmpleado);
    }

    public List<HorarioEmpleadoInfoDTO> listarInfoHorariosEmpleados() {
        return horarioEmpleadoRepository.obtenerInfoHorariosEmpleados();
    }

    public List<HorarioEmpleadoInfoDTO> listarHorariosPorUsuario(String nombreUsuario) {
        Empleado empleado = empleadoRepository.findByPersonaUsuarioNombreUsuario(nombreUsuario);
        if (empleado == null) {
            return Collections.emptyList();
        }
        return empleado.getHorarios().stream()
                .map(h -> new HorarioEmpleadoInfoDTO(
                        h.getIdHorarioEmpleado(),
                        empleado.getPersona().getNombre(),
                        empleado.getPersona().getApellidos(),
                        empleado.getPersona().getUsuario().getUsuarioRoles().stream()
                                .findFirst().map(ur -> ur.getRol().getNombre()).orElse(""),

                        h.getDia(),
                        h.getHoraInicio(),
                        h.getHoraFin(),
                        h.getTurno(),
                        h.getEstado()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public HorarioEmpleado actualizarHorario(Integer horarioId, HorarioEmpleado horarioActualizado) {
        HorarioEmpleado horario = horarioEmpleadoRepository.findById(horarioId)
                .orElseThrow(() -> new RuntimeException("Horario no encontrado"));
        horario.setDia(horarioActualizado.getDia());
        horario.setHoraInicio(horarioActualizado.getHoraInicio());
        horario.setHoraFin(horarioActualizado.getHoraFin());
        horario.setTurno(horarioActualizado.getTurno());
        // Agrega aquí otros campos a actualizar si es necesario
        return horarioEmpleadoRepository.save(horario);
    }

    @Transactional
    public boolean eliminarHorario(Integer horarioId) {
        if (horarioEmpleadoRepository.existsById(horarioId)) {
            horarioEmpleadoRepository.deleteById(horarioId);
            return true;
        }
        return false;
    }

    @Transactional
    public HorarioEmpleado cambiarEstadoHorario(Integer horarioId, Boolean estado) {
        HorarioEmpleado horario = horarioEmpleadoRepository.findById(horarioId).orElse(null);
        if (horario != null) {
            horario.setEstado(estado);
            return horarioEmpleadoRepository.save(horario);
        }
        return null;
    }

    public List<HorarioEmpleado> obtenerHorariosPorEmpleadoYDia(Long idEmpleado, String dia) {
        return horarioEmpleadoRepository.findByEmpleadoAndDia(idEmpleado, dia.toUpperCase());
    }

}