package com.version.gymModuloControl.service;

import java.math.BigDecimal;
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
    private static final BigDecimal MORA_POR_DIA = new BigDecimal("0.10");

    @Scheduled(cron = "0 0/1 * * * ?")
    @Transactional
    public void actualizarAlquileresVencidos() {
        logger.info("Iniciando tarea programada para actualizar alquileres vencidos");
        
        LocalDate fechaActual = LocalDate.now();
        
        // Buscar todos los alquileres activos cuya fecha de fin sea anterior a hoy
        List<Alquiler> alquileresVencidos = alquilerRepository.findByEstadoAndFechaFinBefore(
                EstadoAlquiler.ACTIVO, fechaActual);
        
        logger.info("Se encontraron {} alquileres vencidos para actualizar", alquileresVencidos.size());
        
        // Actualizar el estado de cada alquiler vencido y calcular mora
        for (Alquiler alquiler : alquileresVencidos) {
            // Calcular días de retraso
            long diasRetraso = java.time.temporal.ChronoUnit.DAYS.between(alquiler.getFechaFin(), fechaActual);
            
            // Calcular mora (0.10 soles por día de retraso)
            BigDecimal mora = MORA_POR_DIA.multiply(new BigDecimal(diasRetraso));
            
            alquiler.setEstado(EstadoAlquiler.VENCIDO);
            alquiler.setMora(mora);
            alquilerRepository.save(alquiler);
            
            logger.info("Alquiler ID {} marcado como VENCIDO con mora de S/. {}", 
                alquiler.getIdAlquiler(), mora);
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
