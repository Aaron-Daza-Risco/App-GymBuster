package com.version.gymModuloControl.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "cliente")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCliente;

    private String direccion;

    private Boolean estado = true;

    @Column(name = "fecha_registro")
    private LocalDate fechaRegistro;

    @ManyToOne
    @JoinColumn(name = "persona_id", nullable = false)
    private Persona persona;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
    private List<Asistencia> asistencias;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
    private List<Inscripcion> inscripciones;
}
