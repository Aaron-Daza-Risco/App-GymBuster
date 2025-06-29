package com.version.gymModuloControl.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.version.gymModuloControl.model.EstadoAlquiler;

import com.version.gymModuloControl.dto.AlquilerConDetalleDTO;
import com.version.gymModuloControl.dto.DetalleAlquilerDTO;
import com.version.gymModuloControl.model.Alquiler;
import com.version.gymModuloControl.model.Cliente;
import com.version.gymModuloControl.model.DetalleAlquiler;
import com.version.gymModuloControl.model.Empleado;
import com.version.gymModuloControl.model.PagoAlquiler;
import com.version.gymModuloControl.model.Pieza;
import com.version.gymModuloControl.repository.AlquilerInterface;
import com.version.gymModuloControl.repository.ClienteRepository;
import com.version.gymModuloControl.repository.EmpleadoRepository;

import jakarta.transaction.Transactional;

@Service
public class AlquilerService {

    @Autowired
    private AlquilerInterface alquilerRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    public AlquilerConDetalleDTO obtenerAlquilerConDetalle(Integer idAlquiler) {
        Alquiler alquiler = alquilerRepository.findById(idAlquiler)
                .orElseThrow(() -> new IllegalArgumentException("Alquiler no encontrado"));

        PagoAlquiler pagoAlquiler = alquiler.getPago();

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
                alquiler.getTotal() != null ? alquiler.getTotal().doubleValue() : 0.0,
                alquiler.getEstado(),
                pagoAlquiler != null ? pagoAlquiler.getIdPago() : null,
                pagoAlquiler != null ? pagoAlquiler.getVuelto() : null,
                pagoAlquiler != null ? pagoAlquiler.getMontoPagado() : null,
                pagoAlquiler != null ? pagoAlquiler.getMetodoPago() : null,
                alquiler.getDetalles().stream().map(detalle -> new DetalleAlquilerDTO(
                        detalle.getIdDetalleAlquiler(),
                        detalle.getPieza().getIdPieza(),
                        detalle.getPieza().getNombre(),
                        detalle.getCantidad(),
                        detalle.getPrecioUnitario().doubleValue(),
                        detalle.getSubtotal().doubleValue()
                )).toList()
        );
    }

    public List<AlquilerConDetalleDTO> listarAlquileresConDetalle() {
        List<Alquiler> alquileres = alquilerRepository.findAll();
        return alquileres.stream()
                .map(alquiler -> obtenerAlquilerConDetalle(alquiler.getIdAlquiler()))
                .toList();
    }

    @Transactional
    public Alquiler guardarAlquiler(Alquiler alquiler) {
        if (alquiler.getCliente() == null || alquiler.getCliente().getIdCliente() == null) {
            throw new IllegalArgumentException("Debe especificar un cliente válido para el alquiler.");
        }

        // Obtener el empleado actual desde la sesión
        String nombreUsuario = SecurityContextHolder.getContext().getAuthentication().getName();
        Empleado empleadoActual = empleadoRepository.findByPersonaUsuarioNombreUsuario(nombreUsuario);
        if (empleadoActual == null) {
            throw new IllegalArgumentException("Empleado no encontrado para el usuario actual.");
        }

        // Verificar que el cliente exista
        Cliente cliente = clienteRepository.findById(alquiler.getCliente().getIdCliente())
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado."));

        alquiler.setCliente(cliente);
        alquiler.setEmpleado(empleadoActual);
        alquiler.setFechaInicio(LocalDate.now());
        
        // Si no se proporciona fecha de fin, se establece por defecto a 7 días después
        if (alquiler.getFechaFin() == null) {
            alquiler.setFechaFin(LocalDate.now().plusDays(7));
        }
        
        alquiler.setEstado(EstadoAlquiler.ACTIVO);

        return alquilerRepository.save(alquiler);
    }

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
        
        // Marcar el alquiler como finalizado (devuelto)
        alquiler.setEstado(EstadoAlquiler.FINALIZADO);
        
        // Guardar los cambios
        return alquilerRepository.save(alquiler);
    }
}
