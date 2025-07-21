package com.version.gymModuloControl.controller;

import com.version.gymModuloControl.dto.*;
import com.version.gymModuloControl.model.Empleado;
import com.version.gymModuloControl.model.Inscripcion;
import com.version.gymModuloControl.model.PagoInscripcion;
import com.version.gymModuloControl.service.InscripcionService;
import com.version.gymModuloControl.service.InstructorService;
import com.version.gymModuloControl.service.PagoInscripcionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inscripciones")
public class InscripcionController {

    @Autowired
    private InscripcionService inscripcionService;

    @Autowired
    private InstructorService instructorService;

    @Autowired
    private PagoInscripcionService pagoInscripcionService;

    // Endpoint para registrar inscripción
    @PostMapping("/registrar")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public InscripcionResponseDTO registrarInscripcion(@RequestBody InscripcionRequestDTO request) {
        return inscripcionService.registrarInscripcion(request);
    }


    @GetMapping("/instructores-disponibles/{idPlan}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public List<InstructorDisponibleDTO> listarInstructoresDisponibles(@PathVariable Integer idPlan) {
        return instructorService.obtenerInstructoresDisponiblesPorPlan(idPlan);
    }

    @GetMapping("/horarios-instructor/{idEmpleado}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA', 'ENTRENADOR')")
    public List<HorarioInstructorDTO> listarHorariosPorInstructor(@PathVariable Integer idEmpleado) {
        return instructorService.obtenerHorariosPorInstructor(idEmpleado);
    }

    @PostMapping("/pago/registrar")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<?> registrarPago(@RequestBody PagoInscripcion pago) {
        try {
            PagoInscripcion pagoGuardado = pagoInscripcionService.registrarPago(
                    pago.getInscripcion().getIdInscripcion(),
                    pago.getMontoPagado(),
                    pago.getMetodoPago()
            );
            return ResponseEntity.ok(pagoGuardado);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/inscripciones/{id}/detalle")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<InscripcionConDetalleDTO> obtenerInscripcionConDetalle(@PathVariable Integer id) {
        InscripcionConDetalleDTO dto = inscripcionService.obtenerInscripcionConDetalle(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/listar")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public List<InscripcionConDetalleDTO> listarInscripciones() {
        return inscripcionService.listarTodasLasInscripciones();
    }

    @PutMapping("/cancelar/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<?> cancelarInscripcion(@PathVariable Integer id) {
        try {
            inscripcionService.cancelarInscripcion(id);
            return ResponseEntity.ok("Inscripción cancelada correctamente.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/planes-inscritos/{idCliente}")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<List<PlanesInscritosDTO>> obtenerPlanesInscritosPorCliente(@PathVariable Integer idCliente) {
        List<PlanesInscritosDTO> planes = inscripcionService.obtenerPlanesInscritosPorCliente(idCliente);
        return ResponseEntity.ok(planes);
    }

    @GetMapping("/historial-planes/{idCliente}")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<List<PlanesInscritosDTO>> obtenerHistorialPlanesPorCliente(@PathVariable Integer idCliente) {
        List<PlanesInscritosDTO> historial = inscripcionService.obtenerHistorialPlanesPorCliente(idCliente);
        return ResponseEntity.ok(historial);
    }



}