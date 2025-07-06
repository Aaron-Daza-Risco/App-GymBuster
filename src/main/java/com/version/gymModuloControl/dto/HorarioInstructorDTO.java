package com.version.gymModuloControl.dto;

import com.version.gymModuloControl.model.Turno;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HorarioInstructorDTO {
    private Integer idHorario;
    private String dia;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private Turno turno;
}
