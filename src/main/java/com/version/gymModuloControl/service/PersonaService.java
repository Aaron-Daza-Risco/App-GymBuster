package com.version.gymModuloControl.service;


import com.version.gymModuloControl.dto.RegistroClienteRequest;
import com.version.gymModuloControl.dto.RegistroEmpleadoRequest;
import com.version.gymModuloControl.model.*;
import com.version.gymModuloControl.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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

    @Transactional
    public ResponseEntity<?> registrarCliente(Persona persona, Usuario usuario, String direccion) {

        boolean esCliente = usuario.getUsuarioRoles().stream().anyMatch(ur -> ur.getRol().getNombre().equalsIgnoreCase("CLIENTE"));
        if (!esCliente) {
            return ResponseEntity.badRequest().body("El usuario no tiene un rol de cliente.");
        }

        persona.setUsuario(usuario);
        personaRepository.save(persona);

        Cliente cliente = new Cliente();
        cliente.setPersona(persona);
        cliente.setFechaRegistro(LocalDate.now());
        cliente.setEstado(true);
        cliente.setDireccion(direccion);
        clienteRepository.save(cliente);

        // ✉️ Enviar correo
        String correoCliente = persona.getCorreo();
        String asunto = "¡Bienvenido a GYM APP!";
        String cuerpo = "Hola " + persona.getNombre() + " " + persona.getApellidos() + ",\n\n" +
                "Te damos la bienvenida a *GYM APP* 🏋️‍♂️.\n\n" +
                "Tu cuenta ha sido creada correctamente.\n\n" +
                "👉 Usuario: " + usuario.getNombreUsuario() + "\n\n" +
                "Por seguridad, te recomendamos cambiar tu contraseña la primera vez que ingreses al sistema.\n\n" +
                "¡Ya puedes iniciar sesión y comenzar tu entrenamiento!\n\n" +
                "Gracias por confiar en nosotros 💪.";


        emailService.enviarCorreo(correoCliente, asunto, cuerpo);

        return ResponseEntity.ok("Cliente registrado correctamente.");
    }


    @Transactional
    public ResponseEntity<?> registrarEmpleado(
            Persona persona,
            Usuario usuario,
            String ruc,
            BigDecimal salario,
            LocalDate fechaContratacion,
            String tipoInstructor,
            Integer cupoMaximo
    ) {
        // Validar rol
        boolean esEntrenador = usuario.getUsuarioRoles().stream()
                .anyMatch(ur -> ur.getRol().getNombre().equalsIgnoreCase("ENTRENADOR"));
        boolean esEmpleado = usuario.getUsuarioRoles().stream()
                .anyMatch(ur -> {
                    String rol = ur.getRol().getNombre().toUpperCase();
                    return rol.equals("ADMIN") || rol.equals("RECEPCIONISTA") || rol.equals("ENTRENADOR");
                });
        if (!esEmpleado) {
            return ResponseEntity.badRequest().body("El usuario no tiene un rol de empleado válido.");
        }

        // Validar datos requeridos de persona
        if (persona.getNombre() == null || persona.getNombre().isBlank() ||
                persona.getApellidos() == null || persona.getApellidos().isBlank() ||
                persona.getGenero() == null || persona.getGenero().isBlank() ||
                persona.getCorreo() == null || persona.getCorreo().isBlank() ||
                persona.getDni() == null || persona.getDni().isBlank() ||
                persona.getCelular() == null || persona.getCelular().isBlank()
        || persona.getFechaNacimiento() == null) {
            return ResponseEntity.badRequest().body("Faltan datos requeridos de la persona.");
        }

        // Validar datos requeridos de empleado
        if (ruc == null || ruc.isBlank() ||
                salario == null ||
                fechaContratacion == null) {
            return ResponseEntity.badRequest().body("Faltan datos requeridos del empleado.");
        }

        // Validar campos exclusivos de ENTRENADOR
        if (esEntrenador) {
            if (tipoInstructor == null || tipoInstructor.isBlank() || cupoMaximo == null) {
                return ResponseEntity.badRequest().body("Faltan datos requeridos para el entrenador.");
            }
        } else {
            if (tipoInstructor != null || cupoMaximo != null) {
                return ResponseEntity.badRequest().body("Solo los entrenadores pueden tener tipoInstructor y cupoMaximo.");
            }
        }

        // Validar tipoInstructor si aplica
        TipoInstructor tipoInstructorEnum = null;
        if (esEntrenador) {
            try {
                tipoInstructorEnum = TipoInstructor.valueOf(tipoInstructor);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body("Tipo de instructor inválido.");
            }
        }

        persona.setUsuario(usuario);
        personaRepository.save(persona);

        Empleado empleado = new Empleado();
        empleado.setPersona(persona);
        empleado.setRuc(ruc);
        empleado.setSalario(salario);
        empleado.setFechaContratacion(fechaContratacion);
        empleado.setEstado(true);
        if (esEntrenador) {
            empleado.setTipoInstructor(tipoInstructorEnum);
            empleado.setCupoMaximo(cupoMaximo);
        }

        empleadoRepository.save(empleado);

        return ResponseEntity.ok("Empleado registrado correctamente.");
    }


    public List<RegistroClienteRequest> listarClientes() {
        List<Cliente> clientes = clienteRepository.findAll();
        return clientes.stream().map(cliente -> {
            Persona p = cliente.getPersona();
            RegistroClienteRequest dto = new RegistroClienteRequest();
            dto.setNombre(p.getNombre());
            dto.setApellidos(p.getApellidos());
            dto.setGenero(p.getGenero());
            dto.setCorreo(p.getCorreo());
            dto.setDni(p.getDni());
            dto.setCelular(p.getCelular());
            dto.setFechaNacimiento(p.getFechaNacimiento());
            dto.setDireccion(cliente.getDireccion());
            return dto;
        }).toList();
    }

    public List<RegistroEmpleadoRequest> listarEmpleados() {
        List<Empleado> empleados = empleadoRepository.findAll();
        return empleados.stream().map(empleado -> {
            Persona p = empleado.getPersona();
            RegistroEmpleadoRequest dto = new RegistroEmpleadoRequest();
            dto.setNombre(p.getNombre());
            dto.setApellidos(p.getApellidos());
            dto.setGenero(p.getGenero());
            dto.setCorreo(p.getCorreo());
            dto.setDni(p.getDni());
            dto.setCelular(p.getCelular());
            dto.setFechaNacimiento(p.getFechaNacimiento());
            dto.setRuc(empleado.getRuc());
            dto.setSalario(empleado.getSalario());
            dto.setFechaContratacion(empleado.getFechaContratacion());
            if (empleado.getTipoInstructor() != null) {
                dto.setTipoInstructor(empleado.getTipoInstructor().name());
                dto.setCupoMaximo(empleado.getCupoMaximo());
            }
            return dto;
        }).toList();
    }

}