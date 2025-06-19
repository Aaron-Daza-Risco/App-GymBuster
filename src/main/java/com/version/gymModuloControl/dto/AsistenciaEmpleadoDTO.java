package com.version.gymModuloControl.dto;


import com.version.gymModuloControl.model.EstadoPuntualidad;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsistenciaEmpleadoDTO {
    private Integer idAsistenciaEmpleado;
    private String nombre;
    private String apellidos;
    private LocalDate fecha;
    private LocalTime horaEntrada;
    private EstadoPuntualidad estadoPuntualidad;
}