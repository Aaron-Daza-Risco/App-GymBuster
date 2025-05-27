package com.version.gymModuloControl.dto;


import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;


@Data
public class RegistroEmpleadoRequest {
    // Datos de Persona
    private String nombre;
    private String apellidos;
    private String genero;
    private String correo;
    private String dni;
    private String celular;
    private LocalDate fechaNacimiento;
    // Datos de Empleado
    private String ruc;
    private BigDecimal salario;
    private LocalDate fechaContratacion;
    private String tipoInstructor; // Usa el nombre del enum como String
    private Integer cupoMaximo;

    public RegistroEmpleadoRequest(
            String nombre,
            String apellidos,
            String genero,
            String correo,
            String dni,
            String celular,
            LocalDate fechaNacimiento,
            String ruc,
            BigDecimal salario,
            LocalDate fechaContratacion,
            String tipoInstructor,
            Integer cupoMaximo
    ) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.genero = genero;
        this.correo = correo;
        this.dni = dni;
        this.celular = celular;
        this.fechaNacimiento = fechaNacimiento;
        this.ruc = ruc;
        this.salario = salario;
        this.fechaContratacion = fechaContratacion;
        this.tipoInstructor = tipoInstructor;
        this.cupoMaximo = cupoMaximo;
    }

}