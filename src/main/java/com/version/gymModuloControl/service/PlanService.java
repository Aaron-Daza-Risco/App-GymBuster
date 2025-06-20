package com.version.gymModuloControl.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.version.gymModuloControl.model.Plan;
import com.version.gymModuloControl.repository.InscripcionRepository;
import com.version.gymModuloControl.repository.PlanRepository;

@Service
public class PlanService {
    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private PlanRepository planRepository;

    @Transactional
    public Plan guardarPlan(Plan plan) {
        return planRepository.save(plan);
    }

    public List<Plan> listarTodos() {
        return planRepository.listarTodos();
    }

    @Transactional
    public Plan actualizarPlan(Plan plan) {
        if (plan.getIdPlan() == null || !planRepository.existsById(plan.getIdPlan())) {
            throw new IllegalArgumentException("El plan no existe.");
        }
        return planRepository.save(plan);
    }

    @Transactional
    public Plan cambiarEstadoPlan(Integer idPlan, Boolean estado) {
        Plan plan = planRepository.findById(idPlan).orElse(null);
        if (plan != null) {
            plan.setEstado(estado);
            return planRepository.save(plan);
        }
        return null;
    }

    @Transactional
    public boolean eliminarPlan(Integer idPlan) {
        boolean existeEnInscripcion = inscripcionRepository.existsByPlan_IdPlan(idPlan);
        if (existeEnInscripcion) {
            throw new IllegalStateException("No se puede eliminar el plan porque está asociado a una inscripción.");
        }
        if (planRepository.existsById(idPlan)) {
            planRepository.deleteById(idPlan);
            return true;
        }
        return false;
    }

}