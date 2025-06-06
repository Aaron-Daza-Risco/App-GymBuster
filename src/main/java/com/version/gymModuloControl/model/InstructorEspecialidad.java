package com.version.gymModuloControl.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "instructor_especialidad")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InstructorEspecialidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idInstructorEspecialidad;

    @ManyToOne
    @JoinColumn(name = "empleado_id", nullable = false)
    private Empleado empleado;

    @ManyToOne
    @JoinColumn(name = "especialidad_id", nullable = false)
    private Especialidad especialidad;

    private Boolean estado = true;
}
