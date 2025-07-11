package com.version.gymModuloControl.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.version.gymModuloControl.dto.AlquilerCompletoDTO;
import com.version.gymModuloControl.dto.AlquilerConDetalleDTO;
import com.version.gymModuloControl.dto.DetalleAlquilerDTO;
import com.version.gymModuloControl.model.Alquiler;
import com.version.gymModuloControl.model.Cliente;
import com.version.gymModuloControl.model.DetalleAlquiler;
import com.version.gymModuloControl.model.Empleado;
import com.version.gymModuloControl.model.EstadoAlquiler;
import com.version.gymModuloControl.model.PagoAlquiler;
import com.version.gymModuloControl.model.Pieza;
import com.version.gymModuloControl.repository.AlquilerRepository;
import com.version.gymModuloControl.repository.ClienteRepository;
import com.version.gymModuloControl.repository.EmpleadoRepository;
import com.version.gymModuloControl.repository.PagoAlquilerRepository;

@Service
public class AlquilerService {

    @Autowired
    private AlquilerRepository alquilerRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private PagoAlquilerRepository pagoAlquilerRepository;

    @Autowired
    private DetalleAlquilerService detalleAlquilerService;

    @Autowired
    private PagoAlquilerService pagoAlquilerService;

    public AlquilerConDetalleDTO obtenerAlquilerConDetalle(Integer idAlquiler) {
        Alquiler alquiler = alquilerRepository.findById(idAlquiler)
                .orElseThrow(() -> new IllegalArgumentException("Alquiler no encontrado"));

        PagoAlquiler pagoAlquiler = alquiler.getPago();
        
        // Calcular mora si el alquiler está vencido
        double mora = 0.0;
        double total = alquiler.getTotal() != null ? alquiler.getTotal().doubleValue() : 0.0;
        
        if (alquiler.getEstado() == EstadoAlquiler.VENCIDO) {
            long diasRetraso = java.time.temporal.ChronoUnit.DAYS.between(alquiler.getFechaFin(), LocalDate.now());
            mora = diasRetraso * 0.10; // 0.10 soles por día de retraso
        }
        
        double totalConMora = total + mora;

        return new AlquilerConDetalleDTO(
                alquiler.getIdAlquiler(),
                alquiler.getCliente().getPersona().getNombre(),
                alquiler.getCliente().getPersona().getApellidos(),
                alquiler.getCliente().getPersona().getDni(),
                alquiler.getEmpleado().getPersona().getNombre(),
                alquiler.getEmpleado().getPersona().getApellidos(),
                alquiler.getEmpleado().getPersona().getDni(),
                alquiler.getFechaInicio(),
                alquiler.getFechaFin(),
                total,
                mora,
                totalConMora,
                alquiler.getEstado(),
                pagoAlquiler != null ? pagoAlquiler.getIdPago() : null,
                pagoAlquiler != null ? pagoAlquiler.getVuelto() : null,
                pagoAlquiler != null ? pagoAlquiler.getMontoPagado() : null,
                pagoAlquiler != null ? pagoAlquiler.getMetodoPago() : null,
                alquiler.getDetalles().stream().map(detalle -> {
                    // Calcular días del alquiler
                    long diasAlquiler = java.time.temporal.ChronoUnit.DAYS.between(
                        alquiler.getFechaInicio(), alquiler.getFechaFin());
                    if (diasAlquiler <= 0) {
                        diasAlquiler = 1; // Mínimo 1 día
                    }
                    
                    return new DetalleAlquilerDTO(
                        detalle.getIdDetalleAlquiler(),
                        detalle.getPieza().getIdPieza(),
                        detalle.getPieza().getNombre(),
                        detalle.getCantidad(),
                        detalle.getPrecioUnitario().doubleValue(),
                        detalle.getSubtotal().doubleValue(),
                        (int) diasAlquiler
                    );
                }).toList()
        );
    }

    public List<AlquilerConDetalleDTO> listarAlquileresConDetalle() {
        List<Alquiler> alquileres = alquilerRepository.findAll();
        return alquileres.stream()
                .map(alquiler -> obtenerAlquilerConDetalle(alquiler.getIdAlquiler()))
                .toList();
    }

    // El método guardarAlquiler se eliminó por ser redundante con el enfoque de crearAlquilerCompleto

    @Transactional
    public Alquiler cambiarEstadoAlquiler(Integer idAlquiler, EstadoAlquiler nuevoEstado) {
        Alquiler alquiler = alquilerRepository.findById(idAlquiler).orElse(null);
        if (alquiler != null) {
            alquiler.setEstado(nuevoEstado);
            return alquilerRepository.save(alquiler);
        }
        return null;
    }
    
    @Transactional
    public Alquiler finalizarAlquiler(Integer idAlquiler) {
        return cambiarEstadoAlquiler(idAlquiler, EstadoAlquiler.FINALIZADO);
    }
    
    @Transactional
    public Alquiler cancelarAlquiler(Integer idAlquiler) {
        return cambiarEstadoAlquiler(idAlquiler, EstadoAlquiler.CANCELADO);
    }
    
    @Transactional
    public Alquiler marcarVencido(Integer idAlquiler) {
        return cambiarEstadoAlquiler(idAlquiler, EstadoAlquiler.VENCIDO);
    }

