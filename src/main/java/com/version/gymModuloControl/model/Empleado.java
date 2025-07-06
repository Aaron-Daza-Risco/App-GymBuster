package com.version.gymModuloControl.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @OneToMany(mappedBy = "empleado", cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"empleado"})
    private List<InstructorEspecialidad> especialidades;


}


