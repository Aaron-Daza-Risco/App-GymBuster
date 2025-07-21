package com.version.gymModuloControl.dto;

import java.util.List;

public class PlanConClientesDTO {
    private Integer idPlan;
    private String nombrePlan;
    private String descripcionPlan;
    private String tipoPlan;
    private List<ClienteConHorariosDTO> clientes;

    public PlanConClientesDTO(Integer idPlan, String nombrePlan, String descripcionPlan, String tipoPlan, List<ClienteConHorariosDTO> clientes) {
        this.idPlan = idPlan;
        this.nombrePlan = nombrePlan;
        this.descripcionPlan = descripcionPlan;
        this.tipoPlan = tipoPlan;
        this.clientes = clientes;
    }

    public Integer getIdPlan() { return idPlan; }
    public void setIdPlan(Integer idPlan) { this.idPlan = idPlan; }
    public String getNombrePlan() { return nombrePlan; }
    public void setNombrePlan(String nombrePlan) { this.nombrePlan = nombrePlan; }
    public String getDescripcionPlan() { return descripcionPlan; }
    public void setDescripcionPlan(String descripcionPlan) { this.descripcionPlan = descripcionPlan; }
    public String getTipoPlan() { return tipoPlan; }
    public void setTipoPlan(String tipoPlan) { this.tipoPlan = tipoPlan; }
    public List<ClienteConHorariosDTO> getClientes() { return clientes; }
    public void setClientes(List<ClienteConHorariosDTO> clientes) { this.clientes = clientes; }
}

