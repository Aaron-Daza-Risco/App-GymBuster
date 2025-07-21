package com.version.gymModuloControl.controller;

import com.version.gymModuloControl.dto.DetalleVentaDTO;
import com.version.gymModuloControl.dto.DetallesVentaRequest;
import com.version.gymModuloControl.dto.VentaConDetalleDTO;
import com.version.gymModuloControl.model.DetalleVenta;
import com.version.gymModuloControl.model.PagoVenta;
import com.version.gymModuloControl.model.Venta;
import com.version.gymModuloControl.service.DetalleVentaService;
import com.version.gymModuloControl.service.PagoVentaService;
import com.version.gymModuloControl.service.VentaService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/venta")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    @Autowired
    private DetalleVentaService detalleVentaService;

    @Autowired
    private PagoVentaService pagoVentaService;

    // --------- VENTAS ---------

    @GetMapping("/listar")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<List<VentaConDetalleDTO>> listar() {
        return ResponseEntity.ok(ventaService.listarVentasConDetalle());
    }


    @PostMapping("/guardar")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<?> guardarVenta(@RequestBody Venta venta) {
        try {
            Venta guardada = ventaService.guardarVenta(venta);
            return ResponseEntity.ok(guardada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/cambiar-estado/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<?> cambiarEstado(@PathVariable Integer id, @RequestParam Boolean estado) {
        Venta ventaActualizada = ventaService.cambiarEstadoVenta(id, estado);
        if (ventaActualizada != null) {
            return ResponseEntity.ok(ventaActualizada);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/cancelar/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<?> cancelarVenta(@PathVariable Integer id) {
        try {
            ventaService.cancelarVenta(id);
            return ResponseEntity.ok("Venta cancelada correctamente.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // --------- DETALLES ---------

    @PostMapping("/detalle/agregar-lote")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<?> agregarDetallesVenta(@RequestBody DetallesVentaRequest request) {
        try {
            List<DetalleVenta> detallesGuardados = detalleVentaService.agregarDetallesVenta(request.getVentaId(), request.getDetalles());
            return ResponseEntity.ok(detallesGuardados);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @GetMapping("/detalle/listar/{ventaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<List<DetalleVenta>> listarDetalles(@PathVariable Integer ventaId) {
        return ResponseEntity.ok(detalleVentaService.listarDetallesPorVentaId(ventaId));
    }

    @DeleteMapping("/detalle/eliminar/{detalleId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<?> eliminarDetalle(@PathVariable Integer detalleId) {
        boolean eliminado = detalleVentaService.eliminarDetalleVenta(detalleId);
        if (eliminado) {
            return ResponseEntity.ok("Detalle eliminado y stock restaurado.");
        } else {
            return ResponseEntity.badRequest().body("Detalle no encontrado.");
        }
    }

    // --------- PAGO ---------

    @PostMapping("/pago/registrar")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<?> registrarPago(@RequestBody PagoVenta pago) {
        try {
            PagoVenta pagoGuardado = pagoVentaService.registrarPago(
                    pago.getVenta().getIdVenta(),
                    pago.getMontoPagado(),
                    pago.getMetodoPago()
            );
            return ResponseEntity.ok(pagoGuardado);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
