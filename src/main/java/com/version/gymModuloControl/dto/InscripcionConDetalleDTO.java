package com.version.gymModuloControl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
public class InscripcionConDetalleDTO {

    private Integer idInscripcion;

    // Datos del cliente
    private String clienteNombre;
    private String clienteApellido;
    private String clienteDni;

    // Datos del recepcionista
    private String recepcionistaNombre;
    private String recepcionistaApellido;
    private String recepcionistaDni;

    // Fechas importantes
    private LocalDate fechaInscripcion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    // Monto total
    private BigDecimal monto;

    // Datos del plan
    private String nombrePlan;
    private Integer duracionPlan; // En días o semanas, como lo manejes
    private BigDecimal precioPlan;

    // Estado de la inscripción
    private String estado;

    // Detalles de inscripción (Instructor, días, horarios, etc.)
    private List<DetalleInscripcionDTO> detalles;

    // Datos de pago
    private Integer idPago;
    private BigDecimal montoPagado;
    private BigDecimal vuelto;
    private String metodoPago;
}
