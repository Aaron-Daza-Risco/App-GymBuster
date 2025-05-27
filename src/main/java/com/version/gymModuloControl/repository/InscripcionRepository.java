package com.version.gymModuloControl.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.version.gymModuloControl.model.Inscripcion;

public interface InscripcionRepository extends JpaRepository<Inscripcion, Integer> {
    // Cambiar esto
    List<Inscripcion> findByClienteIdCliente(Integer clienteId);
    
    // Cambiar esto
    List<Inscripcion> findByRecepcionistaIdEmpleado(Integer recepcionistaId);
}
