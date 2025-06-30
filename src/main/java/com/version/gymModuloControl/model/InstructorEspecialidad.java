package com.version.gymModuloControl.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    @JsonIgnoreProperties({"especialidades", "asistenciasEmpleado", "horarios", "inscripcionesRecibidas", "persona"})
    private Empleado empleado;

    @ManyToOne
    @JoinColumn(name = "especialidad_id", nullable = false)
    @JsonIgnoreProperties({"empleados"})
    private Especialidad especialidad;

    private Boolean estado = true;
}
