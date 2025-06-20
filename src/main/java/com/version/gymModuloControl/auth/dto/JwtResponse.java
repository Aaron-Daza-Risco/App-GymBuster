package com.version.gymModuloControl.auth.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtResponse {
    private Integer id;
    private String token;
    private String tipo = "Bearer";
    private String nombreUsuario;
    private List<String> roles;
    
}
