package com.version.gymModuloControl.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.version.gymModuloControl.model.Especialidad;

public interface EspecialidadRepository extends JpaRepository<Especialidad, Integer> {
    List<Especialidad> findByEstadoTrue();
}