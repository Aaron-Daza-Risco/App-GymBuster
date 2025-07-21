package com.version.gymModuloControl.model;

import jakarta.persistence.*;
import lombok.*;

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

    @ManyToOne
    @JoinColumn(name = "inscripcion_id")
    private Inscripcion inscripcion;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(name = "creado_por")
    private String creadoPor;

    @Column(name = "fecha_creacion")
    private java.time.LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion")
    private java.time.LocalDateTime fechaModificacion;
}
