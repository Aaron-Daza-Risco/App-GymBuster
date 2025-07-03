package com.version.gymModuloControl.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "alquiler")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Alquiler {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idAlquiler;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoAlquiler estado = EstadoAlquiler.ACTIVO;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    private BigDecimal total;

    @Column(name = "mora")
    private BigDecimal mora = BigDecimal.ZERO;

    @ManyToOne
    @JoinColumn(name = "empleado_id", nullable = false)
    private Empleado empleado;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @OneToMany(mappedBy = "alquiler", cascade = CascadeType.ALL)
    private List<DetalleAlquiler> detalles;

    @OneToOne(mappedBy = "alquiler", cascade = CascadeType.ALL)
    private PagoAlquiler pago;
}
