package com.version.gymModuloControl.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.version.gymModuloControl.dto.*;
import com.version.gymModuloControl.model.*;
import com.version.gymModuloControl.repository.*;
import com.version.gymModuloControl.service.*;

@RestController
@RequestMapping("/api/alquiler")
public class AlquilerController {

    @Autowired
    private AlquilerService alquilerService;

    @Autowired
    private DetalleAlquilerService detalleAlquilerService;

    @Autowired
    private PagoAlquilerService pagoAlquilerService;

    @Autowired
    private AlquilerSchedulerService alquilerSchedulerService;

    @Autowired
    private PiezaRepository piezaRepository;

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

    @GetMapping("/verificar-vencidos")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<?> verificarAlquileresVencidos() {
        try {
            int cantidadActualizada = alquilerSchedulerService.verificarYActualizarAlquileresVencidos();
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Verificación de alquileres vencidos completada");
            response.put("actualizados", cantidadActualizada);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al verificar alquileres vencidos: " + e.getMessage());
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

    @PostMapping("/calcular-precio")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<?> calcularPrecio(@RequestBody Map<String, Object> request) {
        try {
            LocalDate fechaInicio = LocalDate.parse((String) request.get("fechaInicio"));
            LocalDate fechaFin = LocalDate.parse((String) request.get("fechaFin"));
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> detalles = (List<Map<String, Object>>) request.get("detalles");
            
            // Calculamos los días incluyendo el día inicial y el día final
            // Si el alquiler es del 2 al 12, cuenta: 2,3,4,5,6,7,8,9,10,11,12 (11 días)
            long diasCalculados = ChronoUnit.DAYS.between(fechaInicio, fechaFin) + 1;
            
            // Validar que el período de alquiler esté entre 1 y 30 días
            if (diasCalculados > 30) {
                return ResponseEntity.badRequest()
                    .body("El período de alquiler no puede exceder los 30 días");
            }
            if (diasCalculados < 1) {
                diasCalculados = 1; // Mínimo 1 día
            }
            final long diasAlquiler = diasCalculados;
            
            List<CalculoAlquilerDTO> calculoDetalles = detalles.stream().map(detalle -> {
                Integer piezaId = Integer.valueOf(detalle.get("piezaId").toString());
                Integer cantidad = Integer.valueOf(detalle.get("cantidad").toString());
                
                Pieza pieza = piezaRepository.findById(piezaId)
                    .orElseThrow(() -> new IllegalArgumentException("Pieza no encontrada"));
                
                BigDecimal subtotal = pieza.getPrecioAlquiler()
                    .multiply(BigDecimal.valueOf(cantidad))
                    .multiply(BigDecimal.valueOf(diasAlquiler));
                
                return new CalculoAlquilerDTO(
                    piezaId,
                    pieza.getNombre(),
                    cantidad,
                    pieza.getPrecioAlquiler(),
                    (int) diasAlquiler,
                    subtotal
                );
            }).toList();
            
            BigDecimal totalGeneral = calculoDetalles.stream()
                .map(CalculoAlquilerDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            Map<String, Object> response = new HashMap<>();
            response.put("diasAlquiler", diasAlquiler);
            response.put("detalles", calculoDetalles);
            response.put("total", totalGeneral);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al calcular precio: " + e.getMessage());
        }
    }
}
