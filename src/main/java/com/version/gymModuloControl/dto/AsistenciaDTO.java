package com.version.gymModuloControl.dto;

import java.time.LocalDate;

public class AsistenciaDTO {
    private LocalDate fecha;
    private Boolean estado; // true = asistió, false = no asistió

    // Getters y setters


    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Boolean getEstado() {
        return estado;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
    }
}