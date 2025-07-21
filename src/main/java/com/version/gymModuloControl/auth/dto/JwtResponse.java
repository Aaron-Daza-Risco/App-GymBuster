package com.version.gymModuloControl.auth.dto;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class JwtResponse {
    private Integer id;
    private String token;
    private String tipo = "Bearer";
    private String nombreUsuario;
    private List<String> roles;
    private List<String> todosLosRoles;
    private boolean tieneMultiplesRoles;

    public JwtResponse(Integer id, String token, String tipo, String nombreUsuario, List<String> roles) {
        this.id = id;
        this.token = token;
        this.tipo = tipo;
        this.nombreUsuario = nombreUsuario;
        this.roles = roles;
        this.todosLosRoles = roles;
        this.tieneMultiplesRoles = false;
    }
    
    public JwtResponse(Integer id, String token, String tipo, String nombreUsuario, 
                      List<String> roles, List<String> todosLosRoles, boolean tieneMultiplesRoles) {
        this.id = id;
        this.token = token;
        this.tipo = tipo;
        this.nombreUsuario = nombreUsuario;
        this.roles = roles;
        this.todosLosRoles = todosLosRoles;
        this.tieneMultiplesRoles = tieneMultiplesRoles;
    }
}
