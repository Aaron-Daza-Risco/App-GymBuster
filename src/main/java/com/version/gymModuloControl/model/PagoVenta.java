package com.version.gymModuloControl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "pago_venta")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagoVenta {

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
    @JoinColumn(name = "venta_id", nullable = false)
    private Venta venta;
}
