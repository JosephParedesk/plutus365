package com.pos_backend.subscription_service.infraestructure.driver_adapters.jpa_repository;


import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlanFeatureJpaRepository extends JpaRepository<PlanFeatureData, Long> {
    List<PlanFeatureData> findByPlanId(Long planId);
}