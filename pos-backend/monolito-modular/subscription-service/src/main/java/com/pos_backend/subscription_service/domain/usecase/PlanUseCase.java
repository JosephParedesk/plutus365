package com.pos_backend.subscription_service.domain.usecase;

import com.pos_backend.subscription_service.domain.model.Plan;
import com.pos_backend.subscription_service.domain.model.gateway.PlanGateway;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
public class PlanUseCase {

    private final PlanGateway planGateway;

    public List<Plan> listarPlanes() {
        return planGateway.listarPlanes();
    }

    public Plan buscarPlanPorId(Long id) {
        if (id == null) {
            throw new RuntimeException("El id del plan es obligatorio");
        }
        Plan plan = planGateway.buscarPlanPorId(id);
        if (plan == null) {
            throw new NoSuchElementException("Plan no encontrado");
        }
        return plan;
    }

    public Plan guardarPlan(Plan plan) {
        if (plan.getNombre() == null || plan.getNombre().trim().isEmpty()) {
            throw new RuntimeException("El nombre del plan es obligatorio");
        }
        if (plan.getPrecio() == null || plan.getPrecio() < 0) {
            throw new RuntimeException("El precio del plan es inválido");
        }
        if (plan.getMaxPerfiles() == null || plan.getMaxPerfiles() < 1) {
            throw new RuntimeException("El máximo de perfiles debe ser al menos 1");
        }
        if (plan.getActivo() == null) {
            plan.setActivo(true);
        }
        return planGateway.guardarPlan(plan);
    }

    public List<String> obtenerModulosPorPlan(Long planId) {
        Plan plan = planGateway.buscarPlanPorId(planId);
        if (plan == null) throw new NoSuchElementException("Plan no encontrado");
        return planGateway.obtenerModulosPorPlan(planId);
    }

    public boolean planTieneAccesoAModulo(Long planId, String modulo) {
        List<String> modulos = planGateway.obtenerModulosPorPlan(planId);
        return modulos.contains(modulo.toUpperCase());
    }
}