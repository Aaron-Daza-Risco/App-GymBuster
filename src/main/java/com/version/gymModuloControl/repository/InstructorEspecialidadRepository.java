package com.version.gymModuloControl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.version.gymModuloControl.model.Empleado;
import com.version.gymModuloControl.model.InstructorEspecialidad;

public interface InstructorEspecialidadRepository extends JpaRepository<InstructorEspecialidad, Integer> {
    @Modifying
    @Query("DELETE FROM InstructorEspecialidad ie WHERE ie.empleado = :empleado")
    void deleteByEmpleado(@Param("empleado") Empleado empleado);
}
