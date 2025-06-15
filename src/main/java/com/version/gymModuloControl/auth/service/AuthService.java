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

            // Actualizar último acceso
            Optional<Usuario> usuarioOpt = usuarioRepository.findByNombreUsuario(loginRequest.getNombreUsuario());
            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                usuario.setUltimoAcceso(LocalDateTime.now());
                usuarioRepository.save(usuario);
            }

            return new JwtResponse(jwt, "Bearer", userDetails.getUsername(), roles);
        } catch (AuthenticationException e) {
            throw e;
        }
    }

    @Transactional
    public ResponseEntity<?> register(RegisterRequest request, Authentication authentication) {
        // Verificar si existe el usuario
        if (usuarioRepository.findByNombreUsuario(request.getNombreUsuario()).isPresent()) {
            return ResponseEntity.badRequest().body("Error: El nombre de usuario ya existe.");
        }

        // Obtener rol del usuario autenticado
        String rolActual = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("")
                .replace("ROLE_", "");

        // Validar si el rol solicitado puede ser creado por el rol actual
        String rolSolicitado = request.getRol().toUpperCase();

        if (rolActual.equals("ADMIN")) {
            if (!List.of("ENTRENADOR", "RECEPCIONISTA", "CLIENTE").contains(rolSolicitado)) {
                return ResponseEntity.badRequest()
                        .body("Error: Un administrador solo puede crear entrenadores, recepcionistas o clientes.");
            }
        } else if (rolActual.equals("RECEPCIONISTA")) {
            if (!List.of("CLIENTE", "ENTRENADOR").contains(rolSolicitado)) {
                return ResponseEntity.badRequest()
                        .body("Error: Una recepcionista solo puede crear clientes o entrenadores.");
            }
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No autorizado para crear usuarios.");
        }

        // Evaluar longitud mínima de contraseña
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

        // 4. Crear Cliente o Empleado según el rol
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

            // Si es entrenador y se proporcionaron especialidades, asignarlas
            if (rolSolicitado.equals("ENTRENADOR") && request.getEspecialidadesIds() != null) {
                asignarEspecialidades(empleado.getIdEmpleado(), request.getEspecialidadesIds());
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Usuario registrado exitosamente");
        response.put("usuarioId", usuario.getId());
        
        return ResponseEntity.ok(response);
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
        Empleado empleado = empleadoRepository.findById(empleadoId)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

        // Verificar que el empleado sea un entrenador
        boolean esEntrenador = empleado.getPersona().getUsuario().getUsuarioRoles().stream()
                .anyMatch(ur -> ur.getRol().getNombre().equals("ENTRENADOR"));

        if (!esEntrenador) {
            return ResponseEntity.badRequest()
                    .body("Solo se pueden asignar especialidades a entrenadores");
        }

        // Eliminar especialidades existentes
        instructorEspecialidadRepository.deleteByEmpleado(empleado);

        // Asignar nuevas especialidades
        List<Especialidad> especialidades = especialidadRepository.findAllById(especialidadesIds);
        
        for (Especialidad especialidad : especialidades) {
            InstructorEspecialidad instructorEspecialidad = new InstructorEspecialidad();
            instructorEspecialidad.setEmpleado(empleado);
            instructorEspecialidad.setEspecialidad(especialidad);
            instructorEspecialidad.setEstado(true);
            instructorEspecialidadRepository.save(instructorEspecialidad);
        }

        return ResponseEntity.ok("Especialidades asignadas correctamente");
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
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Usuario no encontrado");
        }

        Usuario usuario = usuarioOpt.get();
        Optional<Rol> rolOpt = rolRepository.findByNombre(rolNombre);
        
        if (rolOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Rol no válido");
        }

        // Limpiar roles existentes
        List<UsuarioRol> rolesActuales = usuarioRolRepository.findByUsuario_Id(userId);
        usuarioRolRepository.deleteAll(rolesActuales);

        // Asignar nuevo rol
        UsuarioRol nuevoUsuarioRol = new UsuarioRol();
        nuevoUsuarioRol.setUsuario(usuario);
        nuevoUsuarioRol.setRol(rolOpt.get());
        usuarioRolRepository.save(nuevoUsuarioRol);

        return ResponseEntity.ok()
                .body(Map.of(
                    "mensaje", "Rol actualizado correctamente",
                    "nuevoRol", rolNombre
                ));
    }
}