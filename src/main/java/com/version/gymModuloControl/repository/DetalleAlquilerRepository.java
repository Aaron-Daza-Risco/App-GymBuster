package com.version.gymModuloControl.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.version.gymModuloControl.model.DetalleAlquiler;

@Repository
public interface DetalleAlquilerRepository extends JpaRepository<DetalleAlquiler, Integer> {
    List<DetalleAlquiler> findByAlquiler_IdAlquiler(Integer idAlquiler);
}
