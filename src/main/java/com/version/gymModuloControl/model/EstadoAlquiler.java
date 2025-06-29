package com.version.gymModuloControl.model;

/**
 * Enumeración que define los posibles estados de un alquiler.
 */
public enum EstadoAlquiler {
    ACTIVO("Alquiler en uso"),
    FINALIZADO("Se completó y se devolvió"),
    VENCIDO("Ya pasó la fecha de fin, pero no se devolvió"),
    CANCELADO("Se canceló antes de usarse");
    
    private final String descripcion;
    
    EstadoAlquiler(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
}
