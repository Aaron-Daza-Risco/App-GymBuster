package com.version.gymModuloControl.controller;

import com.version.gymModuloControl.model.Plan;
import com.version.gymModuloControl.service.PlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plan")
public class PlanController {

    @Autowired
    private PlanService planService;

    @PostMapping("/guardar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Plan> guardarPlan(@RequestBody Plan plan) {
        Plan planGuardado = planService.guardarPlan(plan);
        return ResponseEntity.ok(planGuardado);
    }

    @GetMapping("/listar")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public List<Plan> listarTodos() {
        return planService.listarTodos();
    }


    @PutMapping("/actualizar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> actualizarPlan(@RequestBody Plan plan) {
        try {
            Plan actualizado = planService.actualizarPlan(plan);
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> cambiarEstadoPlan(@PathVariable Integer id, @RequestBody Boolean estado) {
        Plan plan = planService.cambiarEstadoPlan(id, estado);
        if (plan != null) {
            return ResponseEntity.ok(plan);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/eliminar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> eliminarPlan(@PathVariable Integer id) {
        try {
            boolean eliminado = planService.eliminarPlan(id);
            if (eliminado) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}