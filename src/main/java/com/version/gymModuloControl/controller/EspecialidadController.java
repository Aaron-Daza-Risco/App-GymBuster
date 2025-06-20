package com.version.gymModuloControl.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.version.gymModuloControl.model.Especialidad;
import com.version.gymModuloControl.service.EspecialidadService;

@RestController
@RequestMapping("/api/especialidad")
@CrossOrigin(origins = "*")
public class EspecialidadController {

    @Autowired
    private EspecialidadService especialidadService;

    @GetMapping("/listar")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<List<Especialidad>> listarEspecialidades() {
        List<Especialidad> especialidades = especialidadService.listarTodos();
        return ResponseEntity.ok(especialidades);
    }

    @PostMapping("/guardar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Especialidad> guardarEspecialidad(@RequestBody Especialidad especialidad) {
        Especialidad guardada = especialidadService.guardarEspecialidad(especialidad);
        return ResponseEntity.ok(guardada);
    }

    @PutMapping("/actualizar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Especialidad> actualizarEspecialidad(@RequestBody Especialidad especialidad) {
        Especialidad actualizada = especialidadService.actualizarEspecialidad(especialidad);
        return ResponseEntity.ok(actualizada);
    }

    @PutMapping("/cambiarEstado/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Especialidad> cambiarEstado(@PathVariable Integer id, @RequestParam Boolean estado) {
        try {
            Especialidad especialidad = especialidadService.cambiarEstado(id, estado);
            return ResponseEntity.ok(especialidad);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
