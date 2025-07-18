package com.version.gymModuloControl.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.version.gymModuloControl.service.PersonaService;

@RestController
@RequestMapping("/api/personas")
@CrossOrigin(origins = "*")
public class PersonaController {

    @Autowired
    private PersonaService personaService;

    @GetMapping("/clientes")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<?> listarClientes() {
        return ResponseEntity.ok(personaService.listarClientes());
    }

    @GetMapping("/empleados")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<?> listarEmpleados() {
        return ResponseEntity.ok(personaService.listarEmpleados());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<?> buscarPorId(@PathVariable Integer id) {
        return personaService.buscarPersonaPorId(id);
    }

    @GetMapping("/buscar/dni/{dni}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<?> buscarPorDni(@PathVariable String dni) {
        return personaService.buscarPorDni(dni);
    }

    @GetMapping("/buscar/correo/{correo}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<?> buscarPorCorreo(@PathVariable String correo) {
        return personaService.buscarPorCorreo(correo);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<?> actualizarPersona(@PathVariable Integer id, @RequestBody Map<String, Object> datos) {
        return personaService.actualizarDatosPersona(id, datos);
    }

    @PutMapping("/clientes/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<?> actualizarCliente(@PathVariable Integer id, @RequestBody Map<String, Object> datos) {
        return personaService.actualizarDatosCliente(id, datos);
    }


    @PutMapping("/empleados/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<?> actualizarEmpleado(@PathVariable Integer id, @RequestBody Map<String, Object> datos) {
        try {
            return personaService.actualizarDatosEmpleado(id, datos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/clientes/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<?> cambiarEstadoCliente(@PathVariable Integer id, @RequestBody Map<String, Boolean> payload) {
        try {
            return personaService.actualizarDatosCliente(id, Map.of("estado", payload.get("estado")));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/empleados/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<?> obtenerEmpleado(@PathVariable Integer id) {
        return personaService.obtenerEmpleado(id);
    }

    @GetMapping("/clientes/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<?> obtenerCliente(@PathVariable Integer id) {
        return personaService.obtenerCliente(id);
    }

    @GetMapping("/usuario/{userId}/cliente")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<?> obtenerClientePorUsuarioId(@PathVariable Integer userId) {
        return personaService.obtenerClientePorUsuarioId(userId);
    }

    @GetMapping("/usuario/{userId}/empleado")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<?> obtenerEmpleadoPorUsuarioId(@PathVariable Integer userId) {
        return personaService.obtenerEmpleadoPorUsuarioId(userId);
    }
}