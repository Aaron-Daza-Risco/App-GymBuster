package com.version.gymModuloControl.dto;

import java.time.LocalTime;
import com.version.gymModuloControl.model.Turno;
import lombok.Data;

@Data
public class HorarioEmpleadoInfoDTO {

    private String nombre;
    private String apellidos;
    private String rol;
    private String dia;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private Turno turno;

    public HorarioEmpleadoInfoDTO(String nombre, String apellidos, String rol,
                                  String dia, LocalTime horaInicio, LocalTime horaFin, Turno turno) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.rol = rol;
        this.dia = dia;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.turno = turno;
    }

}