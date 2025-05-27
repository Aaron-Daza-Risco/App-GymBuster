// src/main/java/com/version/gymModuloControl/dto/RegistroClienteRequest.java
package com.version.gymModuloControl.dto;

import lombok.Data;

import java.time.LocalDate;



@Data
public class RegistroClienteRequest {
    // Datos de Persona
    private String nombre;
    private String apellidos;
    private String genero;
    private String correo;
    private String dni;
    private String celular;
    private LocalDate fechaNacimiento;
    private String direccion;
}