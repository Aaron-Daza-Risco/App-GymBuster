package com.version.gymModuloControl.controller;


import com.version.gymModuloControl.dto.AsistenciaClienteDTO;
import com.version.gymModuloControl.service.AsistenciaService;
import com.version.gymModuloControl.service.InscripcionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AsistenciaController {

    @Autowired
    private AsistenciaService asistenciaService;

    @PostMapping("/asistencia/registrar")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<String> registrarAsistenciaDesdeQR(@RequestParam Integer idInscripcion) {
        try {
            asistenciaService.registrarAsistenciaPorInscripcion(idInscripcion);
            return ResponseEntity.ok("✅ Asistencia registrada correctamente.");
        } catch (InscripcionService.BusinessException e) {
            // Capturar errores de negocio: inscripción inválida, fechas caducadas, etc.
            return ResponseEntity.badRequest().body("❌ " + e.getMessage());
        } catch (Exception e) {
            // Errores inesperados
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("❌ Error al registrar asistencia.");
        }
    }

    @GetMapping("/listar")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public List<AsistenciaClienteDTO> listarAsistencias() {
        return asistenciaService.listarAsistencias();
    }

}