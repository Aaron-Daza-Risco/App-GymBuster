// src/main/java/com/version/gymModuloControl/repository/EmpleadoRepository.java
package com.version.gymModuloControl.repository;

import com.version.gymModuloControl.model.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface EmpleadoRepository extends JpaRepository<Empleado, Integer> {
    List<Empleado> findByEstado(Boolean estado);
    Empleado findByPersonaUsuarioNombreUsuario(String nombreUsuario);
}