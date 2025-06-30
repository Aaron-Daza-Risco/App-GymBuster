package com.version.gymModuloControl.service;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.version.gymModuloControl.model.Alquiler;
import com.version.gymModuloControl.model.EstadoAlquiler;
import com.version.gymModuloControl.repository.AlquilerRepository;

import jakarta.transaction.Transactional;

@Service
public class AlquilerSchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(AlquilerSchedulerService.class);

    @Autowired
    private AlquilerRepository alquilerRepository;
    
    /**
     * Tarea programada para revisar alquileres vencidos.
     * Se ejecuta cada minuto
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    @Transactional
    public void actualizarAlquileresVencidos() {
        logger.info("Iniciando tarea programada para actualizar alquileres vencidos");
        
        LocalDate fechaActual = LocalDate.now();
        
        // Buscar todos los alquileres activos cuya fecha de fin sea anterior a hoy
        List<Alquiler> alquileresVencidos = alquilerRepository.findByEstadoAndFechaFinBefore(
                EstadoAlquiler.ACTIVO, fechaActual);
        
        logger.info("Se encontraron {} alquileres vencidos para actualizar", alquileresVencidos.size());
        
        // Actualizar el estado de cada alquiler vencido
        for (Alquiler alquiler : alquileresVencidos) {
            alquiler.setEstado(EstadoAlquiler.VENCIDO);
            alquilerRepository.save(alquiler);
            logger.info("Alquiler ID {} marcado como VENCIDO", alquiler.getIdAlquiler());
        }
        
        logger.info("Tarea de actualización de alquileres vencidos completada");
    }
    
    /**
     * Método para ejecutar la verificación bajo demanda (para pruebas o ejecución manual)
     */
    @Transactional
    public int verificarYActualizarAlquileresVencidos() {
        LocalDate fechaActual = LocalDate.now();
        
        // Buscar todos los alquileres activos cuya fecha de fin sea anterior a hoy
        List<Alquiler> alquileresVencidos = alquilerRepository.findByEstadoAndFechaFinBefore(
                EstadoAlquiler.ACTIVO, fechaActual);
        
        // Actualizar el estado de cada alquiler vencido
        for (Alquiler alquiler : alquileresVencidos) {
            alquiler.setEstado(EstadoAlquiler.VENCIDO);
            alquilerRepository.save(alquiler);
        }
        
        return alquileresVencidos.size();
    }
}
