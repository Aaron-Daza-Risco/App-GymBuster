package com.version.gymModuloControl.controller;

import com.version.gymModuloControl.model.Especialidad;
import com.version.gymModuloControl.repository.EspecialidadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/especialidad")
@CrossOrigin(origins = "*")
public class EspecialidadController {

    @Autowired
    private EspecialidadRepository especialidadRepository;

    @GetMapping("/listar")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<List<Especialidad>> listarEspecialidades() {
        List<Especialidad> especialidades = especialidadRepository.findByEstadoTrue();
        return ResponseEntity.ok(especialidades);
    }
}
