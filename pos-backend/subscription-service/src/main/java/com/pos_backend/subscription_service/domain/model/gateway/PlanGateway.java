package com.pos_backend.subscription_service.domain.model.gateway;

import com.pos_backend.subscription_service.domain.model.Plan;

import java.util.List;

public interface PlanGateway {
    List<Plan> listarPlanes();
    Plan buscarPlanPorId(Long id);
    Plan guardarPlan(Plan plan);
    List<String> obtenerModulosPorPlan(Long planId);
}