package com.version.gymModuloControl.service;

import com.version.gymModuloControl.dto.AsistenciaClienteDTO;
import com.version.gymModuloControl.model.*;
import com.version.gymModuloControl.repository.AsistenciaRepository;
import com.version.gymModuloControl.repository.InscripcionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AsistenciaService {

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private AsistenciaRepository asistenciaRepository;

    private static final Logger log = LoggerFactory.getLogger(AsistenciaService.class);

    @Transactional
    public void registrarAsistenciaPorInscripcion(Integer idInscripcion) {
        // 1. Buscar inscripción
        Inscripcion inscripcion = inscripcionRepository.findById(idInscripcion)
                .orElseThrow(() -> new RuntimeException("Inscripción no encontrada."));

        Cliente cliente = inscripcion.getCliente();
        LocalDate hoy = LocalDate.now();

        // 2. Verificar validez de la inscripción
        boolean esValida = inscripcion.getEstado().equals(EstadoInscripcion.ACTIVO) &&
                !hoy.isBefore(inscripcion.getFechaInicio()) &&
                !hoy.isAfter(inscripcion.getFechaFin());

        // 3. Crear asistencia
        Asistencia asistencia = new Asistencia();
        asistencia.setCliente(cliente);
        asistencia.setFecha(hoy);
        asistencia.setHora(LocalTime.now());
        asistencia.setEstado(true);

        // 4. Determinar turno
        LocalTime ahora = LocalTime.now();
        if (ahora.isBefore(LocalTime.NOON)) {
            asistencia.setTurno(Turno.Mañana);
        } else if (ahora.isBefore(LocalTime.of(18, 0))) {
            asistencia.setTurno(Turno.Tarde);
        } else {
            asistencia.setTurno(Turno.Noche);
        }

        asistenciaRepository.save(asistencia);

        // 5. Validación final
        if (!esValida) {
            throw new RuntimeException("❌ La inscripción está vencida o no está activa. Asistencia marcada como INVÁLIDA.");
        }
    }

    public List<AsistenciaClienteDTO> listarAsistencias() {
        return asistenciaRepository.findAll().stream()
                .sorted((a1, a2) -> {
                    int cmp = a2.getFecha().compareTo(a1.getFecha());
                    if (cmp == 0) {
                        // Si la fecha es igual, compara por hora (puede ser null)
                        if (a2.getHora() == null && a1.getHora() == null) return 0;
                        if (a2.getHora() == null) return 1;
                        if (a1.getHora() == null) return -1;
                        return a2.getHora().compareTo(a1.getHora());
                    }
                    return cmp;
                })
                .map(a -> new AsistenciaClienteDTO(
                        a.getCliente().getPersona().getNombre(),
                        a.getCliente().getPersona().getApellidos(),
                        a.getFecha(),
                        a.getHora(),
                        a.getTurno(),
                        a.getEstado()
                ))
                .collect(Collectors.toList());
    }

    @Scheduled(cron = "0 0 0 * * *") // Todos los días a las 00:00
    @Transactional(rollbackFor = Exception.class)
    public void registrarFaltasClientes() {
        LocalDate hoy = LocalDate.now();
        List<Inscripcion> inscripcionesActivas = inscripcionRepository.findAll().stream()
                .filter(i -> i.getEstado().equals(EstadoInscripcion.ACTIVO)
                        && !hoy.isBefore(i.getFechaInicio())
                        && !hoy.isAfter(i.getFechaFin()))
                .toList();

        for (Inscripcion inscripcion : inscripcionesActivas) {
            Cliente cliente = inscripcion.getCliente();
            boolean yaAsistio = asistenciaRepository.existsByClienteAndFecha(cliente, hoy);
            if (!yaAsistio) {
                Asistencia falta = new Asistencia();
                falta.setCliente(cliente);
                falta.setFecha(hoy);
                falta.setHora(null); // No asistió, no hay hora
                falta.setTurno(null); // No aplica turno
                falta.setEstado(false); // No asistió
                asistenciaRepository.save(falta);
                log.info("Falta registrada para cliente {} en fecha {}", cliente.getIdCliente(), hoy);
            }
        }
    }
}