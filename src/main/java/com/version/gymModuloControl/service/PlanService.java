package com.version.gymModuloControl.service;

import com.version.gymModuloControl.model.Plan;
import com.version.gymModuloControl.repository.PlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PlanService {

    @Autowired
    private PlanRepository planRepository;

    @Transactional
    public Plan guardarPlan(Plan plan) {
        return planRepository.save(plan);
    }

    public List<Plan> listarTodos() {
        return planRepository.listarTodos();
    }
}