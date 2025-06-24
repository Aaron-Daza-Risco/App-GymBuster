package com.version.gymModuloControl.service;

import com.version.gymModuloControl.dto.DetalleDTO;
import com.version.gymModuloControl.dto.VentaConDetalleDTO;
import com.version.gymModuloControl.model.Cliente;
import com.version.gymModuloControl.model.Empleado;
import com.version.gymModuloControl.model.PagoVenta;
import com.version.gymModuloControl.model.Venta;
import com.version.gymModuloControl.repository.ClienteRepository;
import com.version.gymModuloControl.repository.EmpleadoRepository;
import com.version.gymModuloControl.repository.VentaRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class VentaService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    public VentaConDetalleDTO obtenerVentaConDetalle(Integer idVenta) {
        Venta venta = ventaRepository.findById(idVenta)
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada"));

        PagoVenta pagoVenta = venta.getPagoVenta();

        return new VentaConDetalleDTO(
                venta.getIdVenta(),
                venta.getCliente().getPersona().getNombre(),
                venta.getCliente().getPersona().getApellidos(),
                venta.getCliente().getPersona().getDni(),
                venta.getEmpleado().getPersona().getNombre(),
                venta.getEmpleado().getPersona().getApellidos(),
                venta.getEmpleado().getPersona().getDni(),
                venta.getFecha(),
                venta.getHora(),
                venta.getTotal() != null ? venta.getTotal().doubleValue() : 0.0,
                venta.getEstado(),
                pagoVenta != null ? pagoVenta.getIdPago() : null,
                pagoVenta != null ? pagoVenta.getVuelto() : null,
                pagoVenta != null ? pagoVenta.getMontoPagado() : null,
                pagoVenta != null ? pagoVenta.getMetodoPago() : null,
                venta.getDetallesVenta().stream().map(detalle -> new DetalleDTO(
                        detalle.getIdDetalleVenta(),
                        detalle.getProducto().getIdProducto(),
                        detalle.getProducto().getNombre(),
                        detalle.getCantidad(),
                        detalle.getPrecioUnitario().doubleValue(),
                        detalle.getSubtotal().doubleValue()
                )).toList()
        );
    }
    public List<VentaConDetalleDTO> listarVentasConDetalle() {
        List<Venta> ventas = ventaRepository.findAll();
        return ventas.stream()
                .map(venta -> obtenerVentaConDetalle(venta.getIdVenta()))
                .toList();
    }




    @Transactional
    public Venta guardarVenta(Venta venta) {
        if (venta.getCliente() == null || venta.getCliente().getIdCliente() == null) {
            throw new IllegalArgumentException("Debe especificar un cliente válido para la venta.");
        }

        // Obtener el empleado actual desde la sesión
        String nombreUsuario = SecurityContextHolder.getContext().getAuthentication().getName();
        Empleado empleadoActual = empleadoRepository.findByPersonaUsuarioNombreUsuario(nombreUsuario);
        if (empleadoActual == null) {
            throw new IllegalArgumentException("Empleado no encontrado para el usuario actual.");
        }

        // Verificar que el cliente exista
        Cliente cliente = clienteRepository.findById(venta.getCliente().getIdCliente())
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado."));

        venta.setCliente(cliente);
        venta.setEmpleado(empleadoActual);
        venta.setFecha(LocalDate.now());
        venta.setHora(LocalTime.now());
        venta.setEstado(true);

        return ventaRepository.save(venta);
    }

    @Transactional
    public Venta cambiarEstadoVenta(Integer idVenta, Boolean estado) {
        Venta venta = ventaRepository.findById(idVenta).orElse(null);
        if (venta != null) {
            venta.setEstado(estado);
            return ventaRepository.save(venta);
        }
        return null;
    }
}
