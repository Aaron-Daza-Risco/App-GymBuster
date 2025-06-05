package com.version.gymModuloControl.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.version.gymModuloControl.model.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    boolean existsByCategoria_IdCategoria(Integer idCategoria);

}
