package com.version.gymModuloControl.service;

import com.version.gymModuloControl.dto.DashboardRecepcionistaDTO;
import com.version.gymModuloControl.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class DashboardRecepcionistaService {
    @Autowired
    private VentaRepository ventaRepository;
    @Autowired
    private AlquilerRepository alquilerRepository;
    @Autowired
    private InscripcionRepository inscripcionRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private AsistenciaRepository asistenciaRepository;

    public DashboardRecepcionistaDTO getDashboardData() {
        DashboardRecepcionistaDTO dto = new DashboardRecepcionistaDTO();
        LocalDate hoy = LocalDate.now();

        // Ganancia diaria por ventas de productos
        BigDecimal gananciaVentas = ventaRepository.findAll().stream()
                .filter(v -> Boolean.TRUE.equals(v.getEstado()) && hoy.equals(v.getFecha()))
                .map(v -> v.getTotal() != null ? v.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setGananciaDiariaVentasProductos(gananciaVentas);

        // Ganancia diaria por alquileres
        BigDecimal gananciaAlquileres = alquilerRepository.findAll().stream()
                .filter(a -> (
                        a.getEstado() == com.version.gymModuloControl.model.EstadoAlquiler.ACTIVO ||
                                a.getEstado() == com.version.gymModuloControl.model.EstadoAlquiler.FINALIZADO ||
                                a.getEstado() == com.version.gymModuloControl.model.EstadoAlquiler.VENCIDO
                ) && hoy.equals(a.getFechaInicio()))
                .map(a -> a.getTotal() != null ? a.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setGananciaDiariaAlquileres(gananciaAlquileres);

        // Ganancia diaria por inscripciones (ACTIVO, FINALIZADO, CANCELADO)
        BigDecimal gananciaInscripciones = inscripcionRepository.findAll().stream()
                .filter(i -> (
                        i.getEstado() == com.version.gymModuloControl.model.EstadoInscripcion.ACTIVO ||
                                i.getEstado() == com.version.gymModuloControl.model.EstadoInscripcion.FINALIZADO ||
                                i.getEstado() == com.version.gymModuloControl.model.EstadoInscripcion.CANCELADO
                ) && hoy.equals(i.getFechaInscripcion()))
                .map(i -> i.getMonto() != null ? i.getMonto() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setGananciaDiariaInscripciones(gananciaInscripciones);

        // Clientes activos
        int clientesActivos = (int) clienteRepository.findAll().stream()
                .filter(c -> Boolean.TRUE.equals(c.getEstado()))
                .count();
        dto.setClientesActivos(clientesActivos);

        // Últimas inscripciones (últimos 5)
        var ultimasInscripciones = inscripcionRepository.findAll().stream()
                .filter(i -> i.getEstado() == com.version.gymModuloControl.model.EstadoInscripcion.ACTIVO)
                .sorted((a, b) -> b.getFechaInscripcion().compareTo(a.getFechaInscripcion()))
                .limit(5)
                .map(i -> {
                    DashboardRecepcionistaDTO.UltimaInscripcionDTO insc = new DashboardRecepcionistaDTO.UltimaInscripcionDTO();
                    insc.setNombreCompleto(i.getCliente().getPersona().getNombre() + " " + i.getCliente().getPersona().getApellidos());
                    insc.setFechaDevolucion(i.getFechaFin());
                    insc.setMontoPagado(i.getMonto() != null ? i.getMonto() : java.math.BigDecimal.ZERO);
                    return insc;
                })
                .toList();
        dto.setUltimasInscripciones(ultimasInscripciones);

        // Últimas ventas (últimos 5)
        var ultimasVentas = ventaRepository.findAll().stream()
                .filter(v -> Boolean.TRUE.equals(v.getEstado()))
                .sorted((a, b) -> b.getFecha().compareTo(a.getFecha()))
                .limit(5)
                .map(v -> {
                    DashboardRecepcionistaDTO.UltimaVentaDTO venta = new DashboardRecepcionistaDTO.UltimaVentaDTO();
                    venta.setCliente(v.getCliente().getPersona().getNombre() + " " + v.getCliente().getPersona().getApellidos());
                    venta.setFecha(v.getFecha());
                    venta.setProductos(v.getDetallesVenta() != null ? v.getDetallesVenta().stream()
                            .filter(d -> d.getProducto() != null)
                            .map(d -> d.getProducto().getNombre())
                            .toList() : List.of());
                    // No hay planes en DetalleVenta, así que dejamos la lista vacía
                    venta.setPlanes(List.of());
                    return venta;
                })
                .toList();
        dto.setUltimasVentas(ultimasVentas);

// Clientes que asistieron hoy (solo asistencias activas y clientes activos)
        var clientesAsistieronHoy = asistenciaRepository.findAll().stream()
                .filter(a -> Boolean.TRUE.equals(a.getEstado()) // solo asistencias activas
                        && a.getFecha() != null && a.getFecha().equals(hoy)
                        && a.getCliente() != null
                        && Boolean.TRUE.equals(a.getCliente().getEstado())) // solo clientes activos
                .map(a -> a.getCliente().getPersona().getNombre() + " " + a.getCliente().getPersona().getApellidos())
                .distinct()
                .toList();
        dto.setClientesAsistieronHoy(clientesAsistieronHoy);

        return dto;
    }
}
