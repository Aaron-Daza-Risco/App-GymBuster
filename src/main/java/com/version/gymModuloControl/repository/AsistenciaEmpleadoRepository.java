package com.version.gymModuloControl.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.version.gymModuloControl.model.AsistenciaEmpleado;

public interface AsistenciaEmpleadoRepository extends JpaRepository<AsistenciaEmpleado, Integer> {
    List<AsistenciaEmpleado> findByEmpleadoIdEmpleado(Integer empleadoId);
}

