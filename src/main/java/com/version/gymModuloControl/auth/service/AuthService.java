package com.version.gymModuloControl.auth.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.version.gymModuloControl.auth.dto.ChangePasswordRequest;
import com.version.gymModuloControl.auth.dto.JwtResponse;
import com.version.gymModuloControl.auth.dto.LoginRequest;
import com.version.gymModuloControl.auth.dto.RegisterRequest;
import com.version.gymModuloControl.auth.dto.UserSecurityDetailsDTO;
import com.version.gymModuloControl.auth.security.jwt.JwtUtils;
import com.version.gymModuloControl.model.Cliente;
import com.version.gymModuloControl.model.Empleado;
import com.version.gymModuloControl.model.Especialidad;
import com.version.gymModuloControl.model.InstructorEspecialidad;
import com.version.gymModuloControl.model.Persona;
import com.version.gymModuloControl.model.Rol;
import com.version.gymModuloControl.model.TipoInstructor;
import com.version.gymModuloControl.model.Usuario;
import com.version.gymModuloControl.model.UsuarioRol;
import com.version.gymModuloControl.repository.ClienteRepository;
import com.version.gymModuloControl.repository.EmpleadoRepository;
import com.version.gymModuloControl.repository.EspecialidadRepository;
import com.version.gymModuloControl.repository.InstructorEspecialidadRepository;
import com.version.gymModuloControl.repository.PersonaRepository;
import com.version.gymModuloControl.repository.RolRepository;
import com.version.gymModuloControl.repository.UsuarioRepository;
import com.version.gymModuloControl.repository.UsuarioRolRepository;


@Service
public class AuthService {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    RolRepository rolRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private UsuarioRolRepository usuarioRolRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private EspecialidadRepository especialidadRepository;
    @Autowired
    private InstructorEspecialidadRepository instructorEspecialidadRepository;

    public JwtResponse login(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getNombreUsuario(), loginRequest.getContrasena()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            // Actualizar último acceso y obtener ID del usuario
            Optional<Usuario> usuarioOpt = usuarioRepository.findByNombreUsuario(loginRequest.getNombreUsuario());
            Integer userId = null;
            List<String> todosLosRoles = new ArrayList<>();
            
            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                usuario.setUltimoAcceso(LocalDateTime.now());
                usuarioRepository.save(usuario);
                userId = usuario.getId();
                
                // Verificar si el usuario tiene múltiples roles (empleado y cliente)
                List<UsuarioRol> rolesUsuario = usuarioRolRepository.findByUsuario_Id(userId);
                todosLosRoles = rolesUsuario.stream()
                                            .map(ur -> "ROLE_" + ur.getRol().getNombre())
                                            .collect(Collectors.toList());
            }

            // Logging para propósitos de depuración
            System.out.println("Usuario ID: " + userId);
            System.out.println("Roles asignados: " + roles);

            // Crear JwtResponse con información de múltiples roles
            boolean tieneMultiplesRoles = todosLosRoles.contains("ROLE_CLIENTE") && 
                                        (todosLosRoles.contains("ROLE_ENTRENADOR") || 
                                         todosLosRoles.contains("ROLE_RECEPCIONISTA") || 
                                         todosLosRoles.contains("ROLE_ADMIN"));
            
            // Logging para propósitos de depuración
            System.out.println("Usuario tiene múltiples roles: " + tieneMultiplesRoles);
            System.out.println("Todos los roles del usuario: " + todosLosRoles);
                                         
            JwtResponse response = new JwtResponse(
                userId, 
                jwt, 
                "Bearer", 
                userDetails.getUsername(), 
                roles, 
                todosLosRoles, 
                tieneMultiplesRoles
            );

