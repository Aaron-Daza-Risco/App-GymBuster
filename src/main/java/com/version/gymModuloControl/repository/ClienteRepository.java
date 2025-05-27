package com.version.gymModuloControl.repository;

import com.version.gymModuloControl.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    List<Cliente> findByEstado(Boolean estado);
}

