package com.version.gymModuloControl.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private String tipo = "Bearer";
    private String nombreUsuario;
    private List<String> roles;
}
