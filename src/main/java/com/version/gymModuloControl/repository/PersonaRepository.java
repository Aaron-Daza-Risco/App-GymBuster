package com.version.gymModuloControl.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.version.gymModuloControl.model.Persona;

public interface PersonaRepository extends JpaRepository<Persona, Integer> {
    Optional<Persona> findByCorreo(String correo);
    Optional<Persona> findByDni(String dni);
    List<Persona> findByNombreContainingOrApellidosContaining(String nombre, String apellidos);
}

