package com.version.gymModuloControl.repository;

import java.util.List;
import java.util.Optional;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.version.gymModuloControl.model.Persona;

public interface PersonaRepository extends JpaRepository<Persona, Integer> {
    Optional<Persona> findByCorreo(String correo);
    Optional<Persona> findByDni(String dni);
    List<Persona> findByNombreContainingOrApellidosContaining(String nombre, String apellidos);
    Optional<Persona> findByUsuarioId(Integer usuarioId);
    
    @Query(value = """
        SELECT COUNT(DISTINCT p.id_persona) 
        FROM persona p 
        JOIN cliente c ON p.id_persona = c.persona_id 
        WHERE c.estado = true
        """, nativeQuery = true)
    Long countClientesActivos();
    
    @Query(value = """
        SELECT 
            DATE_FORMAT(c.fecha_registro, '%Y-%m') as mes,
            MONTHNAME(c.fecha_registro) as nombreMes,
            COUNT(*) as nuevosClientes
        FROM persona p
        JOIN cliente c ON p.id_persona = c.persona_id
        WHERE c.estado = true
        AND c.fecha_registro >= DATE_SUB(CURRENT_DATE, INTERVAL 6 MONTH)
        GROUP BY mes, nombreMes
        ORDER BY mes DESC
        """, nativeQuery = true)
    List<Map<String, Object>> getNuevosClientesPorMes();
}

