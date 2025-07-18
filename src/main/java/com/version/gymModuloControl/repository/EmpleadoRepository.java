// src/main/java/com/version/gymModuloControl/repository/EmpleadoRepository.java
package com.version.gymModuloControl.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.version.gymModuloControl.model.Empleado;
import com.version.gymModuloControl.model.Persona;
import com.version.gymModuloControl.model.TipoInstructor;

public interface EmpleadoRepository extends JpaRepository<Empleado, Integer> {
    List<Empleado> findByEstado(Boolean estado);
    List<Empleado> findByTipoInstructorIsNotNull();
    Empleado findByPersonaUsuarioNombreUsuario(String nombreUsuario);
    Optional<Empleado> findByPersonaIdPersona(Integer idPersona);
    Optional<Empleado> findByPersona(Persona persona);
    List<Empleado> findByTipoInstructorAndEstadoTrueAndCupoMaximoGreaterThan(TipoInstructor tipoInstructor, int minCupo);
    List<Empleado> findByTipoInstructorAndEstadoTrue(TipoInstructor tipoInstructor);
    
    Long countByEstadoTrue();
    
    @Query(value = """
        SELECT 
            p.nombre as nombreEmpleado,
            p.apellidos as apellidosEmpleado,
            e.tipo_instructor as tipoInstructor,
            he.dia as diaSemana,
            he.hora_inicio as horaInicio,
            he.hora_fin as horaFin,
            CASE 
                WHEN TIME(NOW()) BETWEEN he.hora_inicio AND he.hora_fin 
                AND he.dia = CASE 
                    WHEN DAYOFWEEK(CURRENT_DATE) = 1 THEN 'DOMINGO'
                    WHEN DAYOFWEEK(CURRENT_DATE) = 2 THEN 'LUNES'
                    WHEN DAYOFWEEK(CURRENT_DATE) = 3 THEN 'MARTES'
                    WHEN DAYOFWEEK(CURRENT_DATE) = 4 THEN 'MIERCOLES'
                    WHEN DAYOFWEEK(CURRENT_DATE) = 5 THEN 'JUEVES'
                    WHEN DAYOFWEEK(CURRENT_DATE) = 6 THEN 'VIERNES'
                    WHEN DAYOFWEEK(CURRENT_DATE) = 7 THEN 'SABADO'
                END
                THEN 'Activo' 
                ELSE 'Inactivo' 
            END as estado
        FROM empleado e
        JOIN persona p ON e.persona_id = p.id_persona
        JOIN horario_empleado he ON e.id_empleado = he.empleado_id
        WHERE e.estado = true
        AND he.estado = true
        AND he.dia = CASE 
            WHEN DAYOFWEEK(CURRENT_DATE) = 1 THEN 'DOMINGO'
            WHEN DAYOFWEEK(CURRENT_DATE) = 2 THEN 'LUNES'
            WHEN DAYOFWEEK(CURRENT_DATE) = 3 THEN 'MARTES'
            WHEN DAYOFWEEK(CURRENT_DATE) = 4 THEN 'MIERCOLES'
            WHEN DAYOFWEEK(CURRENT_DATE) = 5 THEN 'JUEVES'
            WHEN DAYOFWEEK(CURRENT_DATE) = 6 THEN 'VIERNES'
            WHEN DAYOFWEEK(CURRENT_DATE) = 7 THEN 'SABADO'
        END
        ORDER BY he.hora_inicio
        """, nativeQuery = true)
    List<Map<String, Object>> getEmpleadosTrabajandonHoy();
    
    // Consulta para debugging - obtener todos los horarios
    @Query(value = """
        SELECT 
            p.nombre as nombreEmpleado,
            p.apellidos as apellidosEmpleado,
            e.tipo_instructor as tipoInstructor,
            he.dia as diaSemana,
            he.hora_inicio as horaInicio,
            he.hora_fin as horaFin,
            he.estado as estadoHorario,
            e.estado as estadoEmpleado,
            DAYOFWEEK(CURRENT_DATE) as diaActualNumero,
            CASE 
                WHEN DAYOFWEEK(CURRENT_DATE) = 1 THEN 'DOMINGO'
                WHEN DAYOFWEEK(CURRENT_DATE) = 2 THEN 'LUNES'
                WHEN DAYOFWEEK(CURRENT_DATE) = 3 THEN 'MARTES'
                WHEN DAYOFWEEK(CURRENT_DATE) = 4 THEN 'MIERCOLES'
                WHEN DAYOFWEEK(CURRENT_DATE) = 5 THEN 'JUEVES'
                WHEN DAYOFWEEK(CURRENT_DATE) = 6 THEN 'VIERNES'
                WHEN DAYOFWEEK(CURRENT_DATE) = 7 THEN 'SABADO'
            END as diaActualTexto
        FROM empleado e
        JOIN persona p ON e.persona_id = p.id_persona
        JOIN horario_empleado he ON e.id_empleado = he.empleado_id
        WHERE e.estado = true
        AND he.estado = true
        ORDER BY he.dia, he.hora_inicio
        """, nativeQuery = true)
    List<Map<String, Object>> getAllHorariosEmpleados();
}
