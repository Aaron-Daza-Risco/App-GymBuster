package com.version.gymModuloControl.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DesempenoDTO {
    private Integer idDesempeno;
    private BigDecimal peso;
    private BigDecimal estatura;
    private BigDecimal imc;
    private String diagnostico;
    private String indicador;
    private Integer edad;
    private String nivelFisico;
    private String estado;
    private Integer clienteId;
    private String creadoPor;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;

    public Integer getIdDesempeno() { return idDesempeno; }
    public void setIdDesempeno(Integer idDesempeno) { this.idDesempeno = idDesempeno; }
    public BigDecimal getPeso() { return peso; }
    public void setPeso(BigDecimal peso) { this.peso = peso; }
    public BigDecimal getEstatura() { return estatura; }
    public void setEstatura(BigDecimal estatura) { this.estatura = estatura; }
    public BigDecimal getImc() { return imc; }
    public void setImc(BigDecimal imc) { this.imc = imc; }
    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }
    public String getIndicador() { return indicador; }
    public void setIndicador(String indicador) { this.indicador = indicador; }
    public Integer getEdad() { return edad; }
    public void setEdad(Integer edad) { this.edad = edad; }
    public String getNivelFisico() { return nivelFisico; }
    public void setNivelFisico(String nivelFisico) { this.nivelFisico = nivelFisico; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Integer getClienteId() { return clienteId; }
    public void setClienteId(Integer clienteId) { this.clienteId = clienteId; }
    public String getCreadoPor() { return creadoPor; }
    public void setCreadoPor(String creadoPor) { this.creadoPor = creadoPor; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaModificacion() { return fechaModificacion; }
    public void setFechaModificacion(LocalDateTime fechaModificacion) { this.fechaModificacion = fechaModificacion; }
}
