package com.version.gymModuloControl.dto;

import java.util.List;

public class EntrenadorPlanDTO {
    private String nombre;
    private String apellido;
    private List<HorarioDTO> horarios;

    public EntrenadorPlanDTO(String nombre, String apellido, List<HorarioDTO> horarios) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.horarios = horarios;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public List<HorarioDTO> getHorarios() {
        return horarios;
    }

    public void setHorarios(List<HorarioDTO> horarios) {
        this.horarios = horarios;
    }
}

