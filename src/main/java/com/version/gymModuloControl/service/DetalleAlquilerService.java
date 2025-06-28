package com.version.gymModuloControl.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.version.gymModuloControl.dto.DetalleAlquilerDTO;
import com.version.gymModuloControl.model.Alquiler;
import com.version.gymModuloControl.model.DetalleAlquiler;
import com.version.gymModuloControl.model.Pieza;
import com.version.gymModuloControl.repository.AlquilerInterface;
import com.version.gymModuloControl.repository.DetalleAlquilerInterface;
import com.version.gymModuloControl.repository.PiezaRepository;

import jakarta.transaction.Transactional;

@Service
public class DetalleAlquilerService {

    @Autowired
    private DetalleAlquilerInterface detalleAlquilerRepository;

    @Autowired
    private PiezaRepository piezaRepository;

    @Autowired
    private AlquilerInterface alquilerRepository;

    @Transactional
    public DetalleAlquiler agregarDetalleAlquiler(Integer alquilerId, Integer piezaId, Integer cantidad) {
        // Validar alquiler existente
        Alquiler alquiler = alquilerRepository.findById(alquilerId)
                .orElseThrow(() -> new IllegalArgumentException("Alquiler no encontrado con ID: " + alquilerId));

        // Validar pieza existente
        Pieza pieza = piezaRepository.findById(piezaId)
                .orElseThrow(() -> new IllegalArgumentException("Pieza no encontrada con ID: " + piezaId));

        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
        }

        // Validar stock disponible
        if (pieza.getStock() < cantidad) {
            throw new IllegalArgumentException("Stock insuficiente para la pieza: " + pieza.getNombre());
        }

        // Calcular subtotal
        BigDecimal subtotal = pieza.getPrecioAlquiler().multiply(BigDecimal.valueOf(cantidad));

        // Crear detalle
        DetalleAlquiler detalle = new DetalleAlquiler();
        detalle.setAlquiler(alquiler);
        detalle.setPieza(pieza);
        detalle.setCantidad(cantidad);
        detalle.setPrecioUnitario(pieza.getPrecioAlquiler());
        detalle.setSubtotal(subtotal);

        // Guardar detalle
        detalleAlquilerRepository.save(detalle);

        // Actualizar stock
        pieza.setStock(pieza.getStock() - cantidad);
        piezaRepository.save(pieza);

        // Actualizar total del alquiler
        BigDecimal totalAlquiler = detalleAlquilerRepository.findByAlquiler_IdAlquiler(alquilerId).stream()
                .map(DetalleAlquiler::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        alquiler.setTotal(totalAlquiler);
        alquilerRepository.save(alquiler);

        return detalle;
    }

    public List<DetalleAlquiler> listarDetallesPorAlquilerId(Integer alquilerId) {
        return detalleAlquilerRepository.findByAlquiler_IdAlquiler(alquilerId);
    }

    @Transactional
    public boolean eliminarDetalleAlquiler(Integer detalleId) {
        DetalleAlquiler detalle = detalleAlquilerRepository.findById(detalleId).orElse(null);
        if (detalle != null) {
            // Restaurar stock de la pieza
            Pieza pieza = detalle.getPieza();
            pieza.setStock(pieza.getStock() + detalle.getCantidad());
            piezaRepository.save(pieza);

            // Eliminar detalle
            detalleAlquilerRepository.delete(detalle);

            // Actualizar total del alquiler
            Alquiler alquiler = detalle.getAlquiler();
            BigDecimal totalAlquiler = detalleAlquilerRepository.findByAlquiler_IdAlquiler(alquiler.getIdAlquiler()).stream()
                    .map(DetalleAlquiler::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            alquiler.setTotal(totalAlquiler);
            alquilerRepository.save(alquiler);

            return true;
        }
        return false;
    }

    @Transactional
    public List<DetalleAlquiler> agregarDetallesAlquiler(Integer alquilerId, List<DetalleAlquilerDTO> detallesDTO) {
        List<DetalleAlquiler> detallesGuardados = new ArrayList<>();
        for (DetalleAlquilerDTO dto : detallesDTO) {
            DetalleAlquiler detalle = agregarDetalleAlquiler(alquilerId, dto.getPiezaId(), dto.getCantidad());
            detallesGuardados.add(detalle);
        }
        return detallesGuardados;
    }
}
