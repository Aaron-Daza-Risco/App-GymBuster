package com.version.gymModuloControl.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.version.gymModuloControl.model.Especialidad;
import com.version.gymModuloControl.repository.EspecialidadRepository;

@Service
public class EspecialidadService {
    
    @Autowired
    private EspecialidadRepository especialidadRepository;

    @Transactional
    public Especialidad guardarEspecialidad(Especialidad especialidad) {
        especialidad.setEstado(true);
        return especialidadRepository.save(especialidad);
    }

    public List<Especialidad> listarTodos() {
        return especialidadRepository.findAll();
    }

    public List<Especialidad> listarPorEstado(Boolean estado) {
        return especialidadRepository.findByEstado(estado);
    }

    @Transactional
    public Especialidad actualizarEspecialidad(Especialidad especialidad) {
        return especialidadRepository.save(especialidad);
    }

    @Transactional
    public Especialidad cambiarEstado(Integer id, Boolean nuevoEstado) {
        Especialidad especialidad = especialidadRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Especialidad no encontrada"));
        
        especialidad.setEstado(nuevoEstado);
        return especialidadRepository.save(especialidad);
    }

    
    /**
     * Devuelve una lista simplificada de especialidades con solo los datos esenciales
     * para evitar problemas de serialización JSON
     */
    public List<Map<String, Object>> listarEspecialidadesBasico() {
        return especialidadRepository.findAll().stream()
            .map(especialidad -> {
                Map<String, Object> esp = new HashMap<>();
                esp.put("id", especialidad.getId());
                esp.put("nombre", especialidad.getNombre());
                esp.put("descripcion", especialidad.getDescripcion());
                esp.put("estado", especialidad.getEstado());
                return esp;
            })
            .collect(Collectors.toList());
    }


    @Transactional
    public boolean eliminarEspecialidad(Integer idEspecialidad) {
        if (especialidadRepository.existsById(idEspecialidad)) {
            especialidadRepository.deleteById(idEspecialidad);
            return true;
        }
        return false;
    }


}
