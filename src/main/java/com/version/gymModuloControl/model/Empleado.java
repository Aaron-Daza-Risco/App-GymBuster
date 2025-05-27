package com.version.gymModuloControl.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "empleado")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Empleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idEmpleado;

    private Boolean estado = true;

    @Column(name = "fecha_contratacion")
    private LocalDate fechaContratacion;

    private String ruc;

    private BigDecimal salario;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_instructor")
    private TipoInstructor tipoInstructor;

    @Column(name = "cupo_maximo")
    private Integer cupoMaximo;

    @ManyToOne
    @JoinColumn(name = "persona_id", nullable = false)
    private Persona persona;

    @OneToMany(mappedBy = "empleado", cascade = CascadeType.ALL)
    private List<AsistenciaEmpleado> asistenciasEmpleado;

    @OneToMany(mappedBy = "empleado", cascade = CascadeType.ALL)
    private List<HorarioEmpleado> horarios;

    @OneToMany(mappedBy = "recepcionista", cascade = CascadeType.ALL)
    private List<Inscripcion> inscripcionesRecibidas;
}


