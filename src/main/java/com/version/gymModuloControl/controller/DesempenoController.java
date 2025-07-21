package com.version.gymModuloControl.controller;

import com.version.gymModuloControl.model.Desempeno;
import com.version.gymModuloControl.model.Cliente;
import com.version.gymModuloControl.model.Empleado;
import com.version.gymModuloControl.model.Inscripcion;
import com.version.gymModuloControl.model.EstadoInscripcion;
import com.version.gymModuloControl.service.DesempenoService;
import com.version.gymModuloControl.repository.EmpleadoRepository;
import com.version.gymModuloControl.repository.ClienteRepository;
import com.version.gymModuloControl.dto.DesempenoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;
import com.version.gymModuloControl.model.Persona;

@RestController
@RequestMapping("/api/desempeno")
public class DesempenoController {
    @Autowired
    private DesempenoService desempenoService;
    @Autowired
    private EmpleadoRepository empleadoRepository;
    @Autowired
    private ClienteRepository clienteRepository;

    // Utilidad para convertir entidad a DTO
    private DesempenoDTO toDTO(Desempeno desempeno) {
        if (desempeno == null) return null;
        DesempenoDTO dto = new DesempenoDTO();
        dto.setIdDesempeno(desempeno.getId());
        dto.setPeso(desempeno.getPeso() != null ? java.math.BigDecimal.valueOf(desempeno.getPeso()) : null);
        dto.setEstatura(desempeno.getEstatura() != null ? java.math.BigDecimal.valueOf(desempeno.getEstatura()) : null);
        dto.setImc(desempeno.getImc() != null ? java.math.BigDecimal.valueOf(desempeno.getImc()) : null);
        dto.setDiagnostico(desempeno.getDiagnostico());
        dto.setIndicador(desempeno.getIndicador());
        dto.setEdad(desempeno.getEdad());
        dto.setNivelFisico(desempeno.getNivelFisico());
        dto.setEstado(desempeno.getEstado() != null ? desempeno.getEstado().toString() : null);
        dto.setClienteId(desempeno.getCliente() != null ? desempeno.getCliente().getIdCliente() : null);
        dto.setCreadoPor(desempeno.getCreadoPor());
        dto.setFechaCreacion(desempeno.getFechaCreacion());
        dto.setFechaModificacion(desempeno.getFechaModificacion());
        return dto;
    }

