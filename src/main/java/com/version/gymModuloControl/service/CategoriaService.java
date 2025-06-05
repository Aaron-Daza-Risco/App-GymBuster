package com.version.gymModuloControl.service;
import com.version.gymModuloControl.repository.ProductoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import com.version.gymModuloControl.model.Categoria;
import com.version.gymModuloControl.repository.CategoriaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProductoRepository productoRepository;
    public List<Categoria> listarCategoria() {
        return categoriaRepository.findAll();
    }



    @Transactional
    public Categoria guardarCategoria(Categoria categoria) {
        return categoriaRepository.save(categoria);
    }

    @Transactional
    public Categoria actualizarCategoria(Categoria categoria) {
        return categoriaRepository.save(categoria);
    }

    @Transactional
    public Categoria cambiarEstadoCategoria(Integer idCategoria, Boolean estado) {
        Categoria categoria = categoriaRepository.findById(idCategoria).orElse(null);
        if (categoria != null) {
            categoria.setEstado(estado);
            return categoriaRepository.save(categoria);
        }
        return null;
    }


    @Transactional
    public boolean eliminarCategoria(Integer idCategoria) {
        boolean existeEnProducto = productoRepository.existsByCategoria_IdCategoria(idCategoria);
        if (existeEnProducto) {
            throw new IllegalStateException("No se puede eliminar la categoría porque está asociada a un producto.");
        }
        if (categoriaRepository.existsById(idCategoria)) {
            categoriaRepository.deleteById(idCategoria);
            return true;
        }
        return false;
    }

}
