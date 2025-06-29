package com.version.gymModuloControl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.version.gymModuloControl.model.DetalleAlquiler;

import java.util.List;

@Repository
public interface DetalleAlquilerInterface extends JpaRepository<DetalleAlquiler, Integer> {
    List<DetalleAlquiler> findByAlquiler_IdAlquiler(Integer idAlquiler);
}
