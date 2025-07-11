package com.version.gymModuloControl.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class DashboardRecepcionistaDTO {
    // Ganancias diarias
    private BigDecimal gananciaDiariaVentasProductos;
    private BigDecimal gananciaDiariaAlquileres;
    private BigDecimal gananciaDiariaInscripciones;

    // Clientes activos
    private int clientesActivos;

    // Últimas inscripciones
    public static class UltimaInscripcionDTO {
        private String nombreCompleto;
        private LocalDate fechaDevolucion;
        private BigDecimal montoPagado;
        // getters y setters
        public String getNombreCompleto() { return nombreCompleto; }
        public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
        public LocalDate getFechaDevolucion() { return fechaDevolucion; }
        public void setFechaDevolucion(LocalDate fechaDevolucion) { this.fechaDevolucion = fechaDevolucion; }
        public BigDecimal getMontoPagado() { return montoPagado; }
        public void setMontoPagado(BigDecimal montoPagado) { this.montoPagado = montoPagado; }
    }
    private List<UltimaInscripcionDTO> ultimasInscripciones;

    // Últimas ventas
    public static class UltimaVentaDTO {
        private String cliente;
        private LocalDate fecha;
        private List<String> productos;
        private List<String> planes;
        // getters y setters
        public String getCliente() { return cliente; }
        public void setCliente(String cliente) { this.cliente = cliente; }
        public LocalDate getFecha() { return fecha; }
        public void setFecha(LocalDate fecha) { this.fecha = fecha; }
        public List<String> getProductos() { return productos; }
        public void setProductos(List<String> productos) { this.productos = productos; }
        public List<String> getPlanes() { return planes; }
        public void setPlanes(List<String> planes) { this.planes = planes; }
    }
    private List<UltimaVentaDTO> ultimasVentas;

    // Clientes que asistieron hoy
    private List<String> clientesAsistieronHoy;

    // getters y setters
    public BigDecimal getGananciaDiariaVentasProductos() { return gananciaDiariaVentasProductos; }
    public void setGananciaDiariaVentasProductos(BigDecimal gananciaDiariaVentasProductos) { this.gananciaDiariaVentasProductos = gananciaDiariaVentasProductos; }
    public BigDecimal getGananciaDiariaAlquileres() { return gananciaDiariaAlquileres; }
    public void setGananciaDiariaAlquileres(BigDecimal gananciaDiariaAlquileres) { this.gananciaDiariaAlquileres = gananciaDiariaAlquileres; }
    public BigDecimal getGananciaDiariaInscripciones() { return gananciaDiariaInscripciones; }
    public void setGananciaDiariaInscripciones(BigDecimal gananciaDiariaInscripciones) { this.gananciaDiariaInscripciones = gananciaDiariaInscripciones; }
    public int getClientesActivos() { return clientesActivos; }
    public void setClientesActivos(int clientesActivos) { this.clientesActivos = clientesActivos; }
    public List<UltimaInscripcionDTO> getUltimasInscripciones() { return ultimasInscripciones; }
    public void setUltimasInscripciones(List<UltimaInscripcionDTO> ultimasInscripciones) { this.ultimasInscripciones = ultimasInscripciones; }
    public List<UltimaVentaDTO> getUltimasVentas() { return ultimasVentas; }
    public void setUltimasVentas(List<UltimaVentaDTO> ultimasVentas) { this.ultimasVentas = ultimasVentas; }
    public List<String> getClientesAsistieronHoy() { return clientesAsistieronHoy; }
    public void setClientesAsistieronHoy(List<String> clientesAsistieronHoy) { this.clientesAsistieronHoy = clientesAsistieronHoy; }
}

