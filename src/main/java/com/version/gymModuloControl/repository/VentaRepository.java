package com.version.gymModuloControl.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.version.gymModuloControl.model.Venta;

public interface VentaRepository extends JpaRepository<Venta, Integer> {
    
    @Query("SELECT COUNT(v) FROM Venta v WHERE DATE(v.fecha) = CURRENT_DATE AND v.estado = true")
    Long countVentasHoy();
    
    @Query("SELECT COALESCE(SUM(v.total), 0.0) FROM Venta v WHERE v.estado = true")
    Double sumTotalVentas();
    
    // Suma total de ventas basado en detalles de venta (sin descuentos a nivel de transacción)
    @Query(value = """
        SELECT COALESCE(SUM(dv.cantidad * dv.precio_unitario), 0.0) 
        FROM detalle_venta dv
        JOIN venta v ON dv.venta_id = v.id_venta
        WHERE v.estado = true
        """, nativeQuery = true)
    Double sumTotalVentasPorDetalles();
    
    @Query(value = """
        SELECT 
            DATE_FORMAT(v.fecha, '%Y-%m') as mes,
            CASE MONTH(v.fecha)
                WHEN 1 THEN 'Enero'
                WHEN 2 THEN 'Febrero'
                WHEN 3 THEN 'Marzo'
                WHEN 4 THEN 'Abril'
                WHEN 5 THEN 'Mayo'
                WHEN 6 THEN 'Junio'
                WHEN 7 THEN 'Julio'
                WHEN 8 THEN 'Agosto'
                WHEN 9 THEN 'Septiembre'
                WHEN 10 THEN 'Octubre'
                WHEN 11 THEN 'Noviembre'
                WHEN 12 THEN 'Diciembre'
            END as nombreMes,
            COUNT(DISTINCT v.id_venta) as cantidadVentas,
            COALESCE(SUM(dv.cantidad * dv.precio_unitario), 0.0) as totalVentas
        FROM detalle_venta dv
        JOIN venta v ON dv.venta_id = v.id_venta
        WHERE v.estado = true 
        AND v.fecha >= DATE_SUB(CURRENT_DATE, INTERVAL 6 MONTH)
        GROUP BY mes, nombreMes
        ORDER BY mes DESC
        """, nativeQuery = true)
    List<Map<String, Object>> getVentasPorMes();
    
    @Query(value = """
        SELECT 
            DATE_FORMAT(v.fecha, '%Y-%m') as mes,
            CASE MONTH(v.fecha)
                WHEN 1 THEN 'Enero'
                WHEN 2 THEN 'Febrero'
                WHEN 3 THEN 'Marzo'
                WHEN 4 THEN 'Abril'
                WHEN 5 THEN 'Mayo'
                WHEN 6 THEN 'Junio'
                WHEN 7 THEN 'Julio'
                WHEN 8 THEN 'Agosto'
                WHEN 9 THEN 'Septiembre'
                WHEN 10 THEN 'Octubre'
                WHEN 11 THEN 'Noviembre'
                WHEN 12 THEN 'Diciembre'
            END as nombreMes,
            COUNT(DISTINCT v.id_venta) as cantidadVentas,
            COALESCE(SUM(dv.cantidad * dv.precio_unitario), 0.0) as totalVentas
        FROM detalle_venta dv
        JOIN venta v ON dv.venta_id = v.id_venta
        WHERE v.estado = true 
        AND v.fecha >= DATE_SUB(CURRENT_DATE, INTERVAL ?1 MONTH)
        GROUP BY mes, nombreMes
        ORDER BY mes DESC
        """, nativeQuery = true)
    List<Map<String, Object>> getVentasPorMesConPeriodo(int meses);
    
    @Query(value = """
        SELECT 
            p.nombre as nombreProducto,
            SUM(dv.cantidad) as cantidadVendida,
            SUM(dv.cantidad * dv.precio_unitario) as totalVentas
        FROM detalle_venta dv
        JOIN producto p ON dv.producto_id = p.id_producto
        JOIN venta v ON dv.venta_id = v.id_venta
        WHERE v.estado = true
        AND v.fecha >= DATE_SUB(CURRENT_DATE, INTERVAL 1 MONTH)
        GROUP BY p.id_producto, p.nombre
        ORDER BY cantidadVendida DESC
        LIMIT 10
        """, nativeQuery = true)
    List<Map<String, Object>> getProductosMasVendidos();
    
