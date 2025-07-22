package com.version.gymModuloControl.service;

import com.version.gymModuloControl.dto.DashboardRecepcionistaDTO;
import com.version.gymModuloControl.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
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
    @Autowired
    private EmpleadoRepository empleadoRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;

    public DashboardRecepcionistaDTO getDashboardData(Authentication authentication) {
        DashboardRecepcionistaDTO dto = new DashboardRecepcionistaDTO();
        LocalDate hoy = LocalDate.now();

        // Obtener usuario y empleado autenticado
        String username = authentication.getName();
        var usuarioOpt = usuarioRepository.findByNombreUsuario(username);
        if (usuarioOpt.isEmpty()) return dto;
        var usuario = usuarioOpt.get();
        var empleadoOpt = empleadoRepository.findByPersonaIdPersona(usuario.getPersona().getIdPersona());
        if (empleadoOpt.isEmpty()) return dto;
        var recepcionista = empleadoOpt.get();

        // Ganancia diaria por ventas de productos (solo del recepcionista)
        BigDecimal gananciaVentas = ventaRepository.findAll().stream()
                .filter(v -> Boolean.TRUE.equals(v.getEstado()) && hoy.equals(v.getFecha())
                        && v.getEmpleado() != null
                        && v.getEmpleado().getIdEmpleado().equals(recepcionista.getIdEmpleado()))
                .map(v -> v.getTotal() != null ? v.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setGananciaDiariaVentasProductos(gananciaVentas);

        // Ganancia diaria por alquileres (solo del recepcionista)
        BigDecimal gananciaAlquileres = alquilerRepository.findAll().stream()
                .filter(a -> (
                        a.getEstado() == com.version.gymModuloControl.model.EstadoAlquiler.ACTIVO ||
                                a.getEstado() == com.version.gymModuloControl.model.EstadoAlquiler.FINALIZADO ||
                                a.getEstado() == com.version.gymModuloControl.model.EstadoAlquiler.VENCIDO
                ) && hoy.equals(a.getFechaInicio())
                        && a.getEmpleado() != null
                        && a.getEmpleado().getIdEmpleado().equals(recepcionista.getIdEmpleado()))
                .map(a -> a.getTotal() != null ? a.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setGananciaDiariaAlquileres(gananciaAlquileres);

        // Ganancia diaria por inscripciones (solo del recepcionista)
        BigDecimal gananciaInscripciones = inscripcionRepository.findAll().stream()
                .filter(i -> (
                        i.getEstado() == com.version.gymModuloControl.model.EstadoInscripcion.ACTIVO ||
                                i.getEstado() == com.version.gymModuloControl.model.EstadoInscripcion.FINALIZADO ||
                                i.getEstado() == com.version.gymModuloControl.model.EstadoInscripcion.CANCELADO
                ) && hoy.equals(i.getFechaInscripcion())
                        && i.getRecepcionista() != null
                        && i.getRecepcionista().getIdEmpleado().equals(recepcionista.getIdEmpleado()))
                .map(i -> i.getMonto() != null ? i.getMonto() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setGananciaDiariaInscripciones(gananciaInscripciones);

        // Clientes activos (sin filtro por recepcionista)
        int clientesActivos = (int) clienteRepository.findAll().stream()
                .filter(c -> Boolean.TRUE.equals(c.getEstado()))
                .count();
        dto.setClientesActivos(clientesActivos);

        // Últimas inscripciones (solo del recepcionista)
        var ultimasInscripciones = inscripcionRepository.findAll().stream()
                .filter(i -> i.getEstado() == com.version.gymModuloControl.model.EstadoInscripcion.ACTIVO
                        && i.getRecepcionista() != null
                        && i.getRecepcionista().getIdEmpleado().equals(recepcionista.getIdEmpleado()))
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

        // Últimas ventas (solo del recepcionista)
        var ultimasVentas = ventaRepository.findAll().stream()
                .filter(v -> Boolean.TRUE.equals(v.getEstado())
                        && v.getEmpleado() != null
                        && v.getEmpleado().getIdEmpleado().equals(recepcionista.getIdEmpleado()))
                .sorted((a, b) -> {
                    int cmp = b.getFecha().compareTo(a.getFecha());
                    if (cmp == 0) {
                        return b.getIdVenta().compareTo(a.getIdVenta());
                    }
                    return cmp;
                })
                .limit(5)
                .map(v -> {
                    DashboardRecepcionistaDTO.UltimaVentaDTO venta = new DashboardRecepcionistaDTO.UltimaVentaDTO();
                    venta.setCliente(v.getCliente().getPersona().getNombre() + " " + v.getCliente().getPersona().getApellidos());
                    venta.setFecha(v.getFecha());
                    venta.setProductos(v.getDetallesVenta() != null ? v.getDetallesVenta().stream()
                            .filter(d -> d.getProducto() != null)
                            .map(d -> d.getProducto().getNombre())
                            .toList() : List.of());
                    venta.setPlanes(List.of());
                    return venta;
                })
                .toList();
        dto.setUltimasVentas(ultimasVentas);

        // Clientes que asistieron hoy (sin filtro por recepcionista)
        var clientesAsistieronHoy = asistenciaRepository.findAll().stream()
                .filter(a -> Boolean.TRUE.equals(a.getEstado())
                        && a.getFecha() != null && a.getFecha().equals(hoy)
                        && a.getCliente() != null
                        && Boolean.TRUE.equals(a.getCliente().getEstado()))
                .map(a -> a.getCliente().getPersona().getNombre() + " " + a.getCliente().getPersona().getApellidos())
                .distinct()
                .toList();
        dto.setClientesAsistieronHoy(clientesAsistieronHoy);

        return dto;
    }
}