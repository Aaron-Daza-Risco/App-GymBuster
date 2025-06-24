package com.version.gymModuloControl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
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
    private String clienteDni;

    // Datos empleado (recepcionista)
    private String empleadoNombre;
    private String empleadoApellido;
    private String empleadoDni;

    // Datos venta
    private LocalDate fecha;
    private LocalTime hora;
    private Double total;
    private Boolean estado;

    // Datos pago
    private Integer idPago;
    private BigDecimal vuelto;
    private BigDecimal montoPagado;
    private String metodoPago;

    // Lista detalle venta
    private List<DetalleDTO> detalles;
}