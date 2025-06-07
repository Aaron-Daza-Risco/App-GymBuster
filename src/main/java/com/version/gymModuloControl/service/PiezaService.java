package com.version.gymModuloControl.service;

import com.version.gymModuloControl.model.Pieza;
import com.version.gymModuloControl.repository.PiezaRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PiezaService {

    @Autowired
    private PiezaRepository piezaRepository;

    public List<Pieza> listarPiezas() {
        return piezaRepository.findAll();
    }

    @Transactional
    public Pieza guardarPieza(Pieza pieza) {
        if (pieza.getStock() == null || pieza.getStockMinimo() == null) {
            throw new IllegalArgumentException("Debe especificar el stock total y el stock mínimo para la pieza.");
        }
        if (pieza.getStock() <= pieza.getStockMinimo()) {
            throw new IllegalArgumentException("El stock total debe ser mayor que el stock mínimo para registrar la pieza.");
        }
        pieza.setEstado(true);
        return piezaRepository.save(pieza);
    }

    @Transactional
    public Pieza actualizarPieza(Pieza pieza) {
        if (pieza.getStock() == null || pieza.getStockMinimo() == null) {
            throw new IllegalArgumentException("Debe especificar el stock total y el stock mínimo para la pieza.");
        }
        if (pieza.getStock() <= pieza.getStockMinimo()) {
            throw new IllegalArgumentException("El stock total debe ser mayor que el stock mínimo para actualizar la pieza.");
        }
        pieza.setEstado(true);
        return piezaRepository.save(pieza);
    }

    @Transactional
    public Pieza cambiarEstadoPieza(Integer idPieza, Boolean estado) {
        Pieza pieza = piezaRepository.findById(idPieza).orElse(null);
        if (pieza != null) {
            pieza.setEstado(estado);
            return piezaRepository.save(pieza);
        }
        return null;
    }

    @Transactional
    public boolean eliminarPieza(Integer idPieza) {
        if (piezaRepository.existsById(idPieza)) {
            piezaRepository.deleteById(idPieza);
            return true;
        }
        return false;
    }
}