package com.version.gymModuloControl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class DetallesVentaRequest {
    private Integer ventaId;
    private List<DetalleVentaDTO> detalles;  // esta clase debe tener productoId y cantidad
}