    // Registrar desempeño (solo entrenador premium asignado y cliente elegible)
    @PreAuthorize("hasRole('ENTRENADOR')")
    @PostMapping("/registrar")
    public ResponseEntity<?> registrarDesempeno(@RequestBody Desempeno desempeno) {
        // Obtener usuario autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(403).body("No autenticado");
        }
        String username = authentication.getName();
        // Buscar el Empleado autenticado por la relación Persona -> Usuario -> nombreUsuario
        Empleado entrenador = empleadoRepository.findAll().stream()
            .filter(e -> {
                Persona persona = e.getPersona();
                return persona != null && persona.getUsuario() != null &&
                    username.equals(persona.getUsuario().getNombreUsuario());
            })
            .findFirst().orElse(null);
        if (entrenador == null) {
            return ResponseEntity.status(403).body("Solo un entrenador puede registrar desempeño.");
        }
        if (entrenador.getTipoInstructor() == null || !entrenador.getTipoInstructor().name().equalsIgnoreCase("PREMIUM")) {
            return ResponseEntity.status(403).body("Solo un entrenador premium puede registrar desempeño.");
        }
        Cliente cliente = desempeno.getCliente();
        if (cliente == null || cliente.getIdCliente() == null) {
            return ResponseEntity.badRequest().body("Debe especificar el cliente.");
        }
        // Cargar el cliente completo desde la base de datos
        cliente = clienteRepository.findById(cliente.getIdCliente()).orElse(null);
        if (cliente == null) {
            return ResponseEntity.badRequest().body("Cliente no encontrado.");
        }
        desempeno.setCliente(cliente);
        if (!desempenoService.esElegibleParaDesempeno(cliente)) {
            return ResponseEntity.badRequest().body("El cliente no es elegible para desempeño premium.");
        }
        // Buscar inscripción activa con plan premium
        Inscripcion inscripcionPremium = null;
        for (Inscripcion insc : cliente.getInscripciones()) {
            if (insc.getEstado() == EstadoInscripcion.ACTIVO &&
                insc.getPlan() != null &&
                insc.getPlan().getTipoPlan() != null &&
                insc.getPlan().getTipoPlan().name().equalsIgnoreCase("PREMIUM")) {
                inscripcionPremium = insc;
                break;
            }
        }
        if (inscripcionPremium == null) {
            return ResponseEntity.badRequest().body("El cliente no tiene inscripción activa en un plan premium.");
        }
        // Validar que el entrenador está asignado al cliente y al horario del plan premium
        if (!desempenoService.esEntrenadorAsignadoAlCliente(entrenador, inscripcionPremium)) {
            return ResponseEntity.status(403).body("El entrenador no está asignado a este cliente para el horario del plan premium.");
        }
        // Asociar desempeño a la inscripción activa con plan premium
        desempeno.setInscripcion(inscripcionPremium);
        try {
            Desempeno nuevo = desempenoService.registrarDesempeno(desempeno, username);
            return ResponseEntity.ok(toDTO(nuevo));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(409).body(ex.getMessage());
        }
    }

    // Actualizar desempeño (solo entrenador premium)
    @PreAuthorize("hasRole('ENTRENADOR')")
    @PutMapping("/actualizar/{id}")
    public ResponseEntity<?> actualizarDesempeno(@PathVariable Integer id, @RequestBody Desempeno desempeno) {
        // Obtener usuario autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(403).body("No autenticado");
        }
        String username = authentication.getName();
        Empleado entrenador = empleadoRepository.findAll().stream()
            .filter(e -> {
                Persona persona = e.getPersona();
                return persona != null && persona.getUsuario() != null &&
                    username.equals(persona.getUsuario().getNombreUsuario());
            })
            .findFirst().orElse(null);
        if (entrenador == null || entrenador.getTipoInstructor() == null || !entrenador.getTipoInstructor().name().equalsIgnoreCase("PREMIUM")) {
            return ResponseEntity.status(403).body("Solo un entrenador premium puede actualizar desempeño.");
        }
        Desempeno actual = desempenoService.obtenerPorId(id);
        if (actual == null) {
            return ResponseEntity.notFound().build();
        }
        Inscripcion inscripcionPremium = actual.getInscripcion();
        if (inscripcionPremium == null || !desempenoService.esEntrenadorAsignadoAlCliente(entrenador, inscripcionPremium)) {
            return ResponseEntity.status(403).body("El entrenador no está asignado a este cliente para el horario del plan premium.");
        }
        if (!desempenoService.puedeModificarDesempeno(actual, entrenador)) {
            return ResponseEntity.status(403).body("No tiene permisos para modificar este desempeño.");
        }
        // Mantener campos de auditoría originales
        desempeno.setId(id);
        desempeno.setInscripcion(actual.getInscripcion());
        desempeno.setCliente(actual.getCliente());
        desempeno.setCreadoPor(actual.getCreadoPor());
        desempeno.setFechaCreacion(actual.getFechaCreacion());
        Desempeno actualizado = desempenoService.actualizarDesempeno(desempeno, username);
        return ResponseEntity.ok(toDTO(actualizado));
    }

    // Consultar desempeño por cliente (solo entrenador premium asignado o el cliente puede ver)
    @PreAuthorize("hasAnyRole('CLIENTE','ENTRENADOR','ADMIN')")
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<?> consultarDesempenoPorCliente(@PathVariable Integer clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId).orElse(null);
        if (cliente == null) {
            return ResponseEntity.notFound().build();
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            // Si es entrenador, validar que sea premium y asignado
            if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ENTRENADOR"))) {
                Empleado entrenador = empleadoRepository.findAll().stream()
                    .filter(e -> {
                        Persona persona = e.getPersona();
                        return persona != null && persona.getUsuario() != null &&
                            username.equals(persona.getUsuario().getNombreUsuario());
                    })
                    .findFirst().orElse(null);
                if (entrenador == null || entrenador.getTipoInstructor() == null || !entrenador.getTipoInstructor().name().equalsIgnoreCase("PREMIUM")) {
                    return ResponseEntity.status(403).body("Solo un entrenador premium puede consultar desempeños de sus clientes asignados.");
                }
                boolean asignado = false;
                for (Inscripcion insc : cliente.getInscripciones()) {
                    if (insc.getEstado() == EstadoInscripcion.ACTIVO && desempenoService.esEntrenadorAsignadoAlCliente(entrenador, insc)) {
                        asignado = true;
                        break;
                    }
                }
                if (!asignado) {
                    return ResponseEntity.status(403).body("El entrenador no está asignado a este cliente para el horario del plan premium.");
                }
            }
            // Si es cliente, validar que solo pueda ver su propio desempeño
            if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"))) {
                Persona persona = null;
                // Buscar persona por username
                for (Cliente c : clienteRepository.findAll()) {
                    if (c.getPersona() != null && c.getPersona().getUsuario() != null &&
                        username.equals(c.getPersona().getUsuario().getNombreUsuario())) {
                        persona = c.getPersona();
                        break;
                    }
                }
                if (persona == null || cliente.getPersona() == null || !cliente.getPersona().getIdPersona().equals(persona.getIdPersona())) {
                    return ResponseEntity.status(403).body("Solo el cliente asociado puede consultar su propio desempeño.");
                }
            }
        }
        // Obtener solo el desempeño actual (no lista)
        List<Desempeno> desempenos = desempenoService.obtenerDesempenosPorCliente(cliente);
        DesempenoDTO dto = null;
        if (desempenos != null && !desempenos.isEmpty()) {
            dto = toDTO(desempenos.get(0));
        }
        return ResponseEntity.ok(dto); // Si no hay desempeño, retorna null
    }

    // Eliminar desempeño (solo entrenador premium asignado)
    @PreAuthorize("hasRole('ENTRENADOR')")
    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<?> eliminarDesempeno(@PathVariable Integer id) {
        // Obtener usuario autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(403).body("No autenticado");
        }
        String username = authentication.getName();
        Empleado entrenador = empleadoRepository.findAll().stream()
            .filter(e -> {
                Persona persona = e.getPersona();
                return persona != null && persona.getUsuario() != null &&
                    username.equals(persona.getUsuario().getNombreUsuario());
            })
            .findFirst().orElse(null);
        if (entrenador == null || entrenador.getTipoInstructor() == null || !entrenador.getTipoInstructor().name().equalsIgnoreCase("PREMIUM")) {
            return ResponseEntity.status(403).body("Solo un entrenador premium puede eliminar desempeño.");
        }
        Desempeno desempeno = desempenoService.obtenerPorId(id);
        if (desempeno == null) {
            return ResponseEntity.notFound().build();
        }
        Inscripcion inscripcion = desempeno.getInscripcion();
        if (inscripcion == null || !desempenoService.esEntrenadorAsignadoAlCliente(entrenador, inscripcion)) {
            return ResponseEntity.status(403).body("El entrenador no está asignado a este cliente para el horario de la inscripción del plan premium.");
        }
        desempenoService.eliminarDesempeno(id);
        return ResponseEntity.ok("Desempeño eliminado correctamente.");
    }

    // Consultar historial de desempeños por cliente (incluye inscripciones anteriores)
    @PreAuthorize("hasAnyRole('CLIENTE','ENTRENADOR','ADMIN')")
    @GetMapping("/historial/{clienteId}")
    public ResponseEntity<?> consultarHistorialDesempenosPorCliente(@PathVariable Integer clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId).orElse(null);
        if (cliente == null) {
            return ResponseEntity.notFound().build();
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            // Si es entrenador, validar que sea premium y asignado
            if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ENTRENADOR"))) {
                Empleado entrenador = empleadoRepository.findAll().stream()
                    .filter(e -> {
                        Persona persona = e.getPersona();
                        return persona != null && persona.getUsuario() != null &&
                            username.equals(persona.getUsuario().getNombreUsuario());
                    })
                    .findFirst().orElse(null);
                if (entrenador == null || entrenador.getTipoInstructor() == null || !entrenador.getTipoInstructor().name().equalsIgnoreCase("PREMIUM")) {
                    return ResponseEntity.status(403).body("Solo un entrenador premium puede consultar el historial de desempeños de sus clientes asignados.");
                }
                boolean asignado = false;
                for (Inscripcion insc : cliente.getInscripciones()) {
                    if (insc.getEstado() == EstadoInscripcion.ACTIVO && desempenoService.esEntrenadorAsignadoAlCliente(entrenador, insc)) {
                        asignado = true;
                        break;
                    }
                }
                if (!asignado) {
                    return ResponseEntity.status(403).body("El entrenador no está asignado a este cliente para el horario del plan premium.");
                }
            }
            // Si es cliente, validar que solo pueda ver su propio historial
            if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"))) {
                Persona persona = null;
                for (Cliente c : clienteRepository.findAll()) {
                    if (c.getPersona() != null && c.getPersona().getUsuario() != null &&
                        username.equals(c.getPersona().getUsuario().getNombreUsuario())) {
                        persona = c.getPersona();
                        break;
                    }
                }
                if (persona == null || cliente.getPersona() == null || !cliente.getPersona().getIdPersona().equals(persona.getIdPersona())) {
                    return ResponseEntity.status(403).body("Solo el cliente asociado puede consultar su propio historial de desempeños.");
                }
            }
        }
        List<Desempeno> desempenos = desempenoService.obtenerHistorialDesempenosPorCliente(cliente);
        List<DesempenoDTO> dtos = desempenos.stream().map(this::toDTO).toList();
        return ResponseEntity.ok(dtos);
    }
}
