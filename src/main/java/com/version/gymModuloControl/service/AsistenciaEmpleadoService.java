package com.version.gymModuloControl.service;

import com.version.gymModuloControl.dto.AsistenciaEmpleadoDTO;
import com.version.gymModuloControl.model.*;
import com.version.gymModuloControl.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;


import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AsistenciaEmpleadoService {

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private HorarioEmpleadoRepository horarioEmpleadoRepository;

    @Autowired
    private AsistenciaEmpleadoRepository asistenciaEmpleadoRepository;

    @Transactional
    public String marcarAsistencia(Integer idEmpleado) {
        Empleado empleado = empleadoRepository.findById(idEmpleado)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

        LocalDateTime ahora = LocalDateTime.now();
        LocalTime horaActual = ahora.toLocalTime();
        String diaActual = normalizarDia(ahora.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("es", "ES")));

        List<HorarioEmpleado> horariosHoy = empleado.getHorarios().stream()
                .filter(h -> h.getEstado() && normalizarDia(h.getDia()).equals(diaActual))
                .toList();

        if (horariosHoy.isEmpty()) {
            throw new RuntimeException("No tienes horarios configurados para hoy (" + diaActual + ")");
        }

        Optional<HorarioEmpleado> horarioActualOpt = horariosHoy.stream().filter(h -> {
            LocalTime inicio = h.getHoraInicio().minusMinutes(15);
            LocalTime fin = h.getHoraFin().plusMinutes(0);

            if (h.getHoraFin().isBefore(h.getHoraInicio())) {
                LocalDateTime inicioHoy = ahora.with(inicio);
                LocalDateTime finManana = ahora.plusDays(1).with(fin);
                return ahora.isAfter(inicioHoy) || ahora.isBefore(finManana);
            } else {
                return !horaActual.isBefore(inicio) && !horaActual.isAfter(fin);
            }
        }).min(Comparator.comparing(HorarioEmpleado::getHoraInicio));

        if (horarioActualOpt.isEmpty()) {
            String horariosDisponibles = horariosHoy.stream()
                    .map(h -> String.format("Horario: %s - %s",
                            h.getHoraInicio().toString(),
                            h.getHoraFin().toString()))
                    .collect(java.util.stream.Collectors.joining(", "));

            throw new RuntimeException(String.format(
                    "No tienes un horario activo para este momento (%s). Hora actual: %s. Horarios disponibles: %s",
                    diaActual, horaActual, horariosDisponibles));
        }

        HorarioEmpleado horarioActual = horarioActualOpt.get();

        boolean asistenciaExistente = asistenciaEmpleadoRepository
                .existsByEmpleadoAndFechaAndHoraEntradaBetween(
                        empleado,
                        ahora.toLocalDate(),
                        horarioActual.getHoraInicio().minusMinutes(15),
                        horarioActual.getHoraFin().plusMinutes(0)
                );

        if (asistenciaExistente) {
            throw new RuntimeException("Ya has marcado asistencia para este bloque de horario");
        }

        AsistenciaEmpleado asistencia = new AsistenciaEmpleado();
        asistencia.setEmpleado(empleado);
        asistencia.setFecha(ahora.toLocalDate());
        asistencia.setHoraEntrada(horaActual);
        asistencia.setEstadoPuntualidad(evaluarPuntualidad(horarioActual, horaActual));
        asistenciaEmpleadoRepository.save(asistencia);

        return "Asistencia marcada correctamente";
    }

    private EstadoPuntualidad evaluarPuntualidad(HorarioEmpleado horario, LocalTime horaActual) {
        if (horaActual.isBefore(horario.getHoraInicio().plusMinutes(15))) {
            return EstadoPuntualidad.PUNTUAL;
        } else if (horaActual.isBefore(horario.getHoraInicio().plusMinutes(30))) {
            return EstadoPuntualidad.TARDANZA;
        } else {
            return EstadoPuntualidad.TARDANZA_GRAVE;
        }
    }

    private String normalizarDia(String dia) {
        String normalizado = Normalizer.normalize(dia, Normalizer.Form.NFD);
        return normalizado.replaceAll("\\p{InCombiningDiacriticalMarks}+", "").toUpperCase();
    }

    public List<AsistenciaEmpleadoDTO> listarAsistencias() {
        return asistenciaEmpleadoRepository.findByEstadoOrderByFechaDescHoraEntradaDesc(true).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    private AsistenciaEmpleadoDTO convertirADTO(AsistenciaEmpleado asistencia) {
        return new AsistenciaEmpleadoDTO(
                asistencia.getIdAsistenciaEmpleado(),
                asistencia.getEmpleado().getPersona().getNombre(),
                asistencia.getEmpleado().getPersona().getApellidos(),
                asistencia.getFecha(),
                asistencia.getHoraEntrada(),
                asistencia.getEstadoPuntualidad()
        );
    }


//    @Scheduled(cron = "0 */2 * * * *")
//    @Transactional(rollbackFor = Exception.class)
//    public void registrarFaltasAutomaticas() {
//        log.info("=== Iniciando verificación de faltas ===");
//        LocalDateTime ahora = LocalDateTime.now();
//        String diaActual = normalizarDia(ahora.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("es", "ES")));
//
//        List<Empleado> empleados = empleadoRepository.findByEstado(true);
//        log.info("Verificando {} empleados", empleados.size());
//
//        for (Empleado empleado : empleados) {
//            try {
//                List<HorarioEmpleado> horariosHoy = empleado.getHorarios().stream()
//                        .filter(h -> h.getEstado() && normalizarDia(h.getDia()).equals(diaActual))
//                        .filter(h -> h.getHoraFin().isBefore(ahora.toLocalTime()))
//                        .toList();
//
//                for (HorarioEmpleado horario : horariosHoy) {
//                    registrarFaltaParaHorario(empleado, horario, ahora);
//                }
//            } catch (Exception e) {
//                log.error("Error procesando empleado {}: {}", empleado.getIdEmpleado(), e.getMessage());
//                // No propagamos la excepción para continuar con el siguiente empleado
//            }
//        }
//        log.info("=== Verificación de faltas completada ===");
//    }
//
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    protected void registrarFaltaParaHorario(Empleado empleado, HorarioEmpleado horario, LocalDateTime ahora) {
//        log.info("Verificando horario {} - {} para empleado: {} {}",
//                horario.getHoraInicio(),
//                horario.getHoraFin(),
//                empleado.getPersona().getNombre(),
//                empleado.getPersona().getApellidos());
//
//        boolean asistenciaRegistrada = asistenciaEmpleadoRepository
//                .existsByEmpleadoAndFechaAndHoraEntradaBetween(
//                        empleado,
//                        ahora.toLocalDate(),
//                        horario.getHoraInicio().minusMinutes(15),
//                        horario.getHoraFin()
//                );
//
//        if (!asistenciaRegistrada) {
//            AsistenciaEmpleado falta = new AsistenciaEmpleado();
//            falta.setEmpleado(empleado);
//            falta.setFecha(ahora.toLocalDate());
//            falta.setHoraEntrada(horario.getHoraInicio());
//            falta.setEstadoPuntualidad(EstadoPuntualidad.FALTO);
//            falta.setEstado(true);
//
//            try {
//                asistenciaEmpleadoRepository.save(falta);
//                log.info("Falta registrada exitosamente");
//            } catch (Exception e) {
//                log.error("Error al registrar falta: {}", e.getMessage());
//                throw e; // Propagamos la excepción para que se haga rollback de esta transacción específica
//            }
//        } else {
//            log.info("El empleado ya tiene registro de asistencia para este horario");
//        }
//    }

}
