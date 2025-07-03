package com.version.gymModuloControl.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalculoAlquilerDTO {
    private Integer piezaId;
    private String piezaNombre;
    private Integer cantidad;
    private BigDecimal precioDiario;
    private Integer diasAlquiler;
    private BigDecimal subtotal; // precioDiario * cantidad * diasAlquiler
}
