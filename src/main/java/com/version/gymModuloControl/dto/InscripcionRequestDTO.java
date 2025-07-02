package com.version.gymModuloControl.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class InscripcionRequestDTO {
    private Integer idCliente;
    private Integer idPlan;
    private Integer idInstructor;
    private LocalDate fechaInicio;
    private BigDecimal monto;
}
