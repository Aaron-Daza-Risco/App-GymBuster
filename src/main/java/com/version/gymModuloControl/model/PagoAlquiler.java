package com.version.gymModuloControl.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "pago_alquiler")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagoAlquiler {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idPago;

    private BigDecimal vuelto;

    @Column(name = "monto_pagado")
    private BigDecimal montoPagado;

    @Column(name = "metodo_pago")
    private String metodoPago;

    private Boolean estado = true;

    @OneToOne
    @JoinColumn(name = "alquiler_id", nullable = false)
    private Alquiler alquiler;
}
