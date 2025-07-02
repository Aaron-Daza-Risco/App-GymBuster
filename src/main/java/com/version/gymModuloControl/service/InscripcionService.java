package com.version.gymModuloControl.service;

import com.version.gymModuloControl.dto.InscripcionRequestDTO;
import com.version.gymModuloControl.dto.InscripcionResponseDTO;
import com.version.gymModuloControl.model.*;
import com.version.gymModuloControl.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class InscripcionService {

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private HorarioEmpleadoRepository horarioEmpleadoRepository;

    @Autowired
    private DetalleInscripcionRepository detalleInscripcionRepository;

    private InscripcionResponseDTO mapToResponseDTO(Inscripcion inscripcion, Empleado instructor, List<HorarioEmpleado> horarios) {
        InscripcionResponseDTO dto = new InscripcionResponseDTO();
        dto.setIdInscripcion(inscripcion.getIdInscripcion());
        dto.setClienteNombre(inscripcion.getCliente().getPersona().getNombre() + " " + inscripcion.getCliente().getPersona().getApellidos());
        dto.setPlanNombre(inscripcion.getPlan().getNombre());

        if (instructor != null) {
            dto.setInstructorNombre(instructor.getPersona().getNombre() + " " + instructor.getPersona().getApellidos());
        } else {
            dto.setInstructorNombre(null);
        }

        if (inscripcion.getRecepcionista() != null) {
            dto.setRecepcionistaNombre(inscripcion.getRecepcionista().getPersona().getNombre() + " " + inscripcion.getRecepcionista().getPersona().getApellidos());
        } else {
            dto.setRecepcionistaNombre(null);
        }

        dto.setFechaInscripcion(inscripcion.getFechaInscripcion());
        dto.setFechaInicio(inscripcion.getFechaInicio());
        dto.setFechaFin(inscripcion.getFechaFin());
        dto.setMonto(inscripcion.getMonto());

        List<String> listaHorarios = horarios.stream()
                .map(h -> h.getDia() + " " + h.getHoraInicio() + " - " + h.getHoraFin())
                .toList();
        dto.setHorarios(listaHorarios);

        dto.setEstado(inscripcion.getEstado().name());

        return dto;
    }

    @Transactional
    public InscripcionResponseDTO registrarInscripcion(InscripcionRequestDTO request) {
        // 1. Obtener recepcionista desde la sesión
        String nombreUsuario = SecurityContextHolder.getContext().getAuthentication().getName();
        Empleado recepcionista = empleadoRepository.findByPersonaUsuarioNombreUsuario(nombreUsuario);
        if (recepcionista == null) {
            throw new RuntimeException("No se encontró un empleado (recepcionista) asociado al usuario actual.");
        }

        // 2. Buscar cliente
        Cliente cliente = clienteRepository.findById(request.getIdCliente())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + request.getIdCliente()));

        // 2.1 Validar inscripción activa
        Optional<Inscripcion> inscripcionActiva = inscripcionRepository
                .findByClienteIdClienteAndFechaFinAfterAndEstadoTrue(request.getIdCliente(), LocalDate.now());

        if (inscripcionActiva.isPresent()) {
            throw new BusinessException("El cliente ya tiene una inscripción activa que finaliza el " + inscripcionActiva.get().getFechaFin());
        }

        // 3. Buscar plan
        Plan plan = planRepository.findById(request.getIdPlan())
                .orElseThrow(() -> new RuntimeException("Plan no encontrado con ID: " + request.getIdPlan()));

        Empleado instructor = null;

        // 4. Validación de instructor solo si el plan NO es ESTANDAR
        if (!plan.getTipoPlan().equals(TipoPlan.ESTANDAR)) {
            if (request.getIdInstructor() == null) {
                throw new RuntimeException("Se requiere un instructor para este tipo de plan.");
            }

            instructor = empleadoRepository.findById(request.getIdInstructor())
                    .orElseThrow(() -> new RuntimeException("Instructor no encontrado con ID: " + request.getIdInstructor()));

            if (!instructor.getEstado()) {
                throw new RuntimeException("Instructor no está activo.");
            }

            if (instructor.getCupoMaximo() == null || instructor.getCupoMaximo() <= 0) {
                throw new RuntimeException("Instructor sin cupo disponible para este plan.");
            }

            validarTipoInstructorVsPlan(instructor, plan);
        }

        // 5. Calcular fecha fin
        LocalDate fechaFin = request.getFechaInicio().plusDays(plan.getDuracion());

        // 6. Crear inscripción
        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setRecepcionista(recepcionista);
        inscripcion.setCliente(cliente);
        inscripcion.setPlan(plan);
        inscripcion.setFechaInscripcion(LocalDate.now());
        inscripcion.setFechaInicio(request.getFechaInicio());
        inscripcion.setFechaFin(fechaFin);
        inscripcion.setMonto(request.getMonto());
        inscripcion.setEstado(EstadoInscripcion.ACTIVO);

        inscripcion = inscripcionRepository.save(inscripcion);

        // 7. Obtener y registrar horarios según tipo de plan
        List<HorarioEmpleado> horariosInstructor = new ArrayList<>();

        if (plan.getTipoPlan().equals(TipoPlan.ESTANDAR)) {
            horariosInstructor = horarioEmpleadoRepository.findByEmpleadoTipoInstructorAndEstadoTrue(TipoInstructor.ESTANDAR);

            if (horariosInstructor == null || horariosInstructor.isEmpty()) {
                throw new RuntimeException("No hay instructores de tipo ESTANDAR con horarios activos.");
            }
        } else {
            // Solo en planes NO ESTANDAR
            horariosInstructor = horarioEmpleadoRepository.findByEmpleadoIdEmpleadoAndEstadoTrue(instructor.getIdEmpleado());

            if (horariosInstructor == null || horariosInstructor.isEmpty()) {
                throw new RuntimeException("El instructor no tiene horarios activos.");
            }
        }


        // 8. Guardar detalles de inscripción
        List<DetalleInscripcion> detalles = new ArrayList<>();
        for (HorarioEmpleado horario : horariosInstructor) {
            DetalleInscripcion detalle = new DetalleInscripcion();
            detalle.setInscripcion(inscripcion);
            detalle.setHorarioEmpleado(horario);
            detalles.add(detalle);
        }
        detalleInscripcionRepository.saveAll(detalles);

        // 9. Descontar cupo solo para planes NO ESTANDAR
        if (!plan.getTipoPlan().equals(TipoPlan.ESTANDAR)) {
            instructor.setCupoMaximo(instructor.getCupoMaximo() - 1);
            empleadoRepository.save(instructor);
        }

        // 10. Devolver respuesta
        return mapToResponseDTO(inscripcion, instructor, horariosInstructor);
    }



    private void validarTipoInstructorVsPlan(Empleado instructor, Plan plan) {
        String tipoInstructor = instructor.getTipoInstructor().name();
        String tipoPlan = plan.getTipoPlan().name();

        if (!tipoInstructor.equals(tipoPlan)) {
            throw new RuntimeException("Instructor de tipo [" + tipoInstructor + "] no corresponde al plan [" + tipoPlan + "]");
        }
    }

    public class BusinessException extends RuntimeException {
        public BusinessException(String message) {
            super(message);
        }
    }



}
