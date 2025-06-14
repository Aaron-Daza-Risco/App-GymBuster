package com.version.gymModuloControl.model;

import java.util.List;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idUsuario") // Asegúrate de que coincida con el nombre de la columna en la BD
    private Integer id;

    @Column(nullable = false)
    private String contrasena;

    @Column(nullable = false)
    private Boolean estado = true;

    @Column(name = "nombre_usuario", nullable = false, length = 100)
    private String nombreUsuario;

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL)
    private Persona persona;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    private List<UsuarioRol> usuarioRoles;
}

