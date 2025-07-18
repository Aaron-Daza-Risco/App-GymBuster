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
    
    @Query(value = """
        SELECT 
            DATE_FORMAT(v.fecha, '%Y-%m') as mes,
            MONTHNAME(v.fecha) as nombreMes,
            COUNT(*) as cantidadVentas,
            SUM(v.total) as totalVentas
        FROM venta v 
        WHERE v.estado = true 
        AND v.fecha >= DATE_SUB(CURRENT_DATE, INTERVAL 6 MONTH)
        GROUP BY mes, nombreMes
        ORDER BY mes DESC
        """, nativeQuery = true)
    List<Map<String, Object>> getVentasPorMes();
    
    @Query(value = """
        SELECT 
            DATE_FORMAT(v.fecha, '%Y-%m') as mes,
            MONTHNAME(v.fecha) as nombreMes,
            COUNT(*) as cantidadVentas,
            SUM(v.total) as totalVentas
        FROM venta v 
        WHERE v.estado = true 
        AND v.fecha >= DATE_SUB(CURRENT_DATE, INTERVAL ?1 MONTH)
        GROUP BY mes, nombreMes
        ORDER BY mes DESC
        """, nativeQuery = true)
    List<Map<String, Object>> getVentasPorMesConPeriodo(int meses);
    
    @Query(value = """
        SELECT 
            p.nombre as nombreProducto,
            COUNT(dv.id_detalle_venta) as cantidadVendida,
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
            p.id_producto as idProducto,
            dv.precio_unitario as precioUnitario,
            COUNT(dv.id_detalle_venta) as cantidadVendida,
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
}
