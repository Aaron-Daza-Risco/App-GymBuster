package com.version.gymModuloControl.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.version.gymModuloControl.model.EstadoAlquiler;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlquilerConDetalleDTO {
    private Integer idAlquiler;
    private String clienteNombre;
    private String clienteApellido;
    private String clienteDni;
    private String empleadoNombre;
    private String empleadoApellido;
    private String empleadoDni;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Double total;
    private Double mora;
    private Double totalConMora;
    private EstadoAlquiler estado;
    private Integer idPago;
    private BigDecimal vuelto;
    private BigDecimal montoPagado;
    private String metodoPago;
    private List<DetalleAlquilerDTO> detalles;

}
