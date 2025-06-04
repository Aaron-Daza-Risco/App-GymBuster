package com.version.gymModuloControl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.version.gymModuloControl.model.PagoVenta;

import java.util.Optional;

public interface PagoVentaRepository extends JpaRepository<PagoVenta, Integer> {

    Optional<PagoVenta> findByVenta_IdVenta(Integer idVenta);

}
