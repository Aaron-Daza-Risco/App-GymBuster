package com.version.gymModuloControl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@AllArgsConstructor
public class VentaConDetalleDTO {

    private Integer idVenta;

    // Datos cliente
    private String clienteNombre;
    private String clienteApellido;

    // Datos empleado (recepcionista)
    private String empleadoNombre;
    private String empleadoApellido;

    // Datos venta
    private LocalDate fecha;
    private LocalTime hora;
    private Boolean estado;
    private Double total;

    // Lista detalle venta
    private List<DetalleDTO> detalles;
}
