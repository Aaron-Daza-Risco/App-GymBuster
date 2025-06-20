package com.version.gymModuloControl.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "asistencia_empleado")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsistenciaEmpleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idAsistenciaEmpleado;

    private Boolean estado = true;

    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    private EstadoPuntualidad estadoPuntualidad;

    @Column(name = "hora_entrada")
    private LocalTime horaEntrada;

    @ManyToOne
    @JoinColumn(name = "empleado_id", nullable = false)
    private Empleado empleado;
}

