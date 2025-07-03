package com.version.gymModuloControl.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.version.gymModuloControl.model.Alquiler;
import com.version.gymModuloControl.model.PagoAlquiler;
import com.version.gymModuloControl.repository.AlquilerRepository;
import com.version.gymModuloControl.repository.PagoAlquilerRepository;

import jakarta.transaction.Transactional;

@Service
public class PagoAlquilerService {
    @Autowired
    private PagoAlquilerRepository pagoAlquilerRepository;

    @Autowired
    private AlquilerRepository alquilerRepository;

    @Transactional
    public PagoAlquiler registrarPago(Integer alquilerId, BigDecimal montoPagado, String metodoPago) {
        // Validar existencia del alquiler
        Alquiler alquiler = alquilerRepository.findById(alquilerId)
                .orElseThrow(() -> new IllegalArgumentException("Alquiler no encontrado con ID: " + alquilerId));

        if (alquiler.getTotal() == null) {
            throw new IllegalStateException("El alquiler no tiene un total definido.");
        }

        if (montoPagado.compareTo(alquiler.getTotal()) < 0) {
            throw new IllegalArgumentException("El monto pagado es menor al total del alquiler.");
        }

        // Calcular vuelto
        BigDecimal vuelto = montoPagado.subtract(alquiler.getTotal());

        // Crear pago
        PagoAlquiler pago = new PagoAlquiler();
        pago.setAlquiler(alquiler);
        pago.setMontoPagado(montoPagado);
        pago.setMetodoPago(metodoPago);
        pago.setVuelto(vuelto);
        pago.setEstado(true);

        return pagoAlquilerRepository.save(pago);
    }
}
