package com.version.gymModuloControl.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.version.gymModuloControl.model.EstadoInscripcion;
import com.version.gymModuloControl.model.Inscripcion;

public interface InscripcionRepository extends JpaRepository<Inscripcion, Integer> {
    // Cambiar esto
    List<Inscripcion> findByClienteIdCliente(Integer clienteId);

    // Cambiar esto
    List<Inscripcion> findByRecepcionistaIdEmpleado(Integer recepcionistaId);

    boolean existsByPlan_IdPlan(Integer idPlan);
    Optional<Inscripcion> findByClienteIdClienteAndFechaFinAfterAndEstadoTrue(Integer idCliente, LocalDate fecha);
    Optional<Inscripcion> findTopByClienteIdClienteOrderByFechaInscripcionDesc(Integer idCliente);

    List<Inscripcion> findByClienteIdClienteAndEstadoIn(Integer idCliente, List<String> estados);
    List<Inscripcion> findByPlanIdPlanAndEstado(Integer idPlan, EstadoInscripcion estado);


    
    @Query("SELECT COUNT(i) FROM Inscripcion i WHERE DATE(i.fechaInscripcion) = CURRENT_DATE AND i.estado = 'ACTIVO'")
    Long countInscripcionesHoy();
    
    @Query(value = """
        SELECT 
            DATE_FORMAT(i.fecha_inscripcion, '%Y-%m') as mes,
            MONTHNAME(i.fecha_inscripcion) as nombreMes,
            COUNT(*) as cantidadInscripciones,
            COALESCE(SUM(p.precio), 0.0) as montoTotal
        FROM inscripcion i 
        JOIN plan p ON i.plan_id = p.id_plan
        WHERE i.estado = 'ACTIVO' 
        AND i.fecha_inscripcion >= DATE_SUB(CURRENT_DATE, INTERVAL 6 MONTH)
        GROUP BY mes, nombreMes
        ORDER BY mes DESC
        """, nativeQuery = true)
    List<Map<String, Object>> getInscripcionesPorMes();
    
    @Query(value = """
        SELECT 
            p.nombre as nombrePlan,
            COUNT(i.id_inscripcion) as cantidadClientes,
            ROUND((COUNT(i.id_inscripcion) * 100.0 / (SELECT COUNT(*) FROM inscripcion WHERE estado = 'ACTIVO')), 2) as porcentaje
        FROM inscripcion i
        JOIN plan p ON i.plan_id = p.id_plan
        WHERE i.estado = 'ACTIVO'
        AND i.fecha_fin > CURRENT_DATE
        GROUP BY p.id_plan, p.nombre
        ORDER BY cantidadClientes DESC
        """, nativeQuery = true)
    List<Map<String, Object>> getClientesPorPlan();
    
    @Query(value = """
        SELECT 
            i.id_inscripcion as idInscripcion,
            pc.nombre as nombreCliente,
            pc.apellidos as apellidosCliente,
            p.nombre as nombrePlan,
            i.fecha_inscripcion as fechaInscripcion,
            pe.nombre as nombreRecepcionista
        FROM inscripcion i
        JOIN cliente c ON i.cliente_id = c.id_cliente
        JOIN persona pc ON c.persona_id = pc.id_persona
        JOIN plan p ON i.plan_id = p.id_plan
        LEFT JOIN empleado e ON i.recepcionista_id = e.id_empleado
        LEFT JOIN persona pe ON e.persona_id = pe.id_persona
        WHERE i.estado = 'ACTIVO'
        ORDER BY i.fecha_inscripcion DESC
        LIMIT 10
        """, nativeQuery = true)
    List<Map<String, Object>> getUltimasInscripciones();

}