    @Query(value = """
        SELECT 
            p.nombre as nombreProducto,
            SUM(dv.cantidad) as cantidadVendida,
            SUM(dv.cantidad * dv.precio_unitario) as totalVentas
        FROM detalle_venta dv
        JOIN producto p ON dv.producto_id = p.id_producto
        JOIN venta v ON dv.venta_id = v.id_venta
        WHERE v.estado = true
        AND MONTH(v.fecha) = MONTH(CURRENT_DATE) 
        AND YEAR(v.fecha) = YEAR(CURRENT_DATE)
        GROUP BY p.id_producto, p.nombre
        ORDER BY cantidadVendida DESC
        LIMIT 10
        """, nativeQuery = true)
    List<Map<String, Object>> getProductosMasVendidosEsteMes();
    
    @Query(value = """
        SELECT 
            p.nombre as nombreProducto,
            p.id_producto as idProducto,
            dv.precio_unitario as precioUnitario,
            SUM(dv.cantidad) as cantidadVendida,
            SUM(dv.cantidad * dv.precio_unitario) as totalVentas
        FROM detalle_venta dv
        JOIN producto p ON dv.producto_id = p.id_producto
        JOIN venta v ON dv.venta_id = v.id_venta
        WHERE v.estado = true
        AND v.fecha >= DATE_SUB(CURRENT_DATE, INTERVAL ?1 MONTH)
        GROUP BY p.id_producto, p.nombre, dv.precio_unitario
        ORDER BY cantidadVendida DESC
        LIMIT 10
        """, nativeQuery = true)
    List<Map<String, Object>> getProductosMasVendidosConPeriodo(int meses);
    
    @Query(value = """
        SELECT 
            c.nombre as categoria,
            COUNT(dv.id_detalle_venta) as cantidadVendida,
            SUM(dv.cantidad * dv.precio_unitario) as totalVentas
        FROM detalle_venta dv
        JOIN producto p ON dv.producto_id = p.id_producto
        JOIN categoria c ON p.categoria_id = c.id_categoria
        JOIN venta v ON dv.venta_id = v.id_venta
        WHERE v.estado = true
        AND v.fecha >= DATE_SUB(CURRENT_DATE, INTERVAL 1 MONTH)
        GROUP BY c.id_categoria, c.nombre
        ORDER BY cantidadVendida DESC
        """, nativeQuery = true)
    List<Map<String, Object>> getVentasPorCategoria();
    
    @Query(value = """
        SELECT 
            c.nombre as categoria,
            COUNT(dv.id_detalle_venta) as cantidadVendida,
            SUM(dv.cantidad * dv.precio_unitario) as totalVentas
        FROM detalle_venta dv
        JOIN producto p ON dv.producto_id = p.id_producto
        JOIN categoria c ON p.categoria_id = c.id_categoria
        JOIN venta v ON dv.venta_id = v.id_venta
        WHERE v.estado = true
        AND MONTH(v.fecha) = MONTH(CURRENT_DATE) 
        AND YEAR(v.fecha) = YEAR(CURRENT_DATE)
        GROUP BY c.id_categoria, c.nombre
        ORDER BY cantidadVendida DESC
        """, nativeQuery = true)
    List<Map<String, Object>> getVentasPorCategoriaEsteMes();
    
    @Query(value = """
        SELECT 
            c.nombre as categoria,
            COUNT(dv.id_detalle_venta) as cantidadVendida,
            SUM(dv.cantidad * dv.precio_unitario) as totalVentas
        FROM detalle_venta dv
        JOIN producto p ON dv.producto_id = p.id_producto
        JOIN categoria c ON p.categoria_id = c.id_categoria
        JOIN venta v ON dv.venta_id = v.id_venta
        WHERE v.estado = true
        AND v.fecha >= DATE_SUB(CURRENT_DATE, INTERVAL ?1 MONTH)
        GROUP BY c.id_categoria, c.nombre
        ORDER BY cantidadVendida DESC
        """, nativeQuery = true)
    List<Map<String, Object>> getVentasPorCategoriaConPeriodo(int meses);
    
    @Query(value = """
        SELECT 
            v.id_venta as idVenta,
            pc.nombre as nombreCliente,
            pc.apellidos as apellidosCliente,
            v.total as total,
            v.fecha as fechaVenta,
            pe.nombre as nombreEmpleado
        FROM venta v
        JOIN cliente c ON v.cliente_id = c.id_cliente
        JOIN persona pc ON c.persona_id = pc.id_persona
        JOIN empleado e ON v.empleado_id = e.id_empleado
        JOIN persona pe ON e.persona_id = pe.id_persona
        WHERE v.estado = true
        ORDER BY v.fecha DESC, v.hora DESC
        LIMIT 10
        """, nativeQuery = true)
    List<Map<String, Object>> getUltimasVentas();
    
    // ===== MÉTODOS PARA REPORTES DE VENTAS Y FINANZAS =====
    
    // Ingresos totales por período
    @Query(value = """
        SELECT COALESCE(SUM(v.total), 0.0) 
        FROM venta v 
        WHERE v.estado = true 
        AND v.fecha >= DATE_SUB(CURRENT_DATE, INTERVAL ?1 MONTH)
        """, nativeQuery = true)
    Double sumTotalVentasPorPeriodo(int meses);
    
    // Ingresos del mes actual
    @Query(value = """
        SELECT COALESCE(SUM(v.total), 0.0) 
        FROM venta v 
        WHERE v.estado = true 
        AND MONTH(v.fecha) = MONTH(CURRENT_DATE) 
        AND YEAR(v.fecha) = YEAR(CURRENT_DATE)
        """, nativeQuery = true)
    Double sumTotalVentasEsteMes();
    
    // Ingresos del mes actual basado en detalles de venta (sin descuentos a nivel de transacción)
    @Query(value = """
        SELECT COALESCE(SUM(dv.cantidad * dv.precio_unitario), 0.0) 
        FROM detalle_venta dv
        JOIN venta v ON dv.venta_id = v.id_venta
        WHERE v.estado = true 
        AND MONTH(v.fecha) = MONTH(CURRENT_DATE) 
        AND YEAR(v.fecha) = YEAR(CURRENT_DATE)
        """, nativeQuery = true)
    Double sumIngresosPorDetallesEsteMes();
    
    // Ingresos por período basado en detalles de venta
    @Query(value = """
        SELECT COALESCE(SUM(dv.cantidad * dv.precio_unitario), 0.0) 
        FROM detalle_venta dv
        JOIN venta v ON dv.venta_id = v.id_venta
        WHERE v.estado = true 
        AND v.fecha >= DATE_SUB(CURRENT_DATE, INTERVAL ?1 MONTH)
        """, nativeQuery = true)
    Double sumIngresosPorDetallesPorPeriodo(int meses);
    
    // Ingresos período anterior basado en detalles de venta
    @Query(value = """
        SELECT COALESCE(SUM(dv.cantidad * dv.precio_unitario), 0.0) 
        FROM detalle_venta dv
        JOIN venta v ON dv.venta_id = v.id_venta
        WHERE v.estado = true 
        AND v.fecha >= DATE_SUB(CURRENT_DATE, INTERVAL ?1*2 MONTH)
        AND v.fecha < DATE_SUB(CURRENT_DATE, INTERVAL ?1 MONTH)
        """, nativeQuery = true)
    Double sumIngresosPorDetallesPeriodoAnterior(int meses);
    
    // Ingresos por mes - basado en detalles de venta
    @Query(value = """
        SELECT 
            DATE_FORMAT(v.fecha, '%Y-%m') as mes,
            CASE MONTH(v.fecha)
                WHEN 1 THEN 'Enero'
                WHEN 2 THEN 'Febrero'
                WHEN 3 THEN 'Marzo'
                WHEN 4 THEN 'Abril'
                WHEN 5 THEN 'Mayo'
                WHEN 6 THEN 'Junio'
                WHEN 7 THEN 'Julio'
                WHEN 8 THEN 'Agosto'
                WHEN 9 THEN 'Septiembre'
                WHEN 10 THEN 'Octubre'
                WHEN 11 THEN 'Noviembre'
                WHEN 12 THEN 'Diciembre'
            END as nombreMes,
            COALESCE(SUM(dv.cantidad * dv.precio_unitario), 0.0) as totalIngresos,
            COUNT(DISTINCT v.id_venta) as cantidadVentas
        FROM detalle_venta dv
        JOIN venta v ON dv.venta_id = v.id_venta
        WHERE v.estado = true 
        AND v.fecha >= DATE_SUB(CURRENT_DATE, INTERVAL ?1 MONTH)
        GROUP BY mes, nombreMes
        ORDER BY mes DESC
        """, nativeQuery = true)
    List<Map<String, Object>> getIngresosPorMes(int meses);
    
    // Ventas período anterior
    @Query(value = """
        SELECT COALESCE(SUM(v.total), 0.0) 
        FROM venta v 
        WHERE v.estado = true 
        AND v.fecha >= DATE_SUB(CURRENT_DATE, INTERVAL ?1*2 MONTH)
        AND v.fecha < DATE_SUB(CURRENT_DATE, INTERVAL ?1 MONTH)
        """, nativeQuery = true)
    Double sumTotalVentasPeriodoAnterior(int meses);
    
    // Comparativa mensual mejorada - muestra todos los meses con comparación disponible (basado en detalles)
    @Query(value = """
        WITH todos_los_meses AS (
            SELECT 
                DATE_FORMAT(v.fecha, '%Y-%m') as mes,
                CASE MONTH(v.fecha)
                    WHEN 1 THEN 'Enero'
                    WHEN 2 THEN 'Febrero'
                    WHEN 3 THEN 'Marzo'
                    WHEN 4 THEN 'Abril'
                    WHEN 5 THEN 'Mayo'
                    WHEN 6 THEN 'Junio'
                    WHEN 7 THEN 'Julio'
                    WHEN 8 THEN 'Agosto'
                    WHEN 9 THEN 'Septiembre'
                    WHEN 10 THEN 'Octubre'
                    WHEN 11 THEN 'Noviembre'
                    WHEN 12 THEN 'Diciembre'
                END as nombreMes,
                COALESCE(SUM(dv.cantidad * dv.precio_unitario), 0.0) as ventasActuales
            FROM detalle_venta dv
            JOIN venta v ON dv.venta_id = v.id_venta
            WHERE v.estado = true 
            AND v.fecha >= DATE_SUB(CURRENT_DATE, INTERVAL 8 MONTH)
            GROUP BY mes, nombreMes
            HAVING ventasActuales > 0
            ORDER BY mes ASC
        ),
        comparativa AS (
            SELECT 
                mes,
                nombreMes,
                ventasActuales,
                LAG(ventasActuales) OVER (ORDER BY mes) as ventasAnteriores,
                CASE 
                    WHEN LAG(ventasActuales) OVER (ORDER BY mes) > 0 
                    THEN ROUND(((ventasActuales - LAG(ventasActuales) OVER (ORDER BY mes)) / LAG(ventasActuales) OVER (ORDER BY mes)) * 100, 1)
                    ELSE NULL 
                END as porcentajeCrecimiento
            FROM todos_los_meses
        )
        SELECT 
            mes,
            nombreMes,
            ventasActuales,
            ventasAnteriores,
            porcentajeCrecimiento
        FROM comparativa 
        WHERE ventasAnteriores IS NOT NULL
        ORDER BY mes DESC
        """, nativeQuery = true)
    List<Map<String, Object>> getComparativaMensual(int meses);
    
    // Verificar si existen ventas en períodos anteriores
    @Query(value = """
        SELECT COUNT(*) 
        FROM venta v 
        WHERE v.estado = true 
        AND v.fecha >= DATE_SUB(CURRENT_DATE, INTERVAL ?1*2 MONTH)
        AND v.fecha < DATE_SUB(CURRENT_DATE, INTERVAL ?1 MONTH)
        """, nativeQuery = true)
    Long countVentasPeriodoAnterior(int meses);
    
    // Obtener fecha de la primera venta
    @Query(value = """
        SELECT MIN(v.fecha) as primeraVenta
        FROM venta v 
        WHERE v.estado = true
        """, nativeQuery = true)
    String getFechaprimeraVenta();
    
    // Total transacciones por período
    @Query(value = """
        SELECT COUNT(*) 
        FROM venta v 
        WHERE v.estado = true 
        AND v.fecha >= DATE_SUB(CURRENT_DATE, INTERVAL ?1 MONTH)
        """, nativeQuery = true)
    Long countVentasPorPeriodo(int meses);
    
    // Transacciones por mes
    @Query(value = """
        SELECT 
            DATE_FORMAT(v.fecha, '%Y-%m') as mes,
            CASE MONTH(v.fecha)
                WHEN 1 THEN 'Enero'
                WHEN 2 THEN 'Febrero'
                WHEN 3 THEN 'Marzo'
                WHEN 4 THEN 'Abril'
                WHEN 5 THEN 'Mayo'
                WHEN 6 THEN 'Junio'
                WHEN 7 THEN 'Julio'
                WHEN 8 THEN 'Agosto'
                WHEN 9 THEN 'Septiembre'
                WHEN 10 THEN 'Octubre'
                WHEN 11 THEN 'Noviembre'
                WHEN 12 THEN 'Diciembre'
            END as nombreMes,
            COUNT(*) as cantidadTransacciones,
            COALESCE(AVG(v.total), 0.0) as ticketPromedio
        FROM venta v 
        WHERE v.estado = true 
        AND v.fecha >= DATE_SUB(CURRENT_DATE, INTERVAL ?1 MONTH)
        GROUP BY mes, nombreMes
        ORDER BY mes DESC
        """, nativeQuery = true)
    List<Map<String, Object>> getTransaccionesPorMes(int meses);
    
    // Promedio transacciones diarias
    @Query(value = """
        SELECT COALESCE(AVG(diarias.cantidad), 0.0) as promedio
        FROM (
            SELECT DATE(v.fecha) as fecha, COUNT(*) as cantidad
            FROM venta v 
            WHERE v.estado = true 
            AND v.fecha >= DATE_SUB(CURRENT_DATE, INTERVAL ?1 MONTH)
            GROUP BY DATE(v.fecha)
        ) as diarias
        """, nativeQuery = true)
    Double getPromedioTransaccionesDiarias(int meses);
    
    // COMENTADA TEMPORALMENTE - Rendimiento categorías
    /*
    @Query(value = """
        SELECT 
            c.nombre as categoria,
            COALESCE(SUM(CASE WHEN v.fecha >= DATE_SUB(CURRENT_DATE, INTERVAL ?1 MONTH) 
                         THEN dv.cantidad * dv.precio_unitario ELSE 0 END), 0) as ventasActuales,
            COALESCE(SUM(CASE WHEN v.fecha >= DATE_SUB(CURRENT_DATE, INTERVAL ?1*2 MONTH) 
                         AND v.fecha < DATE_SUB(CURRENT_DATE, INTERVAL ?1 MONTH) 
                         THEN dv.cantidad * dv.precio_unitario ELSE 0 END), 0) as ventasAnteriores
        FROM detalle_venta dv
        JOIN producto p ON dv.producto_id = p.id_producto
        JOIN categoria c ON p.categoria_id = c.id_categoria
        JOIN venta v ON dv.venta_id = v.id_venta
        WHERE v.estado = true
        GROUP BY c.id_categoria, c.nombre
        ORDER BY ventasActuales DESC
        """, nativeQuery = true)
    List<Map<String, Object>> getRendimientoCategorias(int meses);
    */
    
    // Evolución de ventas (siempre últimos 6 meses para mostrar tendencia completa) - basado en detalles
    @Query(value = """
        SELECT 
            DATE_FORMAT(v.fecha, '%Y-%m') as mes,
            CASE MONTH(v.fecha)
                WHEN 1 THEN 'Enero'
                WHEN 2 THEN 'Febrero'
                WHEN 3 THEN 'Marzo'
                WHEN 4 THEN 'Abril'
                WHEN 5 THEN 'Mayo'
                WHEN 6 THEN 'Junio'
                WHEN 7 THEN 'Julio'
                WHEN 8 THEN 'Agosto'
                WHEN 9 THEN 'Septiembre'
                WHEN 10 THEN 'Octubre'
                WHEN 11 THEN 'Noviembre'
                WHEN 12 THEN 'Diciembre'
            END as nombreMes,
            COALESCE(SUM(dv.cantidad * dv.precio_unitario), 0.0) as totalVentas,
            COUNT(DISTINCT v.id_venta) as cantidadVentas,
            CASE WHEN COUNT(DISTINCT v.id_venta) > 0 
                 THEN COALESCE(SUM(dv.cantidad * dv.precio_unitario) / COUNT(DISTINCT v.id_venta), 0.0) 
                 ELSE 0.0 END as ticketPromedio
        FROM detalle_venta dv
        JOIN venta v ON dv.venta_id = v.id_venta
        WHERE v.estado = true 
        AND v.fecha >= DATE_SUB(CURRENT_DATE, INTERVAL 6 MONTH)
        GROUP BY mes, nombreMes
        ORDER BY mes ASC
        """, nativeQuery = true)
    List<Map<String, Object>> getEvolucionVentas(int meses);
    
    // Evolución de ventas con período personalizado (para cálculos de predicción) - basado en detalles
    @Query(value = """
        SELECT 
            DATE_FORMAT(v.fecha, '%Y-%m') as mes,
            CASE MONTH(v.fecha)
                WHEN 1 THEN 'Enero'
                WHEN 2 THEN 'Febrero'
                WHEN 3 THEN 'Marzo'
                WHEN 4 THEN 'Abril'
                WHEN 5 THEN 'Mayo'
                WHEN 6 THEN 'Junio'
                WHEN 7 THEN 'Julio'
                WHEN 8 THEN 'Agosto'
                WHEN 9 THEN 'Septiembre'
                WHEN 10 THEN 'Octubre'
                WHEN 11 THEN 'Noviembre'
                WHEN 12 THEN 'Diciembre'
            END as nombreMes,
            COALESCE(SUM(dv.cantidad * dv.precio_unitario), 0.0) as totalVentas,
            COUNT(DISTINCT v.id_venta) as cantidadVentas,
            CASE WHEN COUNT(DISTINCT v.id_venta) > 0 
                 THEN COALESCE(SUM(dv.cantidad * dv.precio_unitario) / COUNT(DISTINCT v.id_venta), 0.0) 
                 ELSE 0.0 END as ticketPromedio
        FROM detalle_venta dv
        JOIN venta v ON dv.venta_id = v.id_venta
        WHERE v.estado = true 
        AND v.fecha >= DATE_SUB(CURRENT_DATE, INTERVAL ?1 MONTH)
        GROUP BY mes, nombreMes
        ORDER BY mes ASC
        """, nativeQuery = true)
    List<Map<String, Object>> getEvolucionVentasConPeriodo(int meses);
    
    // Rentabilidad productos
    @Query(value = """
        SELECT 
            p.nombre as nombreProducto,
            p.precio_venta as precioVenta,
            p.precio_compra as precioCompra,
            COALESCE(SUM(dv.cantidad), 0) as cantidadVendida,
            COALESCE(SUM(dv.cantidad * dv.precio_unitario), 0.0) as ingresos,
            COALESCE(SUM(dv.cantidad * p.precio_compra), 0.0) as costos,
            COALESCE(SUM(dv.cantidad * dv.precio_unitario) - SUM(dv.cantidad * p.precio_compra), 0.0) as utilidad,
            CASE WHEN SUM(dv.cantidad * dv.precio_unitario) > 0 
                 THEN ((SUM(dv.cantidad * dv.precio_unitario) - SUM(dv.cantidad * p.precio_compra)) / SUM(dv.cantidad * dv.precio_unitario)) * 100 
                 ELSE 0 END as margenPorcentaje
        FROM detalle_venta dv
        JOIN producto p ON dv.producto_id = p.id_producto
        JOIN venta v ON dv.venta_id = v.id_venta
        WHERE v.estado = true
        AND v.fecha >= DATE_SUB(CURRENT_DATE, INTERVAL ?1 MONTH)
        GROUP BY p.id_producto, p.nombre, p.precio_venta, p.precio_compra
        HAVING cantidadVendida > 0
        ORDER BY utilidad DESC
        """, nativeQuery = true)
    List<Map<String, Object>> getRentabilidadProductos(int meses);
    
    // Rentabilidad productos - mes actual
    @Query(value = """
        SELECT 
            p.nombre as nombreProducto,
            p.precio_venta as precioVenta,
            p.precio_compra as precioCompra,
            COALESCE(SUM(dv.cantidad), 0) as cantidadVendida,
            COALESCE(SUM(dv.cantidad * dv.precio_unitario), 0.0) as ingresos,
            COALESCE(SUM(dv.cantidad * p.precio_compra), 0.0) as costos,
            COALESCE(SUM(dv.cantidad * dv.precio_unitario) - SUM(dv.cantidad * p.precio_compra), 0.0) as utilidad,
            CASE WHEN SUM(dv.cantidad * dv.precio_unitario) > 0 
                 THEN ((SUM(dv.cantidad * dv.precio_unitario) - SUM(dv.cantidad * p.precio_compra)) / SUM(dv.cantidad * dv.precio_unitario)) * 100 
                 ELSE 0 END as margenPorcentaje
        FROM detalle_venta dv
        JOIN producto p ON dv.producto_id = p.id_producto
        JOIN venta v ON dv.venta_id = v.id_venta
        WHERE v.estado = true
        AND MONTH(v.fecha) = MONTH(CURRENT_DATE) 
        AND YEAR(v.fecha) = YEAR(CURRENT_DATE)
        GROUP BY p.id_producto, p.nombre, p.precio_venta, p.precio_compra
        HAVING cantidadVendida > 0
        ORDER BY utilidad DESC
        """, nativeQuery = true)
    List<Map<String, Object>> getRentabilidadProductosEsteMes();
    
    // Margen por producto
    @Query(value = """
        SELECT 
            p.nombre as nombreProducto,
            p.precio_venta as precioVenta,
            p.precio_compra as precioCompra,
            (p.precio_venta - p.precio_compra) as margenUnitario,
            CASE WHEN p.precio_venta > 0 THEN ((p.precio_venta - p.precio_compra) / p.precio_venta) * 100 ELSE 0 END as margenPorcentaje,
            COALESCE(SUM(dv.cantidad), 0) as cantidadVendida
        FROM producto p
        LEFT JOIN detalle_venta dv ON p.id_producto = dv.producto_id
        LEFT JOIN venta v ON dv.venta_id = v.id_venta AND v.estado = true 
            AND v.fecha >= DATE_SUB(CURRENT_DATE, INTERVAL ?1 MONTH)
        WHERE p.estado = true
        GROUP BY p.id_producto, p.nombre, p.precio_venta, p.precio_compra
        ORDER BY margenPorcentaje DESC
        """, nativeQuery = true)
    List<Map<String, Object>> getMargenProductos(int meses);
    
    // Costos vs Ingresos
    @Query(value = """
        SELECT 
            COALESCE(SUM(dv.cantidad * dv.precio_unitario), 0.0) as ingresosTotales,
            COALESCE(SUM(dv.cantidad * p.precio_compra), 0.0) as costosTotales,
            COALESCE(SUM(dv.cantidad * (dv.precio_unitario - p.precio_compra)), 0.0) as utilidadBruta,
            CASE WHEN SUM(dv.cantidad * dv.precio_unitario) > 0 
                 THEN (SUM(dv.cantidad * (dv.precio_unitario - p.precio_compra)) / SUM(dv.cantidad * dv.precio_unitario)) * 100 
                 ELSE 0 END as margenBruto
        FROM detalle_venta dv
        JOIN producto p ON dv.producto_id = p.id_producto
        JOIN venta v ON dv.venta_id = v.id_venta
        WHERE v.estado = true
        AND v.fecha >= DATE_SUB(CURRENT_DATE, INTERVAL ?1 MONTH)
        """, nativeQuery = true)
    List<Map<String, Object>> getCostosVsIngresos(int meses);
}
