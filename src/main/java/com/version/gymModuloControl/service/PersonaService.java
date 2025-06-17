package com.version.gymModuloControl.service;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.version.gymModuloControl.model.Cliente;
import com.version.gymModuloControl.model.Empleado;
import com.version.gymModuloControl.model.Persona;
import com.version.gymModuloControl.model.TipoInstructor;
import com.version.gymModuloControl.repository.ClienteRepository;
import com.version.gymModuloControl.repository.EmpleadoRepository;
import com.version.gymModuloControl.repository.PersonaRepository;

@Service
public class PersonaService {

    @Autowired
    private PersonaRepository personaRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private EmpleadoRepository empleadoRepository;
    @Autowired
    private EmailService emailService;

    // Métodos de búsqueda y consulta
    public ResponseEntity<?> buscarPersonaPorId(Integer id) {
        return personaRepository.findById(id)
            .map(persona -> ResponseEntity.ok(mapPersonaToDTO(persona)))
            .orElse(ResponseEntity.notFound().build());
    }

    public ResponseEntity<?> buscarPorDni(String dni) {
        return personaRepository.findByDni(dni)
            .map(persona -> ResponseEntity.ok(mapPersonaToDTO(persona)))
            .orElse(ResponseEntity.notFound().build());
    }

    public ResponseEntity<?> buscarPorCorreo(String correo) {
        return personaRepository.findByCorreo(correo)
            .map(persona -> ResponseEntity.ok(mapPersonaToDTO(persona)))
            .orElse(ResponseEntity.notFound().build());
    }

    // Listados específicos
    public List<Map<String, Object>> listarClientes() {
        return clienteRepository.findAll().stream()
            .map(cliente -> {
                Map<String, Object> clienteMap = new HashMap<>();
                Persona persona = cliente.getPersona();
                clienteMap.put("id", cliente.getIdCliente());
                clienteMap.put("nombre", persona.getNombre());
                clienteMap.put("apellidos", persona.getApellidos());
                clienteMap.put("dni", persona.getDni());
                clienteMap.put("correo", persona.getCorreo());
                clienteMap.put("celular", persona.getCelular());
                clienteMap.put("direccion", cliente.getDireccion());
                clienteMap.put("fechaRegistro", cliente.getFechaRegistro());
                clienteMap.put("estado", cliente.getEstado());
                clienteMap.put("genero", persona.getGenero());
                clienteMap.put("fechaNacimiento", persona.getFechaNacimiento());
                return clienteMap;
            })
            .collect(Collectors.toList());
    }    public List<Map<String, Object>> listarEmpleados() {
        return empleadoRepository.findAll().stream()
            .map(empleado -> {
                Map<String, Object> empleadoMap = new HashMap<>();
                Persona persona = empleado.getPersona();
                
                if (persona != null) {
                    // Información básica del empleado
                    empleadoMap.put("idEmpleado", empleado.getIdEmpleado());
                    empleadoMap.put("estado", empleado.getEstado());
                    empleadoMap.put("ruc", empleado.getRuc());
                    empleadoMap.put("salario", empleado.getSalario());
                    empleadoMap.put("fechaContratacion", empleado.getFechaContratacion());
                    empleadoMap.put("tipoInstructor", empleado.getTipoInstructor() != null ? empleado.getTipoInstructor().toString() : null);
                    empleadoMap.put("cupoMaximo", empleado.getCupoMaximo());

                    // Información de la persona (directamente en el empleado)
                    empleadoMap.put("nombre", persona.getNombre());
                    empleadoMap.put("apellidos", persona.getApellidos());
                    empleadoMap.put("dni", persona.getDni());
                    empleadoMap.put("correo", persona.getCorreo());
                    empleadoMap.put("celular", persona.getCelular());
                    empleadoMap.put("genero", persona.getGenero());
                    empleadoMap.put("fechaNacimiento", persona.getFechaNacimiento());                    // Información de roles
                    if (persona.getUsuario() != null && persona.getUsuario().getUsuarioRoles() != null) {
                        List<String> roles = persona.getUsuario().getUsuarioRoles().stream()
                            .map(usuarioRol -> usuarioRol.getRol() != null ? usuarioRol.getRol().getNombre() : null)
                            .collect(Collectors.toList());
                        empleadoMap.put("roles", roles);
                    } else {
                        empleadoMap.put("roles", List.of());
                    }
                }
                return empleadoMap;
            })
            .collect(Collectors.toList());
    }

