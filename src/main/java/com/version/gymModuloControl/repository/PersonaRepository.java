package com.version.gymModuloControl.repository;

import com.version.gymModuloControl.model.Persona;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PersonaRepository extends JpaRepository<Persona, Integer> {
    Optional<Persona> findByCorreo(String correo);
    Optional<Persona> findByDni(String dni);
}

