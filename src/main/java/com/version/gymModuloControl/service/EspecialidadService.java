package com.version.gymModuloControl.service;

import java.util.List;

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
}
