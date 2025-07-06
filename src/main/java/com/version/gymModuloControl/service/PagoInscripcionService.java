package com.version.gymModuloControl.service;

import com.version.gymModuloControl.model.PagoInscripcion;
import com.version.gymModuloControl.model.Inscripcion;
import com.version.gymModuloControl.repository.PagoInscripcionRepository;
import com.version.gymModuloControl.repository.InscripcionRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PagoInscripcionService {

    @Autowired
    private PagoInscripcionRepository pagoInscripcionRepository;

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Transactional
    public PagoInscripcion registrarPago(Integer inscripcionId, BigDecimal montoPagado, String metodoPago) {
        // Validar existencia de la inscripción
        Inscripcion inscripcion = inscripcionRepository.findById(inscripcionId)
                .orElseThrow(() -> new IllegalArgumentException("Inscripción no encontrada con ID: " + inscripcionId));

        if (inscripcion.getMonto() == null) {
            throw new IllegalStateException("La inscripción no tiene un monto definido.");
        }

        if (montoPagado.compareTo(inscripcion.getMonto()) < 0) {
            throw new IllegalArgumentException("El monto pagado es menor al monto de la inscripción.");
        }

        // Calcular vuelto
        BigDecimal vuelto = montoPagado.subtract(inscripcion.getMonto());

        // Crear pago
        PagoInscripcion pago = new PagoInscripcion();
        pago.setInscripcion(inscripcion);
        pago.setMontoPagado(montoPagado);
        pago.setMetodoPago(metodoPago);
        pago.setVuelto(vuelto);
        pago.setEstado(true);

        return pagoInscripcionRepository.save(pago);
    }
}