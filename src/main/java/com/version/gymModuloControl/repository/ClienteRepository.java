package com.version.gymModuloControl.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.version.gymModuloControl.model.Cliente;


public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    List<Cliente> findByEstado(Boolean estado);
}

