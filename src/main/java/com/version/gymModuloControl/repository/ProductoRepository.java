package com.version.gymModuloControl.repository;
import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.version.gymModuloControl.model.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    boolean existsByCategoria_IdCategoria(Integer idCategoria);
    
    Long countByEstadoTrue();
    
    Long countByStockTotalAndEstado(Integer stockTotal, Boolean estado);
    
    @Query("SELECT COUNT(p) FROM Producto p WHERE p.stockTotal <= p.stockMinimo AND p.estado = true")
    Long countProductosBajoStock();
    
    @Query(value = """
        SELECT 
            p.nombre as nombreProducto,
            p.stock_total as stockActual,
            p.stock_minimo as stockMinimo,
            c.nombre as categoria,
            ROUND((p.stock_total * 100.0 / GREATEST(p.stock_minimo, 1)), 2) as porcentajeStock
        FROM producto p
        JOIN categoria c ON p.categoria_id = c.id_categoria
        WHERE p.stock_total <= p.stock_minimo 
        AND p.estado = true
        ORDER BY porcentajeStock ASC
        LIMIT 10
        """, nativeQuery = true)
    List<Map<String, Object>> getProductosBajoStock();
    
    @Query(value = """
        SELECT 
            p.id_producto as idProducto,
            p.nombre as nombreProducto,
            p.precio_compra as precioCompra,
            p.precio_venta as precioVenta,
            p.stock_total as stockTotal,
            p.stock_minimo as stockMinimo,
            c.nombre as categoria
        FROM producto p
        JOIN categoria c ON p.categoria_id = c.id_categoria
        WHERE p.id_producto = ?1 AND p.estado = true
        """, nativeQuery = true)
    Map<String, Object> getDetalleProducto(Integer idProducto);

}
