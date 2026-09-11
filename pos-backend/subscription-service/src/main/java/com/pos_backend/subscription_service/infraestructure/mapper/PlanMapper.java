package com.pos_backend.subscription_service.infraestructure.mapper;

import com.pos_backend.subscription_service.domain.model.Plan;
import com.pos_backend.subscription_service.infraestructure.driver_adapters.jpa_repository.PlanData;
import org.springframework.stereotype.Component;

@Component
public class PlanMapper {

    public Plan toPlan(PlanData data) {
        Plan plan = new Plan();
        plan.setId(data.getId());
        plan.setNombre(data.getNombre());
        plan.setDescripcion(data.getDescripcion());
        plan.setPrecio(data.getPrecio());
        plan.setMaxPerfiles(data.getMaxPerfiles());
        plan.setActivo(data.getActivo());
        return plan;
    }

    public PlanData toPlanData(Plan plan) {
        PlanData data = new PlanData();
        data.setId(plan.getId());
        data.setNombre(plan.getNombre());
        data.setDescripcion(plan.getDescripcion());
        data.setPrecio(plan.getPrecio());
        data.setMaxPerfiles(plan.getMaxPerfiles());
        data.setActivo(plan.getActivo());
        return data;
    }
}