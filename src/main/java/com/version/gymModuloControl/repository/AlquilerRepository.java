package com.version.gymModuloControl.repository;

import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.version.gymModuloControl.model.Alquiler;
import com.version.gymModuloControl.model.EstadoAlquiler;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AlquilerRepository extends JpaRepository<Alquiler, Integer> {
    /**
     * Encuentra alquileres por estado y con fecha de fin anterior a la fecha especificada
     * @param estado El estado del alquiler a buscar
     * @param fecha La fecha límite antes de la cual buscar
     * @return Lista de alquileres que cumplen con el criterio
     */
    List<Alquiler> findByEstadoAndFechaFinBefore(EstadoAlquiler estado, LocalDate fecha);

    @Query(value = """
    SELECT a.estado, COUNT(*) 
    FROM alquiler a
    WHERE MONTH(a.fecha_inicio) = MONTH(CURRENT_DATE)
    AND YEAR(a.fecha_inicio) = YEAR(CURRENT_DATE)
    GROUP BY a.estado
    """, nativeQuery = true)
    List<Object[]> contarAlquileresPorEstadoMesActual();

    @Query(value = """
    SELECT a.estado, COUNT(*) 
    FROM alquiler a
    WHERE QUARTER(a.fecha_inicio) = QUARTER(CURRENT_DATE)
    AND YEAR(a.fecha_inicio) = YEAR(CURRENT_DATE)
    GROUP BY a.estado
    """, nativeQuery = true)
    List<Object[]> contarAlquileresPorEstadoTrimestreActual();

    @Query(value = """
    SELECT a.estado, COUNT(*) 
    FROM alquiler a
    WHERE YEAR(a.fecha_inicio) = YEAR(CURRENT_DATE)
    GROUP BY a.estado
    """, nativeQuery = true)
    List<Object[]> contarAlquileresPorEstadoAnioActual();


    // DetalleAlquilerRepository.java
    @Query("SELECT d.pieza.nombre, SUM(d.cantidad) as total FROM DetalleAlquiler d GROUP BY d.pieza.nombre ORDER BY total DESC")
    List<Object[]> rankingPiezasMasAlquiladas();

    @Query("SELECT d.pieza.nombre, a.estado, a.mora " +
            "FROM Alquiler a JOIN a.detalles d " +
            "WHERE a.estado = com.version.gymModuloControl.model.EstadoAlquiler.VENCIDO")
    List<Object[]> alquileresVencidos();

    @Query(value = """
    SELECT COALESCE(SUM(a.total), 0.0)
    FROM alquiler a
    WHERE a.estado <> 'CANCELADO'
    AND MONTH(a.fecha_inicio) = MONTH(CURRENT_DATE)
    AND YEAR(a.fecha_inicio) = YEAR(CURRENT_DATE)
    """, nativeQuery = true)
    Double sumTotalAlquileresEsteMes();

    @Query(value = """
    SELECT COALESCE(SUM(a.total), 0.0)
    FROM alquiler a
    WHERE a.estado <> 'CANCELADO'
    AND QUARTER(a.fecha_inicio) = QUARTER(CURRENT_DATE)
    AND YEAR(a.fecha_inicio) = YEAR(CURRENT_DATE)
    """, nativeQuery = true)
    Double sumTotalAlquileresTrimestreActual();

    @Query(value = """
    SELECT COALESCE(SUM(a.total), 0.0)
    FROM alquiler a
    WHERE a.estado <> 'CANCELADO'
    AND YEAR(a.fecha_inicio) = YEAR(CURRENT_DATE)
    """, nativeQuery = true)
    Double sumTotalAlquileresAnioActual();


    @Query("""
    SELECT d.pieza.nombre, SUM(d.cantidad) as unidadesAlquiladas, SUM(d.subtotal) as ingresos
    FROM DetalleAlquiler d
    WHERE MONTH(d.alquiler.fechaInicio) = MONTH(CURRENT_DATE)
    AND YEAR(d.alquiler.fechaInicio) = YEAR(CURRENT_DATE)
    GROUP BY d.pieza.nombre
    ORDER BY unidadesAlquiladas DESC
    """)
    List<Object[]> rankingPiezasMasAlquiladasTop10MesActual(Pageable pageable);

    @Query("""
    SELECT d.pieza.nombre, SUM(d.cantidad) as unidadesAlquiladas, SUM(d.subtotal) as ingresos
    FROM DetalleAlquiler d
    WHERE QUARTER(d.alquiler.fechaInicio) = QUARTER(CURRENT_DATE)
    AND YEAR(d.alquiler.fechaInicio) = YEAR(CURRENT_DATE)
    GROUP BY d.pieza.nombre
    ORDER BY unidadesAlquiladas DESC
    """)
    List<Object[]> rankingPiezasMasAlquiladasTop10TrimestreActual(Pageable pageable);

    @Query("""
    SELECT d.pieza.nombre, SUM(d.cantidad) as unidadesAlquiladas, SUM(d.subtotal) as ingresos
    FROM DetalleAlquiler d
    WHERE YEAR(d.alquiler.fechaInicio) = YEAR(CURRENT_DATE)
    GROUP BY d.pieza.nombre
    ORDER BY unidadesAlquiladas DESC
    """)
    List<Object[]> rankingPiezasMasAlquiladasTop10AnioActual(Pageable pageable);


    @Query("SELECT FUNCTION('MONTH', a.fechaInicio) as mes, SUM(a.total) as total FROM Alquiler a WHERE a.estado = com.version.gymModuloControl.model.EstadoAlquiler.FINALIZADO AND a.fechaInicio >= :fechaInicio GROUP BY FUNCTION('MONTH', a.fechaInicio) ORDER BY mes")
    List<Object[]> alquileresPorMes(LocalDate fechaInicio);
}
