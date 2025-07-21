package com.version.gymModuloControl.dto;

import java.util.List;

public class ClienteConHorariosDTO {
    private Integer idCliente;
    private String nombre;
    private String apellido;
    private List<HorarioDTO> horarios;
    private Integer idInscripcion;

    public ClienteConHorariosDTO(Integer idCliente, String nombre, String apellido, List<HorarioDTO> horarios, Integer idInscripcion) {
        this.idCliente = idCliente;
        this.nombre = nombre;
        this.apellido = apellido;
        this.horarios = horarios;
        this.idInscripcion = idInscripcion;
    }

    public Integer getIdCliente() { return idCliente; }
    public void setIdCliente(Integer idCliente) { this.idCliente = idCliente; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public List<HorarioDTO> getHorarios() { return horarios; }
    public void setHorarios(List<HorarioDTO> horarios) { this.horarios = horarios; }
    public Integer getIdInscripcion() { return idInscripcion; }
    public void setIdInscripcion(Integer idInscripcion) { this.idInscripcion = idInscripcion; }
}
