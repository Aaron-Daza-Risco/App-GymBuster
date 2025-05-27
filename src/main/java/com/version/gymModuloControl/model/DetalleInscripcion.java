package com.version.gymModuloControl.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "detalle_inscripcion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetalleInscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idDetalleInscripcion;

    @ManyToOne
    @JoinColumn(name = "inscripcion_id", nullable = false)
    private Inscripcion inscripcion;

    @ManyToOne
    @JoinColumn(name = "horario_empleado_id", nullable = false)
    private HorarioEmpleado horarioEmpleado;
}

