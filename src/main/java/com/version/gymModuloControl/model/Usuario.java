package com.version.gymModuloControl.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
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

    // Campo para registrar el último acceso del usuario
    @Column(name = "ultimo_acceso")
    private LocalDateTime ultimoAcceso;

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL)
    private Persona persona;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    private List<UsuarioRol> usuarioRoles = new ArrayList<>();

    public List<UsuarioRol> getUsuarioRoles() {
        if (usuarioRoles == null) {
            usuarioRoles = new ArrayList<>();
        }
        return usuarioRoles;
    }
}

