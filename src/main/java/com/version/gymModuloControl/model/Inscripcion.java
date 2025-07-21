package com.version.gymModuloControl.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "inscripcion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idInscripcion;

    @Enumerated(EnumType.STRING)
    private EstadoInscripcion estado = EstadoInscripcion.ACTIVO;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_inscripcion")
    private LocalDate fechaInscripcion;

    private BigDecimal monto;

    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "recepcionista_id", nullable = true)
    private Empleado recepcionista;

    @OneToMany(mappedBy = "inscripcion", cascade = CascadeType.ALL)
    private List<DetalleInscripcion> detallesInscripcion;


    @OneToOne(mappedBy = "inscripcion", cascade = CascadeType.ALL)
    private PagoInscripcion pago;
}