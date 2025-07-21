package com.version.gymModuloControl.repository;

import com.version.gymModuloControl.model.Plan;
import com.version.gymModuloControl.model.TipoPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PlanRepository extends JpaRepository<Plan, Integer> {

    @Query("SELECT p FROM Plan p")
    List<Plan> listarTodos();

    List<Plan> findByTipoPlan(TipoPlan tipoPlan);
}
