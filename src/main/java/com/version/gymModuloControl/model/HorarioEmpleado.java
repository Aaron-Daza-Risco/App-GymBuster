package com.version.gymModuloControl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "horario_empleado")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HorarioEmpleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idHorarioEmpleado;

    private String dia;

    private Boolean estado = true;

    @Column(name = "hora_inicio")
    private LocalTime horaInicio;

    @Column(name = "hora_fin")
    private LocalTime horaFin;

    @Enumerated(EnumType.STRING)
    private Turno turno;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "empleado_id", nullable = false)
    private Empleado empleado;

    @OneToMany(mappedBy = "horarioEmpleado", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<DetalleInscripcion> detallesInscripcion;


}


