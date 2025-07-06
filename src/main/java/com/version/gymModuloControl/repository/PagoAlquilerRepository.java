package com.version.gymModuloControl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.version.gymModuloControl.model.PagoAlquiler;

@Repository
public interface PagoAlquilerRepository extends JpaRepository<PagoAlquiler, Long> {
    
}
