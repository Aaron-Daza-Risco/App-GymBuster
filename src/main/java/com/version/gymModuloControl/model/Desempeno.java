package com.version.gymModuloControl.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "desempeno")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Desempeno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Double peso;
    private Double estatura;
    private Double imc;
    private String diagnostico;
    private String indicador;
    private Integer edad;

    @Column(name = "nivel_fisico")
    private String nivelFisico;

    private Boolean estado = true;

    @OneToMany(mappedBy = "desempeno", cascade = CascadeType.ALL)
    private List<Inscripcion> inscripciones;
}
