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
                        empleado.getPersona().getNombre(),
                        empleado.getPersona().getApellidos(),
                        empleado.getPersona().getUsuario().getUsuarioRoles().stream()
                                .findFirst().map(ur -> ur.getRol().getNombre()).orElse(""),
                        h.getDia(),
                        h.getHoraInicio(),
                        h.getHoraFin(),
                        h.getTurno()
                ))
                .collect(Collectors.toList());
    }

}