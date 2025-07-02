// src/main/java/com/version/gymModuloControl/repository/EmpleadoRepository.java
package com.version.gymModuloControl.repository;

import java.util.List;
import java.util.Optional;

import com.version.gymModuloControl.model.TipoInstructor;
import org.springframework.data.jpa.repository.JpaRepository;

import com.version.gymModuloControl.model.Empleado;
import com.version.gymModuloControl.model.Persona;

public interface EmpleadoRepository extends JpaRepository<Empleado, Integer> {
    List<Empleado> findByEstado(Boolean estado);
    List<Empleado> findByTipoInstructorIsNotNull();
    Empleado findByPersonaUsuarioNombreUsuario(String nombreUsuario);
    Optional<Empleado> findByPersonaIdPersona(Integer idPersona);
    Optional<Empleado> findByPersona(Persona persona);
    List<Empleado> findByTipoInstructorAndEstadoTrueAndCupoMaximoGreaterThan(TipoInstructor tipoInstructor, int minCupo);
    List<Empleado> findByTipoInstructorAndEstadoTrue(TipoInstructor tipoInstructor);
}
