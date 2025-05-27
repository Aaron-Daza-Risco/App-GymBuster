package com.version.gymModuloControl.repository;

import java.util.List;

import com.version.gymModuloControl.dto.HorarioEmpleadoInfoDTO;
import com.version.gymModuloControl.model.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;

import com.version.gymModuloControl.model.HorarioEmpleado;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HorarioEmpleadoRepository extends JpaRepository<HorarioEmpleado, Integer> {
    @Query("SELECT new com.version.gymModuloControl.dto.HorarioEmpleadoInfoDTO(" +
            "p.nombre, p.apellidos, r.nombre, h.dia, h.horaInicio, h.horaFin, h.turno) " +
            "FROM HorarioEmpleado h " +
            "JOIN h.empleado e " +
            "JOIN e.persona p " +
            "JOIN e.persona.usuario u " +
            "JOIN u.usuarioRoles ur " +
            "JOIN ur.rol r")
    List<HorarioEmpleadoInfoDTO> obtenerInfoHorariosEmpleados();





}

