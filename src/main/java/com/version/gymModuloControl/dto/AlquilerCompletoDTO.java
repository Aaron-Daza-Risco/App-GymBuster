package com.version.gymModuloControl.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlquilerCompletoDTO {
    // Información del alquiler
    private Integer clienteId;
    private LocalDate fechaFin;
    
    // Detalles de alquiler
    private List<DetalleAlquilerDTO> detalles;
    
    // Información de pago
    private BigDecimal montoPagado;
    private String metodoPago;
}
