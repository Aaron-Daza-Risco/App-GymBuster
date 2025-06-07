package com.version.gymModuloControl.controller;

import com.version.gymModuloControl.model.Pieza;
import com.version.gymModuloControl.service.PiezaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pieza")
public class PiezaController {

    @Autowired
    private PiezaService piezaService;

    @PostMapping("/guardar")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<Pieza> guardarPieza(@RequestBody Pieza pieza) {
        Pieza piezaGuardada = piezaService.guardarPieza(pieza);
        return ResponseEntity.ok(piezaGuardada);
    }

    @GetMapping("/listar")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<?> listarPiezas(Authentication authentication) {
        return ResponseEntity.ok(piezaService.listarPiezas());
    }

    @PutMapping("/actualizar")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<Pieza> actualizarPieza(@RequestBody Pieza pieza) {
        Pieza piezaActualizada = piezaService.actualizarPieza(pieza);
        return ResponseEntity.ok(piezaActualizada);
    }

    @PutMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<?> cambiarEstadoPieza(@PathVariable Integer id, @RequestBody Boolean estado) {
        Pieza pieza = piezaService.cambiarEstadoPieza(id, estado);
        if (pieza != null) {
            return ResponseEntity.ok(pieza);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/eliminar/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<?> eliminarPieza(@PathVariable Integer id) {
        boolean eliminada = piezaService.eliminarPieza(id);
        if (eliminada) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}