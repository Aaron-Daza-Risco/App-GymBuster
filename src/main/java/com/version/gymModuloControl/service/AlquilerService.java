package com.version.gymModuloControl.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.version.gymModuloControl.dto.AlquilerConDetalleDTO;
import com.version.gymModuloControl.dto.DetalleAlquilerDTO;
import com.version.gymModuloControl.model.Alquiler;
import com.version.gymModuloControl.model.Cliente;
import com.version.gymModuloControl.model.Empleado;
import com.version.gymModuloControl.model.PagoAlquiler;
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
        
        alquiler.setEstado(true);

        return alquilerRepository.save(alquiler);
    }

    @Transactional
    public Alquiler cambiarEstadoAlquiler(Integer idAlquiler, Boolean estado) {
        Alquiler alquiler = alquilerRepository.findById(idAlquiler).orElse(null);
        if (alquiler != null) {
            alquiler.setEstado(estado);
            return alquilerRepository.save(alquiler);
        }
        return null;
    }
}
