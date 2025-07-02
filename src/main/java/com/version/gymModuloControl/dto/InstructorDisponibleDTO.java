package com.version.gymModuloControl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InstructorDisponibleDTO {
    private Integer idEmpleado;
    private String nombreCompleto;
    private String tipoInstructor;
    private Integer cupoMaximo;
}
