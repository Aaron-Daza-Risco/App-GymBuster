package com.version.gymModuloControl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.version.gymModuloControl.model.DetalleVenta;

public interface DetalleVentaInterface extends JpaRepository<DetalleVenta, Integer> {
}
