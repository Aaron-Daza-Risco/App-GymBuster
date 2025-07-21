package com.version.gymModuloControl.repository;

import java.time.LocalDate;
import java.util.List;

import com.version.gymModuloControl.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import com.version.gymModuloControl.model.Asistencia;

public interface AsistenciaRepository extends JpaRepository<Asistencia, Integer> {
    List<Asistencia> findByClienteIdCliente(Integer clienteId);
    boolean existsByClienteAndFecha(Cliente cliente, LocalDate fecha);
}