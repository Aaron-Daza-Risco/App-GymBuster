package com.version.gymModuloControl.service;

import com.version.gymModuloControl.dto.HorarioInstructorDTO;
import com.version.gymModuloControl.dto.InstructorDisponibleDTO;
import com.version.gymModuloControl.model.Empleado;
import com.version.gymModuloControl.model.Plan;
import com.version.gymModuloControl.model.TipoInstructor;
import com.version.gymModuloControl.repository.EmpleadoRepository;
import com.version.gymModuloControl.repository.HorarioEmpleadoRepository;
import com.version.gymModuloControl.repository.PlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InstructorService {

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private HorarioEmpleadoRepository horarioEmpleadoRepository;

    public List<InstructorDisponibleDTO> obtenerInstructoresDisponiblesPorPlan(Integer idPlan) {
        Plan plan = planRepository.findById(idPlan)
                .orElseThrow(() -> new RuntimeException("Plan no encontrado"));

        TipoInstructor tipoInstructorRequerido = TipoInstructor.valueOf(plan.getTipoPlan().name());

        List<Empleado> empleados;

        if (tipoInstructorRequerido == TipoInstructor.PREMIUM) {
            empleados = empleadoRepository.findByTipoInstructorAndEstadoTrueAndCupoMaximoGreaterThan(tipoInstructorRequerido, 0);
        } else if (tipoInstructorRequerido == TipoInstructor.ESTANDAR) {
            empleados = empleadoRepository.findByTipoInstructorAndEstadoTrue(tipoInstructorRequerido);
        } else {
            throw new RuntimeException("Tipo de instructor desconocido");
        }

        // Mapear a DTO
        return empleados.stream()
                .map(e -> new InstructorDisponibleDTO(
                        e.getIdEmpleado(),
                        e.getPersona().getNombre() + " " + e.getPersona().getApellidos(),
                        e.getTipoInstructor().name(),
                        e.getCupoMaximo()
                ))
                .collect(Collectors.toList());
    }


    public List<HorarioInstructorDTO> obtenerHorariosPorInstructor(Integer idEmpleado) {
        return horarioEmpleadoRepository.listarHorariosPorInstructor(idEmpleado);
    }

}
