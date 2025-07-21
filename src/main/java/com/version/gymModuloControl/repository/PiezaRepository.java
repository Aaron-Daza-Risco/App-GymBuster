package com.version.gymModuloControl.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.version.gymModuloControl.model.Pieza;

public interface PiezaRepository extends JpaRepository<Pieza, Integer> {
    
    @Query(value = """
        SELECT COALESCE(SUM(a.total), 0.0)
        FROM alquiler a
        WHERE a.estado = 'FINALIZADO'
        """, nativeQuery = true)
    Double sumTotalAlquileres();

    @Query(value = """
        SELECT 
            p.id_pieza as idPieza,
            p.nombre as nombrePieza,
            p.stock as stockActual,
            p.stock_minimo as stockMinimo,
            ROUND((p.stock * 100.0 / GREATEST(p.stock_minimo, 1)), 2) as porcentajeStock
        FROM pieza p
        WHERE p.stock <= p.stock_minimo 
          AND p.estado = true
        ORDER BY porcentajeStock ASC
        LIMIT 10
        """, nativeQuery = true)
    List<Map<String, Object>> getPiezasBajoStock();

    // Total de piezas activas
    Long countByEstadoTrue();

    // Total de piezas agotadas (stock 0 y activas)
    Long countByStockAndEstado(Integer stock, Boolean estado);
}
