package com.version.gymModuloControl.auth.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.version.gymModuloControl.auth.dto.JwtResponse;
import com.version.gymModuloControl.auth.dto.LoginRequest;
import com.version.gymModuloControl.auth.dto.RegisterRequest;
import com.version.gymModuloControl.auth.dto.UserSecurityDetailsDTO;
import com.version.gymModuloControl.auth.service.AuthService;
import com.version.gymModuloControl.auth.dto.ChangePasswordRequest;



@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        System.out.println("Intento de login con: " + loginRequest.getNombreUsuario());
        try {
            JwtResponse jwtResponse = authService.login(loginRequest);
            return ResponseEntity.ok(jwtResponse);
        } catch (Exception e) {
            System.err.println("Error en autenticación: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PostMapping("/register")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request, Authentication authentication) {
        return authService.register(request, authentication);
    }

    @GetMapping("/usuarios")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers() {
        try {
            return authService.getAllUsers();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener la lista de usuarios: " + e.getMessage());
        }
    }

    @GetMapping("/usuarios/seguridad")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUsersSecurityDetails() {
        try {
            List<UserSecurityDetailsDTO> securityDetails = authService.getUsersSecurityDetails();
            if (securityDetails.isEmpty()) {
                // Si no hay datos, devolver una lista vacía pero con código 200 OK
                return ResponseEntity.ok(securityDetails);
            }
            return ResponseEntity.ok(securityDetails);
        } catch (Exception e) {
            System.err.println("Error al obtener detalles de seguridad: " + e.getMessage());
            e.printStackTrace(); // Para más detalle en los logs del servidor
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener detalles de seguridad de usuarios: " + e.getMessage());
        }
    }

    @PutMapping("/usuarios/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> toggleUserStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> payload) {
        try {
            Boolean nuevoEstado = payload.get("estado");

            // Validar que el estado no sea nulo
            if (nuevoEstado == null) {
                return ResponseEntity.badRequest().body("El estado no puede ser nulo");
            }

            System.out.println("Cambiando estado de usuario ID " + id + " a: " + (nuevoEstado ? "Activo" : "Inactivo"));

            // Llamar al servicio para actualizar el estado
            ResponseEntity<?> result = authService.toggleUserStatus(id.intValue(), nuevoEstado);
            return result;
        } catch (Exception e) {
            System.err.println("Error al actualizar el estado del usuario: " + e.getMessage());
            e.printStackTrace(); // Más detalles en el log
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar el estado del usuario: " + e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        try {
            return authService.getCurrentUserInfo(authentication);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener información del usuario: " + e.getMessage());
        }
    }

    @PutMapping("/usuarios/{id}/rol")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUserRole(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        try {
            return authService.updateUserRole(id.intValue(), payload.get("rol"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar el rol del usuario: " + e.getMessage());
        }
    }

    @PutMapping("/usuarios/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUserCredentials(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        try {
            String nombreUsuario = payload.get("nombreUsuario");
            String contrasena = payload.get("contrasena"); // Será null si no se envía
            return authService.updateUserCredentials(id.intValue(), nombreUsuario, contrasena);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar credenciales del usuario: " + e.getMessage());
        }
    }

    // Endpoint para obtener el perfil del cliente autenticado
    @GetMapping("/perfil")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<?> getPerfil(Authentication authentication) {
        return authService.getPerfilCliente(authentication);
    }

    // Endpoint para cambiar la contraseña del cliente
    @PostMapping("/perfil/cambiar-contrasena")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<?> cambiarContrasena(@RequestBody ChangePasswordRequest request, Authentication authentication) {
        return authService.cambiarContrasenaCliente(request, authentication);
    }
}





