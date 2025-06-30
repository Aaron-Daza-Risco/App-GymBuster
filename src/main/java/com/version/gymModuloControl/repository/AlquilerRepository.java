package com.version.gymModuloControl.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.version.gymModuloControl.model.Alquiler;
import com.version.gymModuloControl.model.EstadoAlquiler;

public interface AlquilerRepository extends JpaRepository<Alquiler, Integer> {
    /**
     * Encuentra alquileres por estado y con fecha de fin anterior a la fecha especificada
     * @param estado El estado del alquiler a buscar
     * @param fecha La fecha límite antes de la cual buscar
     * @return Lista de alquileres que cumplen con el criterio
     */
    List<Alquiler> findByEstadoAndFechaFinBefore(EstadoAlquiler estado, LocalDate fecha);
}
