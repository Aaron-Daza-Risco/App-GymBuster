package com.version.gymModuloControl.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DetallesAlquilerRequest {
    private Integer alquilerId;
    private List<DetalleAlquilerDTO> detalles;
}
