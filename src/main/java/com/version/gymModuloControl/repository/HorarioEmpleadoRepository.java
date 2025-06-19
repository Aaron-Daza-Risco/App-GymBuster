package com.version.gymModuloControl.repository;

import java.util.List;
import com.version.gymModuloControl.dto.HorarioEmpleadoInfoDTO;
import com.version.gymModuloControl.model.HorarioEmpleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HorarioEmpleadoRepository extends JpaRepository<HorarioEmpleado, Integer> {
    @Query("SELECT new com.version.gymModuloControl.dto.HorarioEmpleadoInfoDTO(" +
            "h.idHorarioEmpleado, " +
            "p.nombre, " +
            "p.apellidos, " +
            "r.nombre, " +
            "h.dia, " +
            "h.horaInicio, " +
            "h.horaFin, " +
            "h.turno, " +
            "h.estado) " +
            "FROM HorarioEmpleado h " +
            "JOIN h.empleado e " +
            "JOIN e.persona p " +
            "JOIN e.persona.usuario u " +
            "JOIN u.usuarioRoles ur " +
            "JOIN ur.rol r ")
    List<HorarioEmpleadoInfoDTO> obtenerInfoHorariosEmpleados();

    @Query("SELECT h FROM HorarioEmpleado h " +
            "WHERE h.empleado.idEmpleado = :idEmpleado " +
            "AND h.dia = :dia " +
            "AND h.estado = true " +
            "ORDER BY h.horaInicio ASC")
    List<HorarioEmpleado> findByEmpleadoAndDia(@Param("idEmpleado") Long idEmpleado, @Param("dia") String dia);
}
