package com.version.gymModuloControl.service;

import com.version.gymModuloControl.dto.DetalleInscripcionDTO;
import com.version.gymModuloControl.dto.InscripcionConDetalleDTO;
import com.version.gymModuloControl.dto.InscripcionRequestDTO;
import com.version.gymModuloControl.dto.InscripcionResponseDTO;
import com.version.gymModuloControl.dto.PlanesInscritosDTO;
import com.version.gymModuloControl.dto.HorarioDTO;
import com.version.gymModuloControl.dto.AsistenciaDTO;
import com.version.gymModuloControl.dto.EntrenadorPlanDTO;
import com.version.gymModuloControl.model.*;
import com.version.gymModuloControl.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.version.gymModuloControl.dto.PlanConClientesDTO;
import com.version.gymModuloControl.dto.ClienteConHorariosDTO;



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

    @Autowired
    private QRService qrService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AsistenciaRepository asistenciaRepository;

    private static final Logger log = LoggerFactory.getLogger(InscripcionService.class);

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

        // 2.1 Validar inscripción activa o cancelada
        Optional<Inscripcion> ultimaInscripcionOpt = inscripcionRepository
                .findTopByClienteIdClienteOrderByFechaInscripcionDesc(request.getIdCliente());

        if (ultimaInscripcionOpt.isPresent()) {
            Inscripcion ultimaInscripcion = ultimaInscripcionOpt.get();
            if (!ultimaInscripcion.getEstado().equals(EstadoInscripcion.CANCELADO)) {
                if (ultimaInscripcion.getFechaFin().isAfter(LocalDate.now()) && ultimaInscripcion.getEstado().equals(EstadoInscripcion.ACTIVO)) {
                    throw new BusinessException("El cliente ya tiene una inscripción activa que finaliza el " + ultimaInscripcion.getFechaFin());
                }
            }
            // Si está cancelada, permite la inscripción
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
        LocalDate fechaFin;
        if (plan.getDuracion() == 1) {
            fechaFin = request.getFechaInicio(); // Mismo día para planes diarios
        } else {
            fechaFin = request.getFechaInicio().plusDays(plan.getDuracion());
        }

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

        try {
            String correoCliente = cliente.getPersona().getCorreo();
            String nombreCompleto = cliente.getPersona().getNombre() + " " + cliente.getPersona().getApellidos();

            // Supongamos que ya tienes la inscripción guardada y su ID disponible

            // ✅ Aquí incluimos el ID de la inscripción (clave para registrar asistencia)
            String qrContenido = "ID_INSCRIPCION:" + inscripcion.getIdInscripcion() + "\n" +
                    "Cliente: " + nombreCompleto + "\n" +
                    "Inicio: " + inscripcion.getFechaInicio() + "\n" +
                    "Fin: " + inscripcion.getFechaFin() + "\n" +
                    "Plan: " + plan.getNombre();

            byte[] qrImage = qrService.generateQRCodeImage(qrContenido, 200, 200);

            String asunto = "QR de Asistencia - GYM APP";
            String cuerpo = "Adjunto encontrarás tu código QR para registrar tu asistencia en el gimnasio.\n\n" +
                    "Fechas válidas:\n" +
                    "📅 Inicio: " + inscripcion.getFechaInicio() + "\n" +
                    "📅 Fin: " + inscripcion.getFechaFin() + "\n\n" +
                    "Solo presenta este QR al ingresar al gimnasio.";

            emailService.sendEmailWithQR(correoCliente, asunto, cuerpo, qrImage);


            emailService.sendEmailWithQR(correoCliente, asunto, cuerpo, qrImage);

        } catch (Exception e) {
            throw new RuntimeException("No se pudo enviar el correo con QR: " + e.getMessage());
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

    // En InscripcionService.java
    public InscripcionConDetalleDTO obtenerInscripcionConDetalle(Integer idInscripcion) {
        Inscripcion inscripcion = inscripcionRepository.findById(idInscripcion)
                .orElseThrow(() -> new RuntimeException("Inscripción no encontrada"));

        // Datos del cliente
        String clienteNombre = inscripcion.getCliente().getPersona().getNombre();
        String clienteApellido = inscripcion.getCliente().getPersona().getApellidos();
        String clienteDni = inscripcion.getCliente().getPersona().getDni();

        // Datos del recepcionista
        String recepcionistaNombre = inscripcion.getRecepcionista() != null ? inscripcion.getRecepcionista().getPersona().getNombre() : null;
        String recepcionistaApellido = inscripcion.getRecepcionista() != null ? inscripcion.getRecepcionista().getPersona().getApellidos() : null;
        String recepcionistaDni = inscripcion.getRecepcionista() != null ? inscripcion.getRecepcionista().getPersona().getDni() : null;

        // Datos del plan
        String nombrePlan = inscripcion.getPlan().getNombre();
        Integer duracionPlan = inscripcion.getPlan().getDuracion();
        BigDecimal precioPlan = inscripcion.getPlan().getPrecio();

        // Detalles de inscripción (incluye nombre del instructor)
        List<DetalleInscripcionDTO> detalles = inscripcion.getDetallesInscripcion().stream().map(detalle -> {
            String instructorNombre = detalle.getHorarioEmpleado().getEmpleado().getPersona().getNombre();
            String instructorApellido = detalle.getHorarioEmpleado().getEmpleado().getPersona().getApellidos();
            String dia = detalle.getHorarioEmpleado().getDia();
            String horaInicio = detalle.getHorarioEmpleado().getHoraInicio().toString();
            String horaFin = detalle.getHorarioEmpleado().getHoraFin().toString();
            return new DetalleInscripcionDTO(instructorNombre, instructorApellido, dia, horaInicio, horaFin);
        }).toList();

        // Datos de pago
        Integer idPago = inscripcion.getPago() != null ? inscripcion.getPago().getIdPago() : null;
        BigDecimal montoPagado = inscripcion.getPago() != null ? inscripcion.getPago().getMontoPagado() : null;
        BigDecimal vuelto = inscripcion.getPago() != null ? inscripcion.getPago().getVuelto() : null;
        String metodoPago = inscripcion.getPago() != null ? inscripcion.getPago().getMetodoPago() : null;

        return new InscripcionConDetalleDTO(
                inscripcion.getIdInscripcion(),
                clienteNombre,
                clienteApellido,
                clienteDni,
                recepcionistaNombre,
                recepcionistaApellido,
                recepcionistaDni,
                inscripcion.getFechaInscripcion(),
                inscripcion.getFechaInicio(),
                inscripcion.getFechaFin(),
                inscripcion.getMonto(),
                nombrePlan,
                duracionPlan,
                precioPlan,
                inscripcion.getEstado().name(),
                detalles,
                idPago,
                montoPagado,
                vuelto,
                metodoPago
        );
    }

    public List<InscripcionConDetalleDTO> listarTodasLasInscripciones() {
        List<Inscripcion> inscripciones = inscripcionRepository.findAll();

        return inscripciones.stream().map(inscripcion -> {
            // Datos del cliente
            String clienteNombre = inscripcion.getCliente().getPersona().getNombre();
            String clienteApellido = inscripcion.getCliente().getPersona().getApellidos();
            String clienteDni = inscripcion.getCliente().getPersona().getDni();

            // Datos del recepcionista (puede ser null)
            String recepcionistaNombre = null;
            String recepcionistaApellido = null;
            String recepcionistaDni = null;
            if (inscripcion.getRecepcionista() != null) {
                recepcionistaNombre = inscripcion.getRecepcionista().getPersona().getNombre();
                recepcionistaApellido = inscripcion.getRecepcionista().getPersona().getApellidos();
                recepcionistaDni = inscripcion.getRecepcionista().getPersona().getDni();
            }

            // Datos del plan
            String nombrePlan = inscripcion.getPlan().getNombre();
            Integer duracionPlan = inscripcion.getPlan().getDuracion();
            var precioPlan = inscripcion.getPlan().getPrecio();

            // Datos de pago (puede ser null)
            Integer idPago = null;
            var montoPagado = (inscripcion.getPago() != null) ? inscripcion.getPago().getMontoPagado() : null;
            var vuelto = (inscripcion.getPago() != null) ? inscripcion.getPago().getVuelto() : null;
            var metodoPago = (inscripcion.getPago() != null) ? inscripcion.getPago().getMetodoPago() : null;
            if (inscripcion.getPago() != null) {
                idPago = inscripcion.getPago().getIdPago();
            }

            // Detalles de inscripción (instructor, día, horario)
            List<DetalleInscripcionDTO> detalles = inscripcion.getDetallesInscripcion().stream().map(detalle -> {
                String instructorNombre = detalle.getHorarioEmpleado().getEmpleado().getPersona().getNombre();
                String instructorApellido = detalle.getHorarioEmpleado().getEmpleado().getPersona().getApellidos();
                String dia = detalle.getHorarioEmpleado().getDia();
                String horaInicio = detalle.getHorarioEmpleado().getHoraInicio().toString();
                String horaFin = detalle.getHorarioEmpleado().getHoraFin().toString();
                return new DetalleInscripcionDTO(instructorNombre, instructorApellido, dia, horaInicio, horaFin);
            }).collect(Collectors.toList());

            return new InscripcionConDetalleDTO(
                    inscripcion.getIdInscripcion(),
                    clienteNombre,
                    clienteApellido,
                    clienteDni,
                    recepcionistaNombre,
                    recepcionistaApellido,
                    recepcionistaDni,
                    inscripcion.getFechaInscripcion(),
                    inscripcion.getFechaInicio(),
                    inscripcion.getFechaFin(),
                    inscripcion.getMonto(),
                    nombrePlan,
                    duracionPlan,
                    precioPlan,
                    inscripcion.getEstado().name(),
                    detalles,
                    idPago,
                    montoPagado,
                    vuelto,
                    metodoPago
            );
        }).collect(Collectors.toList());
    }

    public void cancelarInscripcion(Integer idInscripcion) {
        Inscripcion inscripcion = inscripcionRepository.findById(idInscripcion)
                .orElseThrow(() -> new RuntimeException("Inscripción no encontrada"));

        if (inscripcion.getEstado() == EstadoInscripcion.CANCELADO) {
            throw new RuntimeException("La inscripción ya está cancelada.");
        }

        inscripcion.setEstado(EstadoInscripcion.CANCELADO);

        // Si el plan es PREMIUM, sumar cupo al instructor
        if (inscripcion.getPlan().getTipoPlan().name().equals("PREMIUM")) {
            // Obtener el instructor desde el primer detalle de inscripción
            if (!inscripcion.getDetallesInscripcion().isEmpty()) {
                Empleado instructor = inscripcion.getDetallesInscripcion().get(0)
                        .getHorarioEmpleado().getEmpleado();
                if (instructor != null && instructor.getCupoMaximo() != null) {
                    instructor.setCupoMaximo(instructor.getCupoMaximo() + 1);
                    empleadoRepository.save(instructor);
                }
            }
        }

        inscripcionRepository.save(inscripcion);
    }

    @Scheduled(cron = "0 */8 * * * *") // Todos los días a la 1:00 AM
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void finalizarInscripcionesVencidas() {
        log.info("=== Iniciando finalización automática de inscripciones vencidas ===");
        LocalDate hoy = LocalDate.now();
        List<Inscripcion> vencidas = inscripcionRepository.findAll().stream()
                .filter(i -> i.getEstado() == EstadoInscripcion.ACTIVO && !i.getFechaFin().isAfter(hoy))
                .toList();

        for (Inscripcion inscripcion : vencidas) {
            try {
                inscripcion.setEstado(EstadoInscripcion.FINALIZADO);

                // Si el plan es PREMIUM, sumar cupo al instructor
                if (inscripcion.getPlan().getTipoPlan().name().equals("PREMIUM")) {
                    if (!inscripcion.getDetallesInscripcion().isEmpty()) {
                        Empleado instructor = inscripcion.getDetallesInscripcion().get(0)
                                .getHorarioEmpleado().getEmpleado();
                        if (instructor != null && instructor.getCupoMaximo() != null) {
                            instructor.setCupoMaximo(instructor.getCupoMaximo() + 1);
                            empleadoRepository.save(instructor);
                        }
                    }
                }

                inscripcionRepository.save(inscripcion);
                log.info("Inscripción {} finalizada automáticamente.", inscripcion.getIdInscripcion());
            } catch (Exception e) {
                log.error("Error finalizando inscripción {}: {}", inscripcion.getIdInscripcion(), e.getMessage());
            }
        }
        log.info("=== Finalización automática completada ===");
    }

    public List<PlanesInscritosDTO> obtenerPlanesInscritosPorCliente(Integer idCliente) {
        // ⭐ MODIFICACIÓN: Solo obtener inscripciones ACTIVAS
        List<String> estados = List.of(
            EstadoInscripcion.ACTIVO.name()
            // Se removió EstadoInscripcion.FINALIZADO.name() y EstadoInscripcion.CANCELADO.name()
        );
        List<Inscripcion> inscripciones = inscripcionRepository.findByClienteIdClienteAndEstadoIn(
                idCliente, estados
        );
        List<PlanesInscritosDTO> resultado = new ArrayList<>();

        for (Inscripcion inscripcion : inscripciones) {
            PlanesInscritosDTO dto = new PlanesInscritosDTO();
            dto.setNombrePlan(inscripcion.getPlan().getNombre());
            dto.setDescripcionPlan(inscripcion.getPlan().getDescripcion());
            dto.setFechaInicio(inscripcion.getFechaInicio());
            dto.setFechaFin(inscripcion.getFechaFin());
            dto.setTipoPlan(inscripcion.getPlan().getTipoPlan().name());
            dto.setEstadoInscripcion(inscripcion.getEstado().name()); // NUEVO: Incluir estado

            // ⭐ NUEVA LÓGICA: Distinguir entre planes Premium y Estándar
            if (inscripcion.getPlan().getTipoPlan().equals(TipoPlan.PREMIUM)) {
                // PLAN PREMIUM: Un solo entrenador personalizado
                Empleado entrenador = null;
                if (!inscripcion.getDetallesInscripcion().isEmpty()) {
                    entrenador = inscripcion.getDetallesInscripcion().get(0).getHorarioEmpleado().getEmpleado();
                }
                if (entrenador != null) {
                    dto.setEntrenadorNombre(entrenador.getPersona().getNombre());
                    dto.setEntrenadorApellido(entrenador.getPersona().getApellidos());
                }

                // Horarios del entrenador asignado
                List<HorarioDTO> horarios = new ArrayList<>();
                for (DetalleInscripcion det : inscripcion.getDetallesInscripcion()) {
                    HorarioEmpleado h = det.getHorarioEmpleado();
                    HorarioDTO hDto = new HorarioDTO();
                    hDto.setDiaSemana(h.getDia());
                    hDto.setHoraInicio(h.getHoraInicio());
                    hDto.setHoraFin(h.getHoraFin());
                    horarios.add(hDto);
                }
                dto.setHorarios(horarios);

            } else if (inscripcion.getPlan().getTipoPlan().equals(TipoPlan.ESTANDAR)) {
                // PLAN ESTÁNDAR: Múltiples entrenadores disponibles
                List<EntrenadorPlanDTO> entrenadores = new ArrayList<>();

                // Agrupar horarios por entrenador
                Map<Integer, List<HorarioDTO>> horariosPorEntrenador = new HashMap<>();
                Map<Integer, Empleado> entrenadoresMap = new HashMap<>();

                for (DetalleInscripcion det : inscripcion.getDetallesInscripcion()) {
                    HorarioEmpleado h = det.getHorarioEmpleado();
                    Empleado emp = h.getEmpleado();

                    entrenadoresMap.put(emp.getIdEmpleado(), emp);

                    HorarioDTO hDto = new HorarioDTO();
                    hDto.setDiaSemana(h.getDia());
                    hDto.setHoraInicio(h.getHoraInicio());
                    hDto.setHoraFin(h.getHoraFin());

                    horariosPorEntrenador.computeIfAbsent(emp.getIdEmpleado(), k -> new ArrayList<>()).add(hDto);
                }

                // Crear EntrenadorPlanDTO para cada entrenador
                for (Map.Entry<Integer, Empleado> entry : entrenadoresMap.entrySet()) {
                    Empleado emp = entry.getValue();
                    List<HorarioDTO> horariosEntrenador = horariosPorEntrenador.get(emp.getIdEmpleado());

                    EntrenadorPlanDTO entrenadorDto = new EntrenadorPlanDTO(
                        emp.getPersona().getNombre(),
                        emp.getPersona().getApellidos(),
                        horariosEntrenador
                    );
                    entrenadores.add(entrenadorDto);
                }

                dto.setEntrenadores(entrenadores);
                // Para planes estándar, no establecemos horarios generales ya que cada entrenador tiene los suyos
                dto.setHorarios(new ArrayList<>());
            }

            // Asistencias (igual para ambos tipos de planes)
            List<AsistenciaDTO> asistencias = new ArrayList<>();
            List<Asistencia> asistenciasBD = asistenciaRepository.findByClienteIdCliente(idCliente);
            for (Asistencia a : asistenciasBD) {
                if (!a.getFecha().isBefore(inscripcion.getFechaInicio()) && !a.getFecha().isAfter(inscripcion.getFechaFin())) {
                    AsistenciaDTO aDto = new AsistenciaDTO();
                    aDto.setFecha(a.getFecha());
                    aDto.setEstado(a.getEstado());
                    asistencias.add(aDto);
                }
            }
            dto.setAsistencias(asistencias);

            resultado.add(dto);
        }
        return resultado;
    }

    public List<PlanesInscritosDTO> obtenerHistorialPlanesPorCliente(Integer idCliente) {
        // ⭐ NUEVO: Solo obtener inscripciones FINALIZADAS y CANCELADAS para historial
        List<String> estados = List.of(
            EstadoInscripcion.FINALIZADO.name(),
            EstadoInscripcion.CANCELADO.name()
        );
        List<Inscripcion> inscripciones = inscripcionRepository.findByClienteIdClienteAndEstadoIn(
                idCliente, estados
        );
        List<PlanesInscritosDTO> resultado = new ArrayList<>();

        for (Inscripcion inscripcion : inscripciones) {
            PlanesInscritosDTO dto = new PlanesInscritosDTO();
            dto.setNombrePlan(inscripcion.getPlan().getNombre());
            dto.setDescripcionPlan(inscripcion.getPlan().getDescripcion());
            dto.setFechaInicio(inscripcion.getFechaInicio());
            dto.setFechaFin(inscripcion.getFechaFin());
            dto.setTipoPlan(inscripcion.getPlan().getTipoPlan().name());
            dto.setEstadoInscripcion(inscripcion.getEstado().name()); // NUEVO: Incluir estado

            // ⭐ MISMA LÓGICA: Distinguir entre planes Premium y Estándar
            if (inscripcion.getPlan().getTipoPlan().equals(TipoPlan.PREMIUM)) {
                // PLAN PREMIUM: Un solo entrenador personalizado
                Empleado entrenador = null;
                if (!inscripcion.getDetallesInscripcion().isEmpty()) {
                    entrenador = inscripcion.getDetallesInscripcion().get(0).getHorarioEmpleado().getEmpleado();
                }
                if (entrenador != null) {
                    dto.setEntrenadorNombre(entrenador.getPersona().getNombre());
                    dto.setEntrenadorApellido(entrenador.getPersona().getApellidos());
                }

                // Horarios del entrenador asignado
                List<HorarioDTO> horarios = new ArrayList<>();
                for (DetalleInscripcion det : inscripcion.getDetallesInscripcion()) {
                    HorarioEmpleado h = det.getHorarioEmpleado();
                    HorarioDTO hDto = new HorarioDTO();
                    hDto.setDiaSemana(h.getDia());
                    hDto.setHoraInicio(h.getHoraInicio());
                    hDto.setHoraFin(h.getHoraFin());
                    horarios.add(hDto);
                }
                dto.setHorarios(horarios);

            } else if (inscripcion.getPlan().getTipoPlan().equals(TipoPlan.ESTANDAR)) {
                // PLAN ESTÁNDAR: Múltiples entrenadores disponibles
                List<EntrenadorPlanDTO> entrenadores = new ArrayList<>();

                // Agrupar horarios por entrenador
                Map<Integer, List<HorarioDTO>> horariosPorEntrenador = new HashMap<>();
                Map<Integer, Empleado> entrenadoresMap = new HashMap<>();

                for (DetalleInscripcion det : inscripcion.getDetallesInscripcion()) {
                    HorarioEmpleado h = det.getHorarioEmpleado();
                    Empleado emp = h.getEmpleado();

                    entrenadoresMap.put(emp.getIdEmpleado(), emp);

                    HorarioDTO hDto = new HorarioDTO();
                    hDto.setDiaSemana(h.getDia());
                    hDto.setHoraInicio(h.getHoraInicio());
                    hDto.setHoraFin(h.getHoraFin());

                    horariosPorEntrenador.computeIfAbsent(emp.getIdEmpleado(), k -> new ArrayList<>()).add(hDto);
                }

                // Crear EntrenadorPlanDTO para cada entrenador
                for (Map.Entry<Integer, Empleado> entry : entrenadoresMap.entrySet()) {
                    Empleado emp = entry.getValue();
                    List<HorarioDTO> horariosEntrenador = horariosPorEntrenador.get(emp.getIdEmpleado());

                    EntrenadorPlanDTO entrenadorDto = new EntrenadorPlanDTO(
                        emp.getPersona().getNombre(),
                        emp.getPersona().getApellidos(),
                        horariosEntrenador
                    );
                    entrenadores.add(entrenadorDto);
                }

                dto.setEntrenadores(entrenadores);
                // Para planes estándar, no establecemos horarios generales ya que cada entrenador tiene los suyos
                dto.setHorarios(new ArrayList<>());
            }

            // Asistencias del historial (planes pasados)
            List<AsistenciaDTO> asistencias = new ArrayList<>();
            List<Asistencia> asistenciasBD = asistenciaRepository.findByClienteIdCliente(idCliente);
            for (Asistencia a : asistenciasBD) {
                if (!a.getFecha().isBefore(inscripcion.getFechaInicio()) && !a.getFecha().isAfter(inscripcion.getFechaFin())) {
                    AsistenciaDTO aDto = new AsistenciaDTO();
                    aDto.setFecha(a.getFecha());
                    aDto.setEstado(a.getEstado());
                    asistencias.add(aDto);
                }
            }
            dto.setAsistencias(asistencias);

            resultado.add(dto);
        }
        return resultado;
    }

    // --- NUEVO: Métodos para visualización de planes/clientes por tipo de entrenador ---
    public List<PlanConClientesDTO> obtenerPlanesClientesPremium() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Empleado entrenador = empleadoRepository.findByPersonaUsuarioNombreUsuario(username);
        if (entrenador == null) {
            log.error("No se encontró un empleado asociado al usuario autenticado: {}", username);
            throw new RuntimeException("Acceso denegado: usuario no es un empleado válido.");
        }
        if (entrenador.getTipoInstructor() == null) {
            log.error("El empleado {} no tiene tipoInstructor asignado.", entrenador.getIdEmpleado());
            throw new RuntimeException("Acceso denegado: el empleado no tiene tipo de instructor asignado.");
        }
        if (!"PREMIUM".equals(entrenador.getTipoInstructor().name())) {
            log.warn("Acceso denegado: el empleado {} tiene tipoInstructor {} y no PREMIUM", entrenador.getIdEmpleado(), entrenador.getTipoInstructor().name());
            throw new RuntimeException("Acceso denegado: solo entrenadores premium pueden acceder a este recurso.");
        }
        List<Inscripcion> inscripciones = inscripcionRepository.findAll();
        Map<Integer, PlanConClientesDTO> planesMap = new HashMap<>();
        for (Inscripcion insc : inscripciones) {
            if (insc.getPlan().getTipoPlan().name().equals("PREMIUM") && insc.getEstado().name().equals("ACTIVO")) {
                // Verificar si el cliente tiene algún detalle de inscripción con este entrenador
                boolean tieneRelacion = insc.getDetallesInscripcion().stream()
                    .anyMatch(det -> det.getHorarioEmpleado().getEmpleado().getIdEmpleado().equals(entrenador.getIdEmpleado()));
                if (tieneRelacion) {
                    Cliente cliente = insc.getCliente();
                    // Obtener los horarios del entrenador premium en esta inscripción
                    List<HorarioDTO> horariosEntrenador = insc.getDetallesInscripcion().stream()
                        .filter(det -> det.getHorarioEmpleado().getEmpleado().getIdEmpleado().equals(entrenador.getIdEmpleado()))
                        .map(det -> {
                            HorarioEmpleado h = det.getHorarioEmpleado();
                            HorarioDTO hDto = new HorarioDTO();
                            hDto.setDiaSemana(h.getDia());
                            hDto.setHoraInicio(h.getHoraInicio());
                            hDto.setHoraFin(h.getHoraFin());
                            return hDto;
                        })
                        .toList();
                    ClienteConHorariosDTO clienteDTO = new ClienteConHorariosDTO(
                        cliente.getIdCliente(),
                        cliente.getPersona().getNombre(),
                        cliente.getPersona().getApellidos(),
                        horariosEntrenador,
                        insc.getIdInscripcion() // Se agrega el idInscripcion activo premium
                    );
                    int idPlan = insc.getPlan().getIdPlan();
                    PlanConClientesDTO planDTO = planesMap.get(idPlan);
                    if (planDTO == null) {
                        planDTO = new PlanConClientesDTO(
                            idPlan,
                            insc.getPlan().getNombre(),
                            insc.getPlan().getDescripcion(),
                            insc.getPlan().getTipoPlan().name(),
                            new ArrayList<>()
                        );
                        planesMap.put(idPlan, planDTO);
                    }
                    planDTO.getClientes().add(clienteDTO);
                }
            }
        }
        return new ArrayList<>(planesMap.values());
    }

    public List<PlanConClientesDTO> obtenerPlanesClientesEstandar() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Empleado entrenador = empleadoRepository.findByPersonaUsuarioNombreUsuario(username);
        if (entrenador == null) {
            log.error("No se encontró un empleado asociado al usuario autenticado: {}", username);
            throw new RuntimeException("Acceso denegado: usuario no es un empleado válido.");
        }
        if (entrenador.getTipoInstructor() == null) {
            log.error("El empleado {} no tiene tipoInstructor asignado.", entrenador.getIdEmpleado());
            throw new RuntimeException("Acceso denegado: el empleado no tiene tipo de instructor asignado.");
        }
        if (!"ESTANDAR".equals(entrenador.getTipoInstructor().name())) {
            log.warn("Acceso denegado: el empleado {} tiene tipoInstructor {} y no ESTANDAR", entrenador.getIdEmpleado(), entrenador.getTipoInstructor().name());
            throw new RuntimeException("Acceso denegado: solo entrenadores estándar pueden acceder a este recurso.");
        }
        List<Inscripcion> inscripciones = inscripcionRepository.findAll();
        Map<Integer, PlanConClientesDTO> planesMap = new HashMap<>();
        for (Inscripcion insc : inscripciones) {
            if (insc.getPlan().getTipoPlan().name().equals("ESTANDAR") && insc.getEstado().name().equals("ACTIVO")) {
                // Verificar si el entrenador tiene algún detalle de inscripción en esta inscripción
                boolean tieneRelacion = insc.getDetallesInscripcion().stream()
                    .anyMatch(det -> det.getHorarioEmpleado().getEmpleado().getIdEmpleado().equals(entrenador.getIdEmpleado()));
                if (tieneRelacion) {
                    Cliente cliente = insc.getCliente();
                    // Solo los horarios de este cliente que correspondan al entrenador autenticado
                    List<HorarioDTO> horariosClienteEntrenador = insc.getDetallesInscripcion().stream()
                        .filter(det -> det.getHorarioEmpleado().getEmpleado().getIdEmpleado().equals(entrenador.getIdEmpleado()))
                        .map(det -> {
                            HorarioEmpleado h = det.getHorarioEmpleado();
                            HorarioDTO hDto = new HorarioDTO();
                            hDto.setDiaSemana(h.getDia());
                            hDto.setHoraInicio(h.getHoraInicio());
                            hDto.setHoraFin(h.getHoraFin());
                            return hDto;
                        })
                        .toList();
                    ClienteConHorariosDTO clienteDTO = new ClienteConHorariosDTO(
                        cliente.getIdCliente(),
                        cliente.getPersona().getNombre(),
                        cliente.getPersona().getApellidos(),
                        horariosClienteEntrenador,
                        insc.getIdInscripcion() // Se agrega el idInscripcion activo estandar
                    );
                    int idPlan = insc.getPlan().getIdPlan();
                    PlanConClientesDTO planDTO = planesMap.get(idPlan);
                    if (planDTO == null) {
                        planDTO = new PlanConClientesDTO(
                            idPlan,
                            insc.getPlan().getNombre(),
                            insc.getPlan().getDescripcion(),
                            insc.getPlan().getTipoPlan().name(),
                            new ArrayList<>()
                        );
                        planesMap.put(idPlan, planDTO);
                    }
                    planDTO.getClientes().add(clienteDTO);
                }
            }
        }
        return new ArrayList<>(planesMap.values());
    }

}
