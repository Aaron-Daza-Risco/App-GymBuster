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
}