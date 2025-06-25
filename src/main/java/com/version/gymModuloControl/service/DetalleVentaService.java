package com.version.gymModuloControl.service;

import com.version.gymModuloControl.dto.DetalleVentaDTO;
import com.version.gymModuloControl.model.DetalleVenta;
import com.version.gymModuloControl.model.Producto;
import com.version.gymModuloControl.model.Venta;
import com.version.gymModuloControl.repository.DetalleVentaRepository;
import com.version.gymModuloControl.repository.ProductoRepository;
import com.version.gymModuloControl.repository.VentaRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class DetalleVentaService {

    @Autowired
    private DetalleVentaRepository detalleVentaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private VentaRepository ventaRepository;

    @Transactional
    public DetalleVenta agregarDetalleVenta(Integer ventaId, Integer productoId, Integer cantidad) {
        // Validar venta existente
        Venta venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada con ID: " + ventaId));

        // Validar producto existente
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + productoId));

        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
        }

        // Validar stock disponible
        if (producto.getStockTotal() < cantidad) {
            throw new IllegalArgumentException("Stock insuficiente para el producto: " + producto.getNombre());
        }

        // Calcular subtotal
        BigDecimal subtotal = producto.getPrecioVenta().multiply(BigDecimal.valueOf(cantidad));

        // Crear detalle
        DetalleVenta detalle = new DetalleVenta();
        detalle.setVenta(venta);
        detalle.setProducto(producto);
        detalle.setCantidad(cantidad);
        detalle.setPrecioUnitario(producto.getPrecioVenta());
        detalle.setSubtotal(subtotal);

        // Guardar detalle
        detalleVentaRepository.save(detalle);

        // Actualizar stock
        producto.setStockTotal(producto.getStockTotal() - cantidad);
        productoRepository.save(producto);

        // Actualizar total de la venta
        BigDecimal totalVenta = detalleVentaRepository.findByVenta_IdVenta(ventaId).stream()
                .map(DetalleVenta::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        venta.setTotal(totalVenta);
        ventaRepository.save(venta);

        return detalle;
    }

    public List<DetalleVenta> listarDetallesPorVentaId(Integer ventaId) {
        return detalleVentaRepository.findByVenta_IdVenta(ventaId);
    }

    @Transactional
    public boolean eliminarDetalleVenta(Integer detalleId) {
        DetalleVenta detalle = detalleVentaRepository.findById(detalleId).orElse(null);
        if (detalle != null) {
            // Restaurar stock del producto
            Producto producto = detalle.getProducto();
            producto.setStockTotal(producto.getStockTotal() + detalle.getCantidad());
            productoRepository.save(producto);

            // Eliminar detalle
            detalleVentaRepository.delete(detalle);

            // Actualizar total de la venta
            Venta venta = detalle.getVenta();
            BigDecimal totalVenta = detalleVentaRepository.findByVenta_IdVenta(venta.getIdVenta()).stream()
                    .map(DetalleVenta::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            venta.setTotal(totalVenta);
            ventaRepository.save(venta);

            return true;
        }
        return false;
    }

    @Transactional
    public List<DetalleVenta> agregarDetallesVenta(Integer ventaId, List<DetalleVentaDTO> detallesDTO) {
        List<DetalleVenta> detallesGuardados = new ArrayList<>();
        for (DetalleVentaDTO dto : detallesDTO) {
            DetalleVenta detalle = agregarDetalleVenta(ventaId, dto.getProductoId(), dto.getCantidad());
            detallesGuardados.add(detalle);
        }
        return detallesGuardados;
    }


}
