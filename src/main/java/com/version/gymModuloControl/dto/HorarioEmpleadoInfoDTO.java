package com.version.gymModuloControl.dto;

import java.time.LocalTime;
import com.version.gymModuloControl.model.Turno;
import lombok.Data;

@Data
public class HorarioEmpleadoInfoDTO {

    private Integer idHorarioEmpleado;
    private String nombre;
    private String apellidos;
    private String rol;
    private String dia;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private Turno turno;
    private Boolean estado;

    public HorarioEmpleadoInfoDTO(Integer idHorarioEmpleado, String nombre, String apellidos, String rol,
                                  String dia, LocalTime horaInicio, LocalTime horaFin, Turno turno, Boolean estado) {
        this.idHorarioEmpleado = idHorarioEmpleado;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.rol = rol;
        this.dia = dia;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.turno = turno;
        this.estado = estado;
    }
}