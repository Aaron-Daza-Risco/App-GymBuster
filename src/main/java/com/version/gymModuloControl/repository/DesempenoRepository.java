package com.version.gymModuloControl.repository;

import com.version.gymModuloControl.model.Desempeno;
import com.version.gymModuloControl.model.Inscripcion;
import com.version.gymModuloControl.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DesempenoRepository extends JpaRepository<Desempeno, Integer> {
    List<Desempeno> findByInscripcion(Inscripcion inscripcion);
    List<Desempeno> findByCliente(Cliente cliente);
    List<Desempeno> findByClienteAndInscripcion(Cliente cliente, Inscripcion inscripcion);
}