    // Actualizaciones    @Transactional
    public ResponseEntity<?> actualizarDatosPersona(Integer id, Map<String, Object> datos) {
        return personaRepository.findById(id)
            .map(persona -> {
                if (datos.containsKey("nombre")) persona.setNombre((String) datos.get("nombre"));
                if (datos.containsKey("apellidos")) persona.setApellidos((String) datos.get("apellidos"));
                if (datos.containsKey("celular")) persona.setCelular((String) datos.get("celular"));
                if (datos.containsKey("genero")) persona.setGenero((String) datos.get("genero"));
                if (datos.containsKey("fechaNacimiento")) 
                    persona.setFechaNacimiento(LocalDate.parse((String) datos.get("fechaNacimiento")));
                
                Persona personaActualizada = personaRepository.save(persona);
                return ResponseEntity.ok(mapPersonaToDTO(personaActualizada));
            })
            .orElse(ResponseEntity.notFound().build());
    }@Transactional
    public ResponseEntity<?> actualizarDatosCliente(Integer clienteId, Map<String, Object> datos) {
        System.out.println("Actualizando cliente ID: " + clienteId + " con datos: " + datos);
        
        return clienteRepository.findById(clienteId)
            .map(cliente -> {
                // Actualizar campos del cliente
                if (datos.containsKey("direccion")) {
                    cliente.setDireccion((String) datos.get("direccion"));
                }
                
                if (datos.containsKey("estado")) {
                    Boolean nuevoEstado = (Boolean) datos.get("estado");
                    System.out.println("Cambiando estado de cliente ID: " + clienteId + " de " + cliente.getEstado() + " a " + nuevoEstado);
                    cliente.setEstado(nuevoEstado);
                }
                
                // Actualizar campos de la persona asociada
                Persona persona = cliente.getPersona();
                if (datos.containsKey("nombre")) persona.setNombre((String) datos.get("nombre"));
                if (datos.containsKey("apellidos")) persona.setApellidos((String) datos.get("apellidos"));
                if (datos.containsKey("dni")) persona.setDni((String) datos.get("dni"));
                if (datos.containsKey("correo")) persona.setCorreo((String) datos.get("correo"));
                if (datos.containsKey("celular")) persona.setCelular((String) datos.get("celular"));
                if (datos.containsKey("genero")) persona.setGenero((String) datos.get("genero"));
                if (datos.containsKey("fechaNacimiento") && datos.get("fechaNacimiento") != null 
                    && !((String) datos.get("fechaNacimiento")).isEmpty()) {
                    try {
                        persona.setFechaNacimiento(LocalDate.parse((String) datos.get("fechaNacimiento")));
                    } catch (Exception e) {
                        // Si hay error al convertir la fecha, mantenemos el valor actual
                        System.out.println("Error al convertir fecha: " + e.getMessage());
                    }
                }
                
                // Guardar los cambios
                personaRepository.save(persona);
                Cliente clienteActualizado = clienteRepository.save(cliente);
                System.out.println("Cliente actualizado con éxito. Nuevo estado: " + clienteActualizado.getEstado());
                
                // Preparar la respuesta
                Map<String, Object> clienteDTO = mapClienteToDTO(clienteActualizado);
                
                // Agregar información de los campos actualizados para depuración
                Map<String, Object> actualizacionesRealizadas = new HashMap<>();
                if (datos.containsKey("nombre")) actualizacionesRealizadas.put("nombre", persona.getNombre());
                if (datos.containsKey("apellidos")) actualizacionesRealizadas.put("apellidos", persona.getApellidos());
                if (datos.containsKey("dni")) actualizacionesRealizadas.put("dni", persona.getDni());
                if (datos.containsKey("correo")) actualizacionesRealizadas.put("correo", persona.getCorreo());
                if (datos.containsKey("celular")) actualizacionesRealizadas.put("celular", persona.getCelular());
                if (datos.containsKey("genero")) actualizacionesRealizadas.put("genero", persona.getGenero());
                if (datos.containsKey("fechaNacimiento")) actualizacionesRealizadas.put("fechaNacimiento", persona.getFechaNacimiento());
                if (datos.containsKey("direccion")) actualizacionesRealizadas.put("direccion", cliente.getDireccion());
                if (datos.containsKey("estado")) actualizacionesRealizadas.put("estado", cliente.getEstado());
                
                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("mensaje", "Cliente actualizado correctamente");
                responseMap.put("cliente", clienteDTO);
                responseMap.put("cambiosRealizados", actualizacionesRealizadas);
                
                return ResponseEntity.ok(responseMap);
            })
            .orElse(ResponseEntity.notFound().build());
    }@Transactional
    public ResponseEntity<?> actualizarDatosEmpleado(Integer empleadoId, Map<String, Object> datos) {
        return empleadoRepository.findById(empleadoId)
            .map(empleado -> {
                // Actualizar datos del empleado
                if (datos.containsKey("ruc")) empleado.setRuc((String) datos.get("ruc"));                if (datos.containsKey("salario") && datos.get("salario") != null) {
                    try {
                        empleado.setSalario(new BigDecimal(datos.get("salario").toString()));
                    } catch (NumberFormatException e) {
                        // Si hay error al convertir, mantenemos el valor actual
                    }
                }
                if (datos.containsKey("estado")) empleado.setEstado((Boolean) datos.get("estado"));                if (datos.containsKey("tipoInstructor")) {
                    String tipoInstructorStr = (String) datos.get("tipoInstructor");
                    if (tipoInstructorStr != null && !tipoInstructorStr.isEmpty()) {
                        empleado.setTipoInstructor(TipoInstructor.valueOf(tipoInstructorStr));
                    } else {
                        empleado.setTipoInstructor(null);
                    }
                }
                if (datos.containsKey("cupoMaximo") && datos.get("cupoMaximo") != null) {
                    try {
                        if (datos.get("cupoMaximo") instanceof Integer) {
                            empleado.setCupoMaximo((Integer) datos.get("cupoMaximo"));
                        } else {
                            empleado.setCupoMaximo(Integer.valueOf(datos.get("cupoMaximo").toString()));
                        }
                    } catch (NumberFormatException e) {
                        // Si hay error al convertir, mantenemos el valor actual
                    }
                }                if (datos.containsKey("fechaContratacion") && datos.get("fechaContratacion") != null 
                    && !((String) datos.get("fechaContratacion")).isEmpty()) {
                    try {
                        empleado.setFechaContratacion(LocalDate.parse((String) datos.get("fechaContratacion")));
                    } catch (Exception e) {
                        // Si hay error al convertir la fecha, mantenemos el valor actual
                    }
                }
                  // Actualizar datos de la persona asociada
                Persona persona = empleado.getPersona();
                if (persona != null) {
                    if (datos.containsKey("nombre")) persona.setNombre((String) datos.get("nombre"));
                    if (datos.containsKey("apellidos")) persona.setApellidos((String) datos.get("apellidos"));
                    if (datos.containsKey("dni")) persona.setDni((String) datos.get("dni"));
                    if (datos.containsKey("correo")) persona.setCorreo((String) datos.get("correo"));
                    if (datos.containsKey("celular")) persona.setCelular((String) datos.get("celular"));
                    if (datos.containsKey("genero")) persona.setGenero((String) datos.get("genero"));
                    if (datos.containsKey("fechaNacimiento") && datos.get("fechaNacimiento") != null 
                        && !((String) datos.get("fechaNacimiento")).isEmpty()) {
                        try {
                            persona.setFechaNacimiento(LocalDate.parse((String) datos.get("fechaNacimiento")));
                        } catch (Exception e) {
                            // Si hay error al convertir la fecha, mantenemos el valor actual
                        }
                    }
                    
                    // Guardar los cambios en persona
                    personaRepository.save(persona);
                }
                
                Empleado empleadoActualizado = empleadoRepository.save(empleado);
                return ResponseEntity.ok(Map.of(
                    "mensaje", "Empleado actualizado correctamente",
                    "empleado", mapEmpleadoToDTO(empleadoActualizado)
                ));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    // Métodos de mapeo privados
    private Map<String, Object> mapPersonaToDTO(Persona persona) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", persona.getIdPersona());
        dto.put("nombre", persona.getNombre());
        dto.put("apellidos", persona.getApellidos());
        dto.put("dni", persona.getDni());
        dto.put("correo", persona.getCorreo());
        dto.put("celular", persona.getCelular());
        dto.put("fechaNacimiento", persona.getFechaNacimiento());
        dto.put("genero", persona.getGenero());
        return dto;
    }

    private Map<String, Object> mapClienteToDTO(Cliente cliente) {
        Map<String, Object> dto = mapPersonaToDTO(cliente.getPersona());
        dto.put("idCliente", cliente.getIdCliente());
        dto.put("direccion", cliente.getDireccion());
        dto.put("estado", cliente.getEstado());
        dto.put("fechaRegistro", cliente.getFechaRegistro());
        return dto;
    }

    private Map<String, Object> mapEmpleadoToDTO(Empleado empleado) {
        Map<String, Object> dto = mapPersonaToDTO(empleado.getPersona());
        dto.put("idEmpleado", empleado.getIdEmpleado());
        dto.put("ruc", empleado.getRuc());
        dto.put("salario", empleado.getSalario());
        dto.put("fechaContratacion", empleado.getFechaContratacion());
        dto.put("estado", empleado.getEstado());
        dto.put("tipoInstructor", empleado.getTipoInstructor());
        dto.put("cupoMaximo", empleado.getCupoMaximo());
        return dto;
    }
}