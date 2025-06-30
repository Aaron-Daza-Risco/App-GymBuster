package com.version.gymModuloControl.auth.dto;

public class ChangePasswordRequest {
    private String contrasenaActual;
    private String nuevaContrasena;

    // Getters y setters
    public String getContrasenaActual() { return contrasenaActual; }
    public void setContrasenaActual(String contrasenaActual) { this.contrasenaActual = contrasenaActual; }
    public String getNuevaContrasena() { return nuevaContrasena; }
    public void setNuevaContrasena(String nuevaContrasena) { this.nuevaContrasena = nuevaContrasena; }
}