package com.version.gymModuloControl.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.version.gymModuloControl.dto.AlquilerCompletoDTO;
import com.version.gymModuloControl.dto.AlquilerConDetalleDTO;
import com.version.gymModuloControl.model.Alquiler;
import com.version.gymModuloControl.model.DetalleAlquiler;
import com.version.gymModuloControl.model.EstadoAlquiler;
import com.version.gymModuloControl.service.AlquilerService;
import com.version.gymModuloControl.service.DetalleAlquilerService;
import com.version.gymModuloControl.service.PagoAlquilerService;

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


    @PostMapping("/crear-completo")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<?> crearAlquilerCompleto(@RequestBody AlquilerCompletoDTO alquilerCompletoDTO) {
        try {
            AlquilerConDetalleDTO alquilerCreado = alquilerService.crearAlquilerCompleto(alquilerCompletoDTO);
            return ResponseEntity.ok(alquilerCreado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Error al procesar el alquiler completo: " + e.getMessage());
        }
    }

    @PutMapping("/cambiar-estado/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<?> cambiarEstado(@PathVariable Integer id, @RequestParam String estado) {
        try {
            EstadoAlquiler nuevoEstado = EstadoAlquiler.valueOf(estado.toUpperCase());
            Alquiler alquilerActualizado = alquilerService.cambiarEstadoAlquiler(id, nuevoEstado);
            if (alquilerActualizado != null) {
                return ResponseEntity.ok(alquilerActualizado);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Estado inválido. Los estados válidos son: " + 
                    java.util.Arrays.toString(EstadoAlquiler.values()));
        }
    }
    
    @PutMapping("/finalizar/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<?> finalizarAlquiler(@PathVariable Integer id) {
        try {
            Alquiler alquilerActualizado = alquilerService.finalizarAlquiler(id);
            if (alquilerActualizado != null) {
                return ResponseEntity.ok(alquilerActualizado);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al finalizar el alquiler: " + e.getMessage());
        }
    }
    
    @PutMapping("/cancelar/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<?> cancelarAlquiler(@PathVariable Integer id) {
        try {
            Alquiler alquilerActualizado = alquilerService.cancelarAlquiler(id);
            if (alquilerActualizado != null) {
                return ResponseEntity.ok(alquilerActualizado);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al cancelar el alquiler: " + e.getMessage());
        }
    }
    
    @PutMapping("/vencido/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<?> marcarVencido(@PathVariable Integer id) {
        try {
            Alquiler alquilerActualizado = alquilerService.marcarVencido(id);
            if (alquilerActualizado != null) {
                return ResponseEntity.ok(alquilerActualizado);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al marcar el alquiler como vencido: " + e.getMessage());
        }
    }

    @PutMapping("/registrar-devolucion/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<?> registrarDevolucion(@PathVariable Integer id) {
        try {
            Alquiler alquilerActualizado = alquilerService.registrarDevolucion(id);
            return ResponseEntity.ok(alquilerActualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al procesar la devolución: " + e.getMessage());
        }
    }

    // --------- DETALLES ---------

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
}
