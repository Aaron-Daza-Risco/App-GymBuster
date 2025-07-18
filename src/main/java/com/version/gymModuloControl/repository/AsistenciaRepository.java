package com.version.gymModuloControl.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.version.gymModuloControl.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.version.gymModuloControl.model.Asistencia;

public interface AsistenciaRepository extends JpaRepository<Asistencia, Integer> {
    List<Asistencia> findByClienteIdCliente(Integer clienteId);
    boolean existsByClienteAndFecha(Cliente cliente, LocalDate fecha);
    
    @Query(value = """
        SELECT 
            CASE DAYOFWEEK(a.fecha)
                WHEN 1 THEN 'Domingo'
                WHEN 2 THEN 'Lunes'
                WHEN 3 THEN 'Martes'
                WHEN 4 THEN 'Miércoles'
                WHEN 5 THEN 'Jueves'
                WHEN 6 THEN 'Viernes'
                WHEN 7 THEN 'Sábado'
            END as diaSemana,
            COUNT(*) as cantidadAsistencias
        FROM asistencia a
        WHERE a.fecha >= DATE_SUB(CURRENT_DATE, INTERVAL 30 DAY)
        GROUP BY DAYOFWEEK(a.fecha), diaSemana
        ORDER BY DAYOFWEEK(a.fecha)
        """, nativeQuery = true)
    List<Map<String, Object>> getAsistenciasPorDiaSemana();
}

