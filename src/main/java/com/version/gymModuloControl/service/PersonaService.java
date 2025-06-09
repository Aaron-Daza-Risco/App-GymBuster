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
                return clienteMap;
            })
            .collect(Collectors.toList());
    }

    public List<Map<String, Object>> listarEmpleados() {
        return empleadoRepository.findByEstado(true).stream()
            .map(empleado -> {
                Map<String, Object> empleadoMap = new HashMap<>();
                Persona persona = empleado.getPersona();
                empleadoMap.put("id", empleado.getIdEmpleado());
                empleadoMap.put("nombre", persona.getNombre());
                empleadoMap.put("apellidos", persona.getApellidos());
                empleadoMap.put("dni", persona.getDni());
                empleadoMap.put("correo", persona.getCorreo());
                empleadoMap.put("celular", persona.getCelular());
                empleadoMap.put("ruc", empleado.getRuc());
                empleadoMap.put("salario", empleado.getSalario());
                empleadoMap.put("fechaContratacion", empleado.getFechaContratacion());
                empleadoMap.put("tipoInstructor", empleado.getTipoInstructor());
                empleadoMap.put("estado", empleado.getEstado());
                return empleadoMap;
            })
            .collect(Collectors.toList());
    }

    // Actualizaciones
    @Transactional
    public ResponseEntity<?> actualizarDatosPersona(Integer id, Map<String, Object> datos) {
        return personaRepository.findById(id)
            .map(persona -> {
                if (datos.containsKey("nombre")) persona.setNombre((String) datos.get("nombre"));
                if (datos.containsKey("apellidos")) persona.setApellidos((String) datos.get("apellidos"));
                if (datos.containsKey("celular")) persona.setCelular((String) datos.get("celular"));
                if (datos.containsKey("fechaNacimiento")) 
                    persona.setFechaNacimiento(LocalDate.parse((String) datos.get("fechaNacimiento")));
                
                Persona personaActualizada = personaRepository.save(persona);
                return ResponseEntity.ok(mapPersonaToDTO(personaActualizada));
            })
            .orElse(ResponseEntity.notFound().build());
    }    @Transactional
    public ResponseEntity<?> actualizarDatosCliente(Integer clienteId, Map<String, Object> datos) {
        return clienteRepository.findById(clienteId)
            .map(cliente -> {
                // Actualizar campos del cliente
                if (datos.containsKey("direccion")) {
                    cliente.setDireccion((String) datos.get("direccion"));
                }
                
                if (datos.containsKey("estado")) {
                    Boolean nuevoEstado = (Boolean) datos.get("estado");
                    cliente.setEstado(nuevoEstado);
                }
                
                // Actualizar campos de la persona asociada
                Persona persona = cliente.getPersona();
                if (datos.containsKey("nombre")) persona.setNombre((String) datos.get("nombre"));
                if (datos.containsKey("apellidos")) persona.setApellidos((String) datos.get("apellidos"));
                if (datos.containsKey("dni")) persona.setDni((String) datos.get("dni"));
                if (datos.containsKey("correo")) persona.setCorreo((String) datos.get("correo"));
                if (datos.containsKey("celular")) persona.setCelular((String) datos.get("celular"));
                if (datos.containsKey("fechaNacimiento")) 
                    persona.setFechaNacimiento(LocalDate.parse((String) datos.get("fechaNacimiento")));
                
                // Guardar los cambios
                personaRepository.save(persona);
                Cliente clienteActualizado = clienteRepository.save(cliente);

                // Preparar la respuesta
                Map<String, Object> clienteDTO = mapClienteToDTO(clienteActualizado);
                return ResponseEntity.ok(Map.of(
                    "mensaje", "Cliente actualizado correctamente",
                    "cliente", clienteDTO
                ));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @Transactional
    public ResponseEntity<?> actualizarDatosEmpleado(Integer empleadoId, Map<String, Object> datos) {
        return empleadoRepository.findById(empleadoId)
            .map(empleado -> {
                if (datos.containsKey("ruc")) empleado.setRuc((String) datos.get("ruc"));
                if (datos.containsKey("salario")) 
                    empleado.setSalario(new BigDecimal(datos.get("salario").toString()));
                if (datos.containsKey("estado")) empleado.setEstado((Boolean) datos.get("estado"));
                
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