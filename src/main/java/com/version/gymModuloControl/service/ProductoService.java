package com.version.gymModuloControl.service;

import com.version.gymModuloControl.model.Categoria;
import com.version.gymModuloControl.model.Producto;
import com.version.gymModuloControl.repository.CategoriaRepository;
import com.version.gymModuloControl.repository.ProductoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    public List<Producto> listarProductos() {
        return productoRepository.findAll();
    }

    @Transactional
    public Producto guardarProducto(Producto producto) {
        if (producto.getCategoria() == null || producto.getCategoria().getIdCategoria() == null) {
            throw new IllegalArgumentException("Debe especificar una categoría válida para el producto.");
        }

        if (producto.getStockTotal() != null && producto.getStockMinimo() != null &&
                producto.getStockTotal() <= producto.getStockMinimo()) {
            throw new IllegalArgumentException("El stock total debe ser mayor que el stock mínimo para registrar el producto.");
        }

        // Validar que la categoría exista en la base de datos
        Categoria categoria = categoriaRepository.findById(producto.getCategoria().getIdCategoria())
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada."));

        producto.setCategoria(categoria);
        producto.setEstado(true);

        return productoRepository.save(producto);
    }


    @Transactional
    public Producto actualizarProducto(Producto producto) {
        if (producto.getCategoria() == null || producto.getCategoria().getIdCategoria() == null) {
            throw new IllegalArgumentException("Debe especificar una categoría válida para el producto.");
        }

        if (producto.getStockTotal() != null && producto.getStockMinimo() != null &&
                producto.getStockTotal() <= producto.getStockMinimo()) {
            throw new IllegalArgumentException("El stock total debe ser mayor que el stock mínimo para actualizar el producto.");
        }

        // Validar que la categoría exista en la base de datos
        Categoria categoria = categoriaRepository.findById(producto.getCategoria().getIdCategoria())
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada."));

        producto.setCategoria(categoria);
        producto.setEstado(true); // Se asegura que el estado sea activo si pasa la validación

        return productoRepository.save(producto);
    }


    @Transactional
    public Producto cambiarEstadoProducto(Integer idProducto, Boolean estado) {
        Producto producto = productoRepository.findById(idProducto).orElse(null);
        if (producto != null) {
            producto.setEstado(estado);
            return productoRepository.save(producto);
        }
        return null;
    }

    @Transactional
    public boolean eliminarProducto(Integer idProducto) {
        if (productoRepository.existsById(idProducto)) {
            productoRepository.deleteById(idProducto);
            return true;
        }
        return false;
    }
}