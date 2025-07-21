package com.version.gymModuloControl.controller;

import com.version.gymModuloControl.dto.PlanConClientesDTO;
import com.version.gymModuloControl.service.InscripcionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/entrenador")
public class EntrenadorController {

    @Autowired
    private InscripcionService inscripcionService;

    // Endpoint para entrenadores premium
    @GetMapping("/premium/planes-clientes")
    @PreAuthorize("hasRole('ENTRENADOR')")
    public ResponseEntity<List<PlanConClientesDTO>> obtenerPlanesClientesPremium() {
        return ResponseEntity.ok(inscripcionService.obtenerPlanesClientesPremium());
    }

    // Endpoint para entrenadores estándar
    @GetMapping("/estandar/planes-clientes")
    @PreAuthorize("hasRole('ENTRENADOR')")
    public ResponseEntity<List<PlanConClientesDTO>> obtenerPlanesClientesEstandar() {
        return ResponseEntity.ok(inscripcionService.obtenerPlanesClientesEstandar());
    }
}
