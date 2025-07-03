package com.version.gymModuloControl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DetalleInscripcionDTO {
    private String instructorNombre;
    private String instructorApellido;
    private String dia;
    private String horaInicio;
    private String horaFin;
}