    @Transactional
    public Alquiler registrarDevolucion(Integer idAlquiler) {
        // Buscar el alquiler
        Alquiler alquiler = alquilerRepository.findById(idAlquiler)
                .orElseThrow(() -> new IllegalArgumentException("Alquiler no encontrado con ID: " + idAlquiler));
        
        // Verificar que el alquiler esté activo o vencido
        if (alquiler.getEstado() == EstadoAlquiler.FINALIZADO || alquiler.getEstado() == EstadoAlquiler.CANCELADO) {
            throw new IllegalStateException("No se puede procesar la devolución de un alquiler ya finalizado o cancelado");
        }
        
        // Obtener los detalles del alquiler para actualizar el inventario
        List<DetalleAlquiler> detalles = alquiler.getDetalles();
        
        // Actualizar el stock de cada pieza
        for (DetalleAlquiler detalle : detalles) {
            Pieza pieza = detalle.getPieza();
            pieza.setStock(pieza.getStock() + detalle.getCantidad());
            // No necesitamos guardar la pieza aquí, se guardará automáticamente con la transacción
        }
        
        // Si el alquiler está vencido, calcular y agregar la mora al total
        if (alquiler.getEstado() == EstadoAlquiler.VENCIDO) {
            long diasRetraso = java.time.temporal.ChronoUnit.DAYS.between(alquiler.getFechaFin(), LocalDate.now());
            BigDecimal mora = new BigDecimal("0.10").multiply(new BigDecimal(diasRetraso));
            BigDecimal totalConMora = alquiler.getTotal().add(mora);
            alquiler.setMora(mora);
            alquiler.setTotal(totalConMora);
        }
        
        // Marcar el alquiler como finalizado (devuelto)
        alquiler.setEstado(EstadoAlquiler.FINALIZADO);
        
        // Guardar los cambios
        return alquilerRepository.save(alquiler);
    }

    // Método para crear un alquiler completo en una sola transacción
    @Transactional
    public AlquilerConDetalleDTO crearAlquilerCompleto(AlquilerCompletoDTO alquilerCompletoDTO) {
        // 1. Obtener el empleado actual
        Empleado empleadoActual = obtenerEmpleadoActual();

        // 2. Validar cliente
        Cliente cliente = clienteRepository.findById(alquilerCompletoDTO.getClienteId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));

        // 3. Preparar el alquiler
        Alquiler alquiler = new Alquiler();
        alquiler.setCliente(cliente);
        alquiler.setEmpleado(empleadoActual);

        LocalDate hoy = LocalDate.now();
        alquiler.setFechaInicio(hoy);

        LocalDate fechaFin = alquilerCompletoDTO.getFechaFin();
        if (fechaFin == null || fechaFin.isBefore(hoy) || fechaFin.isEqual(hoy)) {
            fechaFin = hoy.plusDays(7);
        }

        long diasAlquiler = ChronoUnit.DAYS.between(hoy, fechaFin) + 1;  // Incluye el día de inicio
        if (diasAlquiler > 30) {
            throw new IllegalArgumentException("El período de alquiler no puede exceder los 30 días");
        }
        if (diasAlquiler < 1) {
            throw new IllegalArgumentException("El período de alquiler debe ser de al menos 1 día");
        }

        alquiler.setFechaFin(fechaFin);
        alquiler.setEstado(EstadoAlquiler.ACTIVO);

        // 4. Guardar el alquiler
        alquiler = alquilerRepository.save(alquiler);

        // 5. Procesar y guardar los detalles
        List<DetalleAlquiler> detalles = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (DetalleAlquilerDTO detalleDTO : alquilerCompletoDTO.getDetalles()) {
            DetalleAlquiler detalle = detalleAlquilerService.agregarDetalleAlquiler(
                    alquiler.getIdAlquiler(),
                    detalleDTO.getPiezaId(),
                    detalleDTO.getCantidad()
            );

            // Calcular el subtotal manualmente aquí (precio × cantidad × días)
            BigDecimal precioUnitario = detalle.getPieza().getPrecioAlquiler();
            BigDecimal subtotal = precioUnitario
                    .multiply(BigDecimal.valueOf(detalle.getCantidad()))
                    .multiply(BigDecimal.valueOf(diasAlquiler));
            detalle.setSubtotal(subtotal);

            detalles.add(detalle);
            total = total.add(subtotal);
        }

        alquiler.setDetalles(detalles);
        alquiler.setTotal(total);

        // 6. Registrar el pago (opcional)
        if (alquilerCompletoDTO.getMontoPagado() != null &&
                alquilerCompletoDTO.getMetodoPago() != null &&
                !alquilerCompletoDTO.getMetodoPago().isEmpty()) {

            PagoAlquiler pago = pagoAlquilerService.registrarPago(
                    alquiler.getIdAlquiler(),
                    alquilerCompletoDTO.getMontoPagado(),
                    alquilerCompletoDTO.getMetodoPago()
            );
            alquiler.setPago(pago);
        }

        // 7. Guardar los cambios finales del alquiler
        alquiler = alquilerRepository.save(alquiler);

        // 8. Devolver DTO con todo
        return obtenerAlquilerConDetalle(alquiler.getIdAlquiler());
    }

    private Empleado obtenerEmpleadoActual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No hay usuario autenticado");
        }
        String username = authentication.getName();
        Empleado empleadoActual = empleadoRepository.findByPersonaUsuarioNombreUsuario(username);
        if (empleadoActual == null) {
            throw new RuntimeException("No se encontró el empleado para el usuario: " + username);
        }
        return empleadoActual;
    }
}
