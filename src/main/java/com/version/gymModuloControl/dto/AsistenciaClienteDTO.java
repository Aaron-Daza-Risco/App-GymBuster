package com.version.gymModuloControl.dto;

import com.version.gymModuloControl.model.Turno;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
public class AsistenciaClienteDTO {
    private String nombre;
    private String apellido;
    private LocalDate fecha;
    private LocalTime hora;
    private Turno turno;
    private Boolean estado;
}