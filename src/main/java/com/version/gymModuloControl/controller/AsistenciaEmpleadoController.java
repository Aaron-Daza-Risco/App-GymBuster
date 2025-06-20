package com.version.gymModuloControl.controller;

import com.version.gymModuloControl.dto.AsistenciaEmpleadoDTO;
import com.version.gymModuloControl.model.AsistenciaEmpleado;
import com.version.gymModuloControl.service.AsistenciaEmpleadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/asistencia")
public class AsistenciaEmpleadoController {

    @Autowired
    private AsistenciaEmpleadoService asistenciaEmpleadoService;

    @PostMapping("/marcar")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<?> marcarAsistencia(@RequestParam Integer idEmpleado) {
        try {
            String asistencia = asistenciaEmpleadoService.marcarAsistencia(idEmpleado);
            return ResponseEntity.ok(asistencia);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/listar")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<List<AsistenciaEmpleadoDTO>> listarAsistencias() {
        return ResponseEntity.ok(asistenciaEmpleadoService.listarAsistencias());
    }



}