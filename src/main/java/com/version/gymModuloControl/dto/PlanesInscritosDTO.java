package com.version.gymModuloControl.dto;

import java.time.LocalDate;
import java.util.List;

public class PlanesInscritosDTO {
    private String nombrePlan;
    private String descripcionPlan;
    private String tipoPlan; // NUEVO: ESTANDAR o PREMIUM
    private String estadoInscripcion; // NUEVO: ACTIVO, FINALIZADO, CANCELADO

    // Para planes PREMIUM (un solo entrenador)
    private String entrenadorNombre;
    private String entrenadorApellido;

    // Para planes ESTANDAR (múltiples entrenadores)
    private List<EntrenadorPlanDTO> entrenadores; // NUEVO

    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private List<HorarioDTO> horarios;
    private List<AsistenciaDTO> asistencias;

    // Getters y setters

    public String getDescripcionPlan() {
        return descripcionPlan;
    }

    public void setDescripcionPlan(String descripcionPlan) {
        this.descripcionPlan = descripcionPlan;
    }

    public String getNombrePlan() {
        return nombrePlan;
    }

    public void setNombrePlan(String nombrePlan) {
        this.nombrePlan = nombrePlan;
    }

    public String getEntrenadorNombre() {
        return entrenadorNombre;
    }

    public void setEntrenadorNombre(String entrenadorNombre) {
        this.entrenadorNombre = entrenadorNombre;
    }

    public String getEntrenadorApellido() {
        return entrenadorApellido;
    }

    public void setEntrenadorApellido(String entrenadorApellido) {
        this.entrenadorApellido = entrenadorApellido;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public List<AsistenciaDTO> getAsistencias() {
        return asistencias;
    }

    public void setAsistencias(List<AsistenciaDTO> asistencias) {
        this.asistencias = asistencias;
    }

    public List<HorarioDTO> getHorarios() {
        return horarios;
    }

    public void setHorarios(List<HorarioDTO> horarios) {
        this.horarios = horarios;
    }

    public String getTipoPlan() {
        return tipoPlan;
    }

    public void setTipoPlan(String tipoPlan) {
        this.tipoPlan = tipoPlan;
    }

    public List<EntrenadorPlanDTO> getEntrenadores() {
        return entrenadores;
    }

    public void setEntrenadores(List<EntrenadorPlanDTO> entrenadores) {
        this.entrenadores = entrenadores;
    }

    public String getEstadoInscripcion() {
        return estadoInscripcion;
    }

    public void setEstadoInscripcion(String estadoInscripcion) {
        this.estadoInscripcion = estadoInscripcion;
    }
}