            return response;
        } catch (AuthenticationException e) {
            throw e;
        }
    }

    @Transactional
    public ResponseEntity<?> register(RegisterRequest request, Authentication authentication) {
        try {
            System.out.println("======= INICIO PROCESO REGISTRO =======");
            System.out.println("Datos de registro: Usuario=" + request.getNombreUsuario() +
                    ", Rol=" + request.getRol() +
                    ", Nombre=" + request.getNombre() +
                    " " + request.getApellidos());

            // Verificar si existe el usuario
            if (usuarioRepository.findByNombreUsuario(request.getNombreUsuario()).isPresent()) {
                System.out.println("Error: El nombre de usuario ya existe: " + request.getNombreUsuario());
                return ResponseEntity.badRequest().body("Error: El nombre de usuario ya existe.");
            }

            // Validar longitud mínima de contraseña
            if (request.getContrasena().length() < 6) {
                return ResponseEntity.badRequest()
                        .body("Error: La contraseña debe tener al menos 6 caracteres.");
            }

            // 1. Crear Usuario
            Usuario usuario = new Usuario();
            usuario.setNombreUsuario(request.getNombreUsuario());

            // Encriptar contraseña
            String passwordEncriptada = passwordEncoder.encode(request.getContrasena());
            usuario.setContrasena(passwordEncriptada);
            usuario.setEstado(true);
            usuarioRepository.save(usuario);

            // 2. Asignar Rol
            String rolSolicitado = request.getRol().toUpperCase();
            Rol rol = rolRepository.findByNombre(rolSolicitado)
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

            UsuarioRol usuarioRol = new UsuarioRol();
            usuarioRol.setUsuario(usuario);
            usuarioRol.setRol(rol);
            usuarioRolRepository.save(usuarioRol);

            // 3. Crear Persona
            Persona persona = new Persona();
            persona.setNombre(request.getNombre());
            persona.setApellidos(request.getApellidos());
            persona.setGenero(request.getGenero());
            persona.setCorreo(request.getCorreo());
            persona.setDni(request.getDni());
            persona.setCelular(request.getCelular());
            persona.setFechaNacimiento(request.getFechaNacimiento());
            persona.setUsuario(usuario);
            personaRepository.save(persona);

            if (rolSolicitado.equals("CLIENTE")) {
                Cliente cliente = new Cliente();
                cliente.setPersona(persona);
                cliente.setDireccion(request.getDireccion());
                cliente.setEstado(true);
                cliente.setFechaRegistro(LocalDate.now());
                clienteRepository.save(cliente);

            } else {
                Empleado empleado = new Empleado();
                empleado.setPersona(persona);
                empleado.setRuc(request.getRuc());
                empleado.setSalario(request.getSalario());
                empleado.setFechaContratacion(request.getFechaContratacion());
                empleado.setEstado(true);

                if (rolSolicitado.equals("ENTRENADOR")) {
                    empleado.setTipoInstructor(TipoInstructor.valueOf(request.getTipoInstructor()));
                    empleado.setCupoMaximo(request.getCupoMaximo());
                }

                empleado = empleadoRepository.save(empleado);

                // Crear automáticamente una cuenta de cliente para el empleado
                Cliente clienteEmpleado = new Cliente();
                clienteEmpleado.setPersona(persona);
                clienteEmpleado.setDireccion(request.getDireccion() != null ? request.getDireccion() : "");
                clienteEmpleado.setEstado(true);
                clienteEmpleado.setFechaRegistro(LocalDate.now());
                clienteRepository.save(clienteEmpleado);

                // Asignar también el rol de cliente al usuario
                Rol rolCliente = rolRepository.findByNombre("CLIENTE")
                        .orElseThrow(() -> new RuntimeException("Rol CLIENTE no encontrado"));

                UsuarioRol usuarioRolCliente = new UsuarioRol();
                usuarioRolCliente.setUsuario(usuario);
                usuarioRolCliente.setRol(rolCliente);
                usuarioRolRepository.save(usuarioRolCliente);

                System.out.println("Cuenta de cliente creada automáticamente para el empleado: " +
                        persona.getNombre() + " " + persona.getApellidos());

                // Si es entrenador y se proporcionaron especialidades, asignarlas
                if (rolSolicitado.equals("ENTRENADOR") && request.getEspecialidadesIds() != null) {
                    System.out.println("Intentando asignar especialidades al entrenador ID: " + empleado.getIdEmpleado());
                    System.out.println("Especialidades a asignar: " + request.getEspecialidadesIds());

                    try {
                        entityManager.flush();
                        List<Especialidad> especialidades = especialidadRepository.findAllById(request.getEspecialidadesIds());
                        System.out.println("Especialidades encontradas: " + especialidades.size());

                        for (Especialidad especialidad : especialidades) {
                            InstructorEspecialidad instructorEspecialidad = new InstructorEspecialidad();
                            instructorEspecialidad.setEmpleado(empleado);
                            instructorEspecialidad.setEspecialidad(especialidad);
                            instructorEspecialidad.setEstado(true);
                            instructorEspecialidadRepository.save(instructorEspecialidad);
                            System.out.println("Asignada especialidad ID: " + especialidad.getId() +
                                    " al empleado ID: " + empleado.getIdEmpleado());
                        }
                        entityManager.flush();
                    } catch (Exception e) {
                        System.err.println("Error al asignar especialidades: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Usuario registrado exitosamente");
            response.put("usuarioId", usuario.getId());

            System.out.println("======= FIN PROCESO REGISTRO EXITOSO =======");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("======= ERROR EN PROCESO REGISTRO =======");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al registrar usuario: " + e.getMessage());
        }
    }

    // Método para obtener detalles de seguridad de todos los usuarios
    public List<UserSecurityDetailsDTO> getUsersSecurityDetails() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        List<UserSecurityDetailsDTO> securityDetails = new ArrayList<>();

        for (Usuario usuario : usuarios) {
            UserSecurityDetailsDTO dto = new UserSecurityDetailsDTO();
            dto.setId(usuario.getId());
            dto.setNombreUsuario(usuario.getNombreUsuario());
            dto.setUltimoAcceso(usuario.getUltimoAcceso());
            dto.setEstado(usuario.getEstado());
            securityDetails.add(dto);
        }

        return securityDetails;
    }

    public ResponseEntity<?> getAllUsers() {
        List<Usuario> usuarios = usuarioRepository.findAll();

        // Transformar la lista de usuarios a DTOs
        List<Map<String, Object>> userDTOs = usuarios.stream().map(usuario -> {
            Map<String, Object> userDTO = new HashMap<>();
            userDTO.put("id", usuario.getId());
            userDTO.put("nombreUsuario", usuario.getNombreUsuario());
            userDTO.put("estado", usuario.getEstado());

            // Obtener los roles del usuario
            List<String> roles = usuario.getUsuarioRoles().stream()
                    .map(usuarioRol -> usuarioRol.getRol().getNombre())
                    .collect(Collectors.toList());

            userDTO.put("roles", roles);

            // Añadir información de la persona (incluyendo el DNI)
            if (usuario.getPersona() != null) {
                userDTO.put("nombre", usuario.getPersona().getNombre());
                userDTO.put("apellidos", usuario.getPersona().getApellidos());
                userDTO.put("dni", usuario.getPersona().getDni());
                userDTO.put("correo", usuario.getPersona().getCorreo());

                // Buscar si es empleado o cliente y agregar el ID correspondiente
                Optional<Empleado> empleado = empleadoRepository.findByPersona(usuario.getPersona());
                if (empleado.isPresent()) {
                    userDTO.put("idEmpleado", empleado.get().getIdEmpleado());
                } else {
                    Optional<Cliente> cliente = clienteRepository.findByPersona(usuario.getPersona());
                    if (cliente.isPresent()) {
                        userDTO.put("idCliente", cliente.get().getIdCliente());
                    }
                }
            }

            return userDTO;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(userDTOs);
    }

    public ResponseEntity<?> toggleUserStatus(Integer id, Boolean estado) {
        try {
            System.out.println("Servicio toggleUserStatus - ID: " + id + ", Nuevo estado: " + estado);

            return usuarioRepository.findById(id)
                    .map(usuario -> {
                        // Guardar el estado anterior para logging
                        Boolean estadoAnterior = usuario.getEstado();

                        // Actualizar estado
                        usuario.setEstado(estado);
                        Usuario usuarioActualizado = usuarioRepository.save(usuario);

                        System.out.println("Usuario actualizado - ID: " + id + ", Estado anterior: "
                                + estadoAnterior + ", Nuevo estado: " + usuarioActualizado.getEstado());

                        Map<String, Object> response = new HashMap<>();
                        response.put("message", "Estado del usuario actualizado correctamente");
                        response.put("id", usuarioActualizado.getId());
                        response.put("estado", usuarioActualizado.getEstado());

                        return ResponseEntity.ok(response);
                    })
                    .orElseGet(() -> {
                        System.err.println("Usuario no encontrado con ID: " + id);
                        Map<String, Object> errorResponse = new HashMap<>();
                        errorResponse.put("message", "Usuario no encontrado con ID: " + id);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
                    });
        } catch (Exception e) {
            System.err.println("Error inesperado al actualizar estado de usuario: " + e.getMessage());
            e.printStackTrace(); // Para obtener más detalles en el log
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Error al actualizar estado: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Transactional
    public ResponseEntity<?> asignarEspecialidades(Integer empleadoId, List<Integer> especialidadesIds) {
        try {
            System.out.println("Iniciando asignación de especialidades para empleado ID: " + empleadoId);
            System.out.println("Especialidades IDs a asignar: " + especialidadesIds);
            
            Empleado empleado = empleadoRepository.findById(empleadoId)
                    .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));
    
            // Verificar que el empleado sea un entrenador
            boolean esEntrenador = empleado.getPersona().getUsuario().getUsuarioRoles().stream()
                    .anyMatch(ur -> ur.getRol().getNombre().equals("ENTRENADOR"));
    
            if (!esEntrenador) {
                System.out.println("Error: El empleado ID " + empleadoId + " no es un entrenador");
                return ResponseEntity.badRequest()
                        .body("Solo se pueden asignar especialidades a entrenadores");
            }
    
            // Validar que hay especialidades para asignar
            if (especialidadesIds == null || especialidadesIds.isEmpty()) {
                System.out.println("Error: No se proporcionaron especialidades para asignar");
                return ResponseEntity.badRequest().body("Debe proporcionar al menos una especialidad para asignar");
            }
            
            // Asegurar que todas las transacciones anteriores estén confirmadas
            entityManager.flush();
            
            // Eliminar especialidades existentes de forma segura (primera query JPQL, luego eliminar entidades)
            List<InstructorEspecialidad> especialidadesExistentes = instructorEspecialidadRepository.findAll().stream()
                .filter(ie -> ie.getEmpleado().getIdEmpleado().equals(empleadoId))
                .toList();
                
            if (!especialidadesExistentes.isEmpty()) {
                System.out.println("Eliminando " + especialidadesExistentes.size() + " especialidades existentes");
                instructorEspecialidadRepository.deleteAll(especialidadesExistentes);
                entityManager.flush(); // Confirmar la eliminación antes de insertar nuevas
            } else {
                System.out.println("No se encontraron especialidades existentes para este empleado");
            }
    
            // Asignar nuevas especialidades
            List<Especialidad> especialidades = especialidadRepository.findAllById(especialidadesIds);
            
            if (especialidades.isEmpty()) {
                System.out.println("Error: Las especialidades con IDs " + especialidadesIds + " no existen");
                return ResponseEntity.badRequest().body("Las especialidades proporcionadas no existen");
            }
            
            System.out.println("Encontradas " + especialidades.size() + " especialidades para asignar");
    
            // Guardar las nuevas relaciones en la base de datos una por una
            int asignadas = 0;
            for (Especialidad especialidad : especialidades) {
                try {
                    InstructorEspecialidad instructorEspecialidad = new InstructorEspecialidad();
                    instructorEspecialidad.setEmpleado(empleado);
                    instructorEspecialidad.setEspecialidad(especialidad);
                    instructorEspecialidad.setEstado(true);
                    
                    InstructorEspecialidad saved = instructorEspecialidadRepository.save(instructorEspecialidad);
                    entityManager.flush(); // Confirmar cada inserción
                    
                    if (saved != null && saved.getIdInstructorEspecialidad() != null) {
                        System.out.println("Asignada especialidad [" + especialidad.getId() + "] " + 
                                         especialidad.getNombre() + " al empleado " + empleado.getIdEmpleado());
                        asignadas++;
                    } else {
                        System.out.println("Advertencia: No se confirmó el guardado de la especialidad " + 
                                         especialidad.getId());
                    }
                } catch (Exception e) {
                    System.err.println("Error al asignar especialidad " + especialidad.getId() + ": " + e.getMessage());
                }
            }
            
            System.out.println("Asignación completada. Total asignadas: " + asignadas + " de " + especialidades.size());
    
            return ResponseEntity.ok("Especialidades asignadas correctamente: " + asignadas);
        } catch (Exception e) {
            System.err.println("Error grave al asignar especialidades: " + e.getMessage());
            e.printStackTrace(); // Solo para debug
            return ResponseEntity.status(500).body("Error al asignar especialidades: " + e.getMessage());
        }
    }

    public ResponseEntity<?> getCurrentUserInfo(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Usuario no autenticado");
        }

        String username = authentication.getName();
        Optional<Usuario> usuarioOpt = usuarioRepository.findByNombreUsuario(username);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Usuario no encontrado");
        }

        Usuario usuario = usuarioOpt.get();
        Persona persona = usuario.getPersona();

        if (persona == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Información personal no encontrada");
        }

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", usuario.getId());
        userInfo.put("nombreUsuario", usuario.getNombreUsuario());
        userInfo.put("estado", usuario.getEstado());
        userInfo.put("roles", usuario.getUsuarioRoles().stream()
                .map(ur -> ur.getRol().getNombre())
                .collect(Collectors.toList()));

        // Información personal
        userInfo.put("nombre", persona.getNombre());
        userInfo.put("apellidos", persona.getApellidos());
        userInfo.put("correo", persona.getCorreo());
        userInfo.put("dni", persona.getDni());
        userInfo.put("celular", persona.getCelular());
        userInfo.put("fechaNacimiento", persona.getFechaNacimiento());
        userInfo.put("genero", persona.getGenero());

        return ResponseEntity.ok(userInfo);
    }

    @Transactional
    public ResponseEntity<?> updateUserRole(int userId, String rolNombre) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(userId);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }

        Usuario usuario = usuarioOpt.get();
        Optional<Rol> rolOpt = rolRepository.findByNombre(rolNombre);
        if (rolOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Rol no válido");
        }

        List<UsuarioRol> rolesActuales = usuarioRolRepository.findByUsuario_Id(userId);
        boolean tieneRolCliente = rolesActuales.stream().anyMatch(ur -> ur.getRol().getNombre().equals("CLIENTE"));

        // Si se cambia a CLIENTE, eliminar solo los roles de empleado (no el registro de empleado)
        if (rolNombre.equals("CLIENTE")) {
            List<UsuarioRol> rolesEmpleado = rolesActuales.stream()
                    .filter(ur -> ur.getRol().getNombre().equals("ADMIN") ||
                            ur.getRol().getNombre().equals("RECEPCIONISTA") ||
                            ur.getRol().getNombre().equals("ENTRENADOR"))
                    .collect(Collectors.toList());
            usuario.getUsuarioRoles().removeAll(rolesEmpleado);
            usuarioRolRepository.deleteAll(rolesEmpleado);

            // No eliminar el registro de empleado

            // Si no tiene rol cliente, asignarlo
            if (!tieneRolCliente) {
                UsuarioRol nuevoRol = new UsuarioRol();
                nuevoRol.setUsuario(usuario);
                nuevoRol.setRol(rolOpt.get());
                usuarioRolRepository.save(nuevoRol);
                usuario.getUsuarioRoles().add(nuevoRol);
            }

            usuarioRepository.save(usuario);
            entityManager.flush();
            entityManager.clear();

            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Ahora el usuario es solo CLIENTE");
            response.put("nuevoRol", rolNombre);
            return ResponseEntity.ok(response);
        }

        // Si era cliente y se le asigna rol de empleado
        if (tieneRolCliente && (rolNombre.equals("RECEPCIONISTA") || rolNombre.equals("ADMIN") || rolNombre.equals("ENTRENADOR"))) {
            boolean yaTieneRol = rolesActuales.stream().anyMatch(ur -> ur.getRol().getNombre().equals(rolNombre));
            if (!yaTieneRol) {
                UsuarioRol nuevoRol = new UsuarioRol();
                nuevoRol.setUsuario(usuario);
                nuevoRol.setRol(rolOpt.get());
                usuarioRolRepository.save(nuevoRol);
                usuario.getUsuarioRoles().add(nuevoRol);
            }

            // Crear registro de empleado si no existe
            Optional<Empleado> empleadoExistente = empleadoRepository.findByPersonaIdPersona(usuario.getPersona().getIdPersona());
            if (empleadoExistente.isEmpty()) {
                Empleado nuevoEmpleado = new Empleado();
                nuevoEmpleado.setPersona(usuario.getPersona());
                nuevoEmpleado.setEstado(true);
                nuevoEmpleado.setFechaContratacion(LocalDate.now());
                empleadoRepository.save(nuevoEmpleado);
            }

            usuarioRepository.save(usuario);
            entityManager.flush();
            entityManager.clear();

            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Rol de empleado asignado correctamente");
            response.put("nuevoRol", rolNombre);
            return ResponseEntity.ok(response);
        }

        // Otros cambios de rol (por ejemplo entre roles de empleado)
        List<UsuarioRol> rolesAEliminar = rolesActuales.stream()
                .filter(ur -> ur.getRol().getNombre().equals("ADMIN") ||
                        ur.getRol().getNombre().equals("RECEPCIONISTA") ||
                        ur.getRol().getNombre().equals("ENTRENADOR"))
                .filter(ur -> !ur.getRol().getNombre().equals(rolNombre))
                .collect(Collectors.toList());
        usuario.getUsuarioRoles().removeAll(rolesAEliminar);
        usuarioRolRepository.deleteAll(rolesAEliminar);

        boolean yaTieneNuevoRol = rolesActuales.stream().anyMatch(ur -> ur.getRol().getNombre().equals(rolNombre));
        if (!yaTieneNuevoRol) {
            UsuarioRol nuevoRol = new UsuarioRol();
            nuevoRol.setUsuario(usuario);
            nuevoRol.setRol(rolOpt.get());
            usuarioRolRepository.save(nuevoRol);
            usuario.getUsuarioRoles().add(nuevoRol);
        }

        usuarioRepository.save(usuario);
        entityManager.flush();
        entityManager.clear();

        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Rol actualizado correctamente");
        response.put("nuevoRol", rolNombre);
        return ResponseEntity.ok(response);
    }

    @Transactional
    public ResponseEntity<?> updateUserCredentials(int userId, String nombreUsuario, String contrasena) {
        try {
            // Validar que el ID sea válido
            Optional<Usuario> usuarioOpt = usuarioRepository.findById(userId);
            if (!usuarioOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No se encontró el usuario con ID: " + userId);
            }

            Usuario usuario = usuarioOpt.get();

            // Validar que el nuevo nombre de usuario no exista ya (si es diferente al actual)
            if (!usuario.getNombreUsuario().equals(nombreUsuario)) {
                Optional<Usuario> existingUsuario = usuarioRepository.findByNombreUsuario(nombreUsuario);
                if (existingUsuario.isPresent()) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body("El nombre de usuario ya está en uso: " + nombreUsuario);
                }
                usuario.setNombreUsuario(nombreUsuario);
            }

            // Actualizar la contraseña solo si se proporciona una nueva
            if (contrasena != null && !contrasena.isEmpty()) {
                usuario.setContrasena(passwordEncoder.encode(contrasena));
            }

            // Guardar los cambios
            usuarioRepository.save(usuario);

            return ResponseEntity.ok("Usuario actualizado correctamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar credenciales: " + e.getMessage());
        }
    }

    public ResponseEntity<?> getPerfilCliente(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autenticado");
        }
        String username = authentication.getName();
        Optional<Usuario> usuarioOpt = usuarioRepository.findByNombreUsuario(username);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }
        Usuario usuario = usuarioOpt.get();
        Persona persona = usuario.getPersona();
        if (persona == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Datos personales no encontrados");
        }
        Map<String, Object> perfil = new HashMap<>();
        perfil.put("nombreUsuario", usuario.getNombreUsuario());
        perfil.put("nombre", persona.getNombre());
        perfil.put("apellidos", persona.getApellidos());
        perfil.put("correo", persona.getCorreo());
        perfil.put("dni", persona.getDni());
        perfil.put("celular", persona.getCelular());
        perfil.put("fechaNacimiento", persona.getFechaNacimiento());
        perfil.put("genero", persona.getGenero());
        // Calcular edad
        if (persona.getFechaNacimiento() != null) {
            int edad = LocalDate.now().getYear() - persona.getFechaNacimiento().getYear();
            perfil.put("edad", edad);
        }
        return ResponseEntity.ok(perfil);
    }

    public ResponseEntity<?> cambiarContrasenaCliente(ChangePasswordRequest request, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autenticado");
        }
        String username = authentication.getName();
        Optional<Usuario> usuarioOpt = usuarioRepository.findByNombreUsuario(username);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }
        Usuario usuario = usuarioOpt.get();

        // Verificar contraseña actual
        if (!passwordEncoder.matches(request.getContrasenaActual(), usuario.getContrasena())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("La contraseña actual es incorrecta");
        }

        // Validar nueva contraseña
        String nueva = request.getNuevaContrasena();
        if (nueva == null || nueva.length() < 6) {
            return ResponseEntity.badRequest().body("La nueva contraseña debe tener al menos 6 caracteres");
        }
        if (passwordEncoder.matches(nueva, usuario.getContrasena())) {
            return ResponseEntity.badRequest().body("La nueva contraseña no puede ser igual a la actual");
        }

        // Actualizar y guardar
        usuario.setContrasena(passwordEncoder.encode(nueva));
        usuarioRepository.save(usuario);

        return ResponseEntity.ok("Contraseña actualizada correctamente");
    }

    public ResponseEntity<?> getUserDetails(Integer id) {
        try {
            return usuarioRepository.findById(id)
                    .map(usuario -> {
                        Map<String, Object> userDetails = new HashMap<>();
                        userDetails.put("id", usuario.getId());
                        userDetails.put("nombreUsuario", usuario.getNombreUsuario());
                        userDetails.put("estado", usuario.getEstado());
                        userDetails.put("ultimoAcceso", usuario.getUltimoAcceso());

                        // Obtener los roles del usuario
                        List<String> roles = usuario.getUsuarioRoles().stream()
                                .map(usuarioRol -> usuarioRol.getRol().getNombre())
                                .collect(Collectors.toList());
                        userDetails.put("roles", roles);

                        // Añadir información de la persona
                        if (usuario.getPersona() != null) {
                            Persona persona = usuario.getPersona();
                            userDetails.put("nombre", persona.getNombre());
                            userDetails.put("apellidos", persona.getApellidos());
                            userDetails.put("dni", persona.getDni());
                            userDetails.put("correo", persona.getCorreo());
                            userDetails.put("genero", persona.getGenero());
                            userDetails.put("celular", persona.getCelular());
                            userDetails.put("fechaNacimiento", persona.getFechaNacimiento());

                            // Buscar si la persona es un empleado
                            Optional<Empleado> empleado = empleadoRepository.findByPersonaIdPersona(persona.getIdPersona());
                            if (empleado.isPresent()) {
                                userDetails.put("ruc", empleado.get().getRuc());
                                userDetails.put("salario", empleado.get().getSalario());
                                userDetails.put("fechaContratacion", empleado.get().getFechaContratacion());
                                userDetails.put("tipoInstructor", empleado.get().getTipoInstructor());
                            } else {
                                // Si no es empleado, buscar si es cliente
                                Optional<Cliente> cliente = clienteRepository.findByPersona(persona);
                                if (cliente.isPresent()) {
                                    userDetails.put("direccion", cliente.get().getDireccion());
                                    userDetails.put("fechaRegistro", cliente.get().getFechaRegistro());
                                }
                            }
                        }

                        return ResponseEntity.ok(userDetails);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            System.err.println("Error al obtener detalles del usuario: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener detalles del usuario: " + e.getMessage());
        }
    }

    public boolean isUsuarioActivo(String nombreUsuario) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByNombreUsuario(nombreUsuario);
        return usuarioOpt.isPresent() && Boolean.TRUE.equals(usuarioOpt.get().getEstado());
    }
}

