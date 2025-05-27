package com.version.gymModuloControl.controller;

import com.version.gymModuloControl.dto.HorarioEmpleadoInfoDTO;
import com.version.gymModuloControl.model.HorarioEmpleado;
import com.version.gymModuloControl.service.HorarioEmpleadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/horario-empleado")
public class HorarioEmpleadoController {

    @Autowired
    private HorarioEmpleadoService horarioEmpleadoService;

    @PostMapping("/agregar/{empleadoId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> agregarHorario(
            @PathVariable Integer empleadoId,
            @RequestBody HorarioEmpleado horarioEmpleado) {
        try {
            HorarioEmpleado nuevoHorario = horarioEmpleadoService.agregarHorario(empleadoId, horarioEmpleado);
            return ResponseEntity.ok(nuevoHorario);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/listar")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA', 'ENTRENADOR')")
    public List<HorarioEmpleadoInfoDTO> listarInfoHorariosEmpleados(Authentication authentication) {
        boolean isEntrenador = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ENTRENADOR"));
        if (isEntrenador) {
            String username = authentication.getName();
            return horarioEmpleadoService.listarHorariosPorUsuario(username);
        } else {
            return horarioEmpleadoService.listarInfoHorariosEmpleados();
        }
    }
}