package com.version.gymModuloControl.repository;

import com.version.gymModuloControl.model.PagoInscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PagoInscripcionRepository extends JpaRepository<PagoInscripcion, Integer> {
}