package com.version.gymModuloControl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "plan")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idPlan;

    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    private Boolean estado = true;

    private BigDecimal precio;

    private String duracion; // Ej: "30 días", "6 semanas", etc.

    @JsonIgnore
    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL)
    private List<Inscripcion> inscripciones;
}


