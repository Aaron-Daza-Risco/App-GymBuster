package com.version.gymModuloControl.auth.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String nombreUsuario;
    private String contrasena;
}

