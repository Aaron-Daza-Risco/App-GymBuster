package com.version.gymModuloControl.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.version.gymModuloControl.model.Rol;

public interface RolRepository extends JpaRepository<Rol, Integer> {
    Optional<Rol> findByNombre(String nombre);
}

