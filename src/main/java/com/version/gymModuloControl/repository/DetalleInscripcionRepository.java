package com.version.gymModuloControl.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.version.gymModuloControl.model.DetalleInscripcion;

public interface DetalleInscripcionRepository extends JpaRepository<DetalleInscripcion, Integer> {
    List<DetalleInscripcion> findByInscripcionIdInscripcion(Integer inscripcionId);
    List<DetalleInscripcion> findByHorarioEmpleadoEmpleadoIdEmpleado(Integer idEntrenador);
}
