package com.version.gymModuloControl.auth.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.version.gymModuloControl.auth.dto.JwtResponse;
import com.version.gymModuloControl.auth.dto.LoginRequest;
import com.version.gymModuloControl.auth.dto.RegisterRequest;
import com.version.gymModuloControl.auth.security.jwt.JwtUtils;
import com.version.gymModuloControl.model.Rol;
import com.version.gymModuloControl.model.Usuario;
import com.version.gymModuloControl.model.UsuarioRol;
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
    UsuarioRepository usuarioRepository;

    @Autowired
    UsuarioRolRepository usuarioRolRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    public JwtResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getNombreUsuario(), loginRequest.getContrasena()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return new JwtResponse(jwt, "Bearer", userDetails.getUsername(), roles);
    }

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

        // Crear usuario
        Usuario usuario = new Usuario();
        usuario.setNombreUsuario(request.getNombreUsuario());
        usuario.setContrasena(passwordEncoder.encode(request.getContrasena()));
        usuario.setEstado(true);
        usuarioRepository.save(usuario);

        // Asignar rol
        Rol rol = rolRepository.findAll().stream()
                .filter(r -> r.getNombre().equalsIgnoreCase(rolSolicitado))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        UsuarioRol usuarioRol = new UsuarioRol();
        usuarioRol.setUsuario(usuario);
        usuarioRol.setRol(rol);
        usuarioRolRepository.save(usuarioRol);

        return ResponseEntity.ok("Usuario creado correctamente con rol " + rol.getNombre());
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
            return userDTO;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(userDTOs);
    }

    public ResponseEntity<?> toggleUserStatus(Integer id, Boolean estado) {
        return usuarioRepository.findById(id)
                .map(usuario -> {
                    usuario.setEstado(estado);
                    usuarioRepository.save(usuario);

                    Map<String, Object> response = new HashMap<>();
                    response.put("message", "Estado del usuario actualizado correctamente");
                    response.put("id", usuario.getId());
                    response.put("estado", usuario.getEstado());

                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("message", "Usuario no encontrado con ID: " + id);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
                });
    }
}