package com.version.gymModuloControl.controller;

import com.version.gymModuloControl.dto.RegistroClienteRequest;
import com.version.gymModuloControl.dto.RegistroEmpleadoRequest;
import com.version.gymModuloControl.model.Persona;
import com.version.gymModuloControl.model.Usuario;
import com.version.gymModuloControl.repository.UsuarioRepository;
import com.version.gymModuloControl.service.PersonaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import java.util.List;


@RestController
@RequestMapping("/api/persona")
public class PersonaController {

    @Autowired
    private PersonaService personaService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping("/registrar-cliente")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<?> registrarCliente(@RequestBody RegistroClienteRequest request, @RequestParam Integer usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Persona persona = new Persona();
        persona.setNombre(request.getNombre());
        persona.setApellidos(request.getApellidos());
        persona.setGenero(request.getGenero());
        persona.setCorreo(request.getCorreo());
        persona.setDni(request.getDni());
        persona.setCelular(request.getCelular());
        persona.setFechaNacimiento(request.getFechaNacimiento());

        return personaService.registrarCliente(persona, usuario, request.getDireccion());
    }

    @PostMapping("/registrar-empleado")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<?> registrarEmpleado(@RequestBody RegistroEmpleadoRequest request, @RequestParam Integer usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Persona persona = new Persona();
        persona.setNombre(request.getNombre());
        persona.setApellidos(request.getApellidos());
        persona.setGenero(request.getGenero());
        persona.setCorreo(request.getCorreo());
        persona.setDni(request.getDni());
        persona.setCelular(request.getCelular());
        persona.setFechaNacimiento(request.getFechaNacimiento());


        return personaService.registrarEmpleado(
                persona,
                usuario,
                request.getRuc(),
                request.getSalario(),
                request.getFechaContratacion(),
                request.getTipoInstructor(),
                request.getCupoMaximo()
        );
    }


    @GetMapping("/listar-clientes")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<List<RegistroClienteRequest>> listarClientes() {
        return ResponseEntity.ok(personaService.listarClientes());
    }

    @GetMapping("/listar-empleados")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<List<RegistroEmpleadoRequest>> listarEmpleados() {
        return ResponseEntity.ok(personaService.listarEmpleados());
    }


}