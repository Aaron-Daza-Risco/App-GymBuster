package com.version.gymModuloControl.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.version.gymModuloControl.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByNombreUsuario(String nombreUsuario);
}

