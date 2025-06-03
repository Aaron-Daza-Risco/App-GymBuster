package com.version.gymModuloControl.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "pieza")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pieza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idPieza;

    private String nombre;

    private Boolean estado = true;

    private Integer stock;

    private BigDecimal peso;

    @Column(name = "precio_alquiler")
    private BigDecimal precioAlquiler;

    @Column(name = "stock_minimo")
    private Integer stockMinimo;
}
