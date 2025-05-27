package com.version.gymModuloControl.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.version.gymModuloControl.model.Asistencia;

public interface AsistenciaRepository extends JpaRepository<Asistencia, Integer> {
    List<Asistencia> findByClienteIdCliente(Integer clienteId);
}

