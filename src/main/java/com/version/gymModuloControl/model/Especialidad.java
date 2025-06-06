package com.version.gymModuloControl.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "especialidad")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Especialidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nombre;

    private String descripcion;

    private Boolean estado = true;

    @OneToMany(mappedBy = "especialidad", cascade = CascadeType.ALL)
    private List<Empleado> empleados;
}
