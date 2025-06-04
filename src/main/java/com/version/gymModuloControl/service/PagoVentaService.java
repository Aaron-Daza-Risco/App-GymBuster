package com.version.gymModuloControl.service;

import com.version.gymModuloControl.model.PagoVenta;
import com.version.gymModuloControl.model.Venta;
import com.version.gymModuloControl.repository.PagoVentaRepository;
import com.version.gymModuloControl.repository.VentaRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PagoVentaService {

    @Autowired
    private PagoVentaRepository pagoVentaRepository;

    @Autowired
    private VentaRepository ventaRepository;

    @Transactional
    public PagoVenta registrarPago(Integer ventaId, BigDecimal montoPagado, String metodoPago) {
        // Validar existencia de la venta
        Venta venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada con ID: " + ventaId));

        if (venta.getTotal() == null) {
            throw new IllegalStateException("La venta no tiene un total definido.");
        }

        if (montoPagado.compareTo(venta.getTotal()) < 0) {
            throw new IllegalArgumentException("El monto pagado es menor al total de la venta.");
        }

        // Calcular vuelto
        BigDecimal vuelto = montoPagado.subtract(venta.getTotal());

        // Crear pago
        PagoVenta pago = new PagoVenta();
        pago.setVenta(venta);
        pago.setMontoPagado(montoPagado);
        pago.setMetodoPago(metodoPago);
        pago.setVuelto(vuelto);
        pago.setEstado(true);

        return pagoVentaRepository.save(pago);
    }


}
