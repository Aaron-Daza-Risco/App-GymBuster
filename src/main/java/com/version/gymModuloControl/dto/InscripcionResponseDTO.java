package com.version.gymModuloControl.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class InscripcionResponseDTO {
    private Integer idInscripcion;
    private String clienteNombre;
    private String planNombre;
    private String instructorNombre;
    private String recepcionistaNombre;
    private LocalDate fechaInscripcion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private BigDecimal monto;
    private List<String> horarios;  // Ejemplo: ["LUNES 08:00 - 09:00", "MIERCOLES 10:00 - 11:00"]
    private String estado;

}
