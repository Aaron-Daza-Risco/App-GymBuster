package com.version.gymModuloControl.auth.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSecurityDetailsDTO {
    private Integer id;
    private String nombreUsuario;
    private LocalDateTime ultimoAcceso;
    private Boolean estado;
}
