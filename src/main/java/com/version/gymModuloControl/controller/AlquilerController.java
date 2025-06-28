package com.version.gymModuloControl.controller;

import com.version.gymModuloControl.dto.AlquilerConDetalleDTO;
import com.version.gymModuloControl.dto.DetallesAlquilerRequest;
import com.version.gymModuloControl.model.Alquiler;
import com.version.gymModuloControl.model.DetalleAlquiler;
import com.version.gymModuloControl.model.PagoAlquiler;
import com.version.gymModuloControl.service.AlquilerService;
import com.version.gymModuloControl.service.DetalleAlquilerService;
import com.version.gymModuloControl.service.PagoAlquilerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alquiler")
public class AlquilerController {

    @Autowired
    private AlquilerService alquilerService;

    @Autowired
    private DetalleAlquilerService detalleAlquilerService;

    @Autowired
    private PagoAlquilerService pagoAlquilerService;

    // --------- ALQUILER ---------

    @GetMapping("/listar")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<List<AlquilerConDetalleDTO>> listar() {
        return ResponseEntity.ok(alquilerService.listarAlquileresConDetalle());
    }

    @PostMapping("/guardar")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<?> guardarAlquiler(@RequestBody Alquiler alquiler) {
        try {
            Alquiler guardado = alquilerService.guardarAlquiler(alquiler);
            return ResponseEntity.ok(guardado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/cambiar-estado/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<?> cambiarEstado(@PathVariable Integer id, @RequestParam Boolean estado) {
        Alquiler alquilerActualizado = alquilerService.cambiarEstadoAlquiler(id, estado);
        if (alquilerActualizado != null) {
            return ResponseEntity.ok(alquilerActualizado);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // --------- DETALLES ---------

    @PostMapping("/detalle/agregar-lote")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<?> agregarDetallesAlquiler(@RequestBody DetallesAlquilerRequest request) {
        try {
            List<DetalleAlquiler> detallesGuardados = detalleAlquilerService.agregarDetallesAlquiler(request.getAlquilerId(), request.getDetalles());
            return ResponseEntity.ok(detallesGuardados);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/detalle/listar/{alquilerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<List<DetalleAlquiler>> listarDetalles(@PathVariable Integer alquilerId) {
        return ResponseEntity.ok(detalleAlquilerService.listarDetallesPorAlquilerId(alquilerId));
    }

    @DeleteMapping("/detalle/eliminar/{detalleId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<?> eliminarDetalle(@PathVariable Integer detalleId) {
        boolean eliminado = detalleAlquilerService.eliminarDetalleAlquiler(detalleId);
        if (eliminado) {
            return ResponseEntity.ok("Detalle eliminado y stock restaurado.");
        } else {
            return ResponseEntity.badRequest().body("Detalle no encontrado.");
        }
    }

    // --------- PAGO ---------

    @PostMapping("/pago/registrar")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<?> registrarPago(@RequestBody PagoAlquiler pago) {
        try {
            PagoAlquiler pagoGuardado = pagoAlquilerService.registrarPago(
                    pago.getAlquiler().getIdAlquiler(),
                    pago.getMontoPagado(),
                    pago.getMetodoPago()
            );
            return ResponseEntity.ok(pagoGuardado);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
