package com.version.gymModuloControl.auth.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class RegisterRequest {
    // Datos de Usuario
    private String nombreUsuario;
    private String contrasena;
    private String rol;
    
    // Datos de Persona
    private String nombre;
    private String apellidos;
    private String genero;
    private String correo;
    private String dni;
    private String celular;
    private LocalDate fechaNacimiento;
    
    // Datos específicos de Cliente
    private String direccion;
    
    // Datos específicos de Empleado
    private String ruc;
    private BigDecimal salario;
    private LocalDate fechaContratacion;
    
    // Datos específicos de Entrenador
    private String tipoInstructor; // ESTANDAR o PREMIUM
    private Integer cupoMaximo;
    private List<Integer> especialidadesIds;
    
    public String getNombreUsuario() {
        return nombreUsuario;
    }
    
    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }
    
    public String getContrasena() {
        return contrasena;
    }
    
    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
    
    public String getRol() {
        return rol;
    }
    
    public void setRol(String rol) {
        this.rol = rol;
    }
}
