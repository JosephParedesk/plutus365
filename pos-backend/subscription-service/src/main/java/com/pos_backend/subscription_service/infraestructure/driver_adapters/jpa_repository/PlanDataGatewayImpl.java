package com.pos_backend.subscription_service.infraestructure.driver_adapters.jpa_repository;


import com.pos_backend.subscription_service.domain.model.Plan;
import com.pos_backend.subscription_service.domain.model.gateway.PlanGateway;
import com.pos_backend.subscription_service.infraestructure.mapper.PlanMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PlanDataGatewayImpl implements PlanGateway {

    private final PlanDataJpaRepository planDataJpaRepository;
    private final PlanFeatureJpaRepository planFeatureJpaRepository;
    private final PlanMapper planMapper;

    @Override
    public List<Plan> listarPlanes() {
        return planDataJpaRepository.findByActivoTrue()
                .stream().map(planMapper::toPlan).toList();
    }

    @Override
    public Plan buscarPlanPorId(Long id) {
        return planDataJpaRepository.findById(id)
                .map(planMapper::toPlan).orElse(null);
    }

    @Override
    public Plan guardarPlan(Plan plan) {
        PlanData saved = planDataJpaRepository.save(planMapper.toPlanData(plan));
        return planMapper.toPlan(saved);
    }

    @Override
    public List<String> obtenerModulosPorPlan(Long planId) {
        return planFeatureJpaRepository.findByPlanId(planId)
                .stream()
                .map(PlanFeatureData::getModulo)
                .toList();
    }
}