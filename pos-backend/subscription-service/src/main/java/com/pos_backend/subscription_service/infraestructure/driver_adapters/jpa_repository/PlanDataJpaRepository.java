package com.pos_backend.subscription_service.infraestructure.driver_adapters.jpa_repository;


import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlanDataJpaRepository extends JpaRepository<PlanData, Long> {
    List<PlanData> findByActivoTrue();
}