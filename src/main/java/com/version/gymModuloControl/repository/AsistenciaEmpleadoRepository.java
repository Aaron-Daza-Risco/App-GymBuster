package com.version.gymModuloControl.repository;


import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.version.gymModuloControl.model.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;

import com.version.gymModuloControl.model.AsistenciaEmpleado;

public interface AsistenciaEmpleadoRepository extends JpaRepository<AsistenciaEmpleado, Integer> {
    List<AsistenciaEmpleado> findByEmpleadoIdEmpleado(Integer empleadoId);

    boolean existsByEmpleadoAndFechaAndHoraEntradaBetween(
            Empleado empleado,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin
    );

    List<AsistenciaEmpleado> findByEstadoOrderByFechaDescHoraEntradaDesc(Boolean estado);

}

