// src/main/java/com/version/gymModuloControl/repository/EmpleadoRepository.java
package com.version.gymModuloControl.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.version.gymModuloControl.model.Empleado;

public interface EmpleadoRepository extends JpaRepository<Empleado, Integer> {
    List<Empleado> findByEstado(Boolean estado);
    List<Empleado> findByTipoInstructorIsNotNull();
    Empleado findByPersonaUsuarioNombreUsuario(String nombreUsuario);
    Optional<Empleado> findByPersonaIdPersona(Integer idPersona);

}
