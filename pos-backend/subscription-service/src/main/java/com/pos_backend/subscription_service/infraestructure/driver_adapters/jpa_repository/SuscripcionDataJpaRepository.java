package com.pos_backend.subscription_service.infraestructure.driver_adapters.jpa_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SuscripcionDataJpaRepository extends JpaRepository<SuscripcionData, Long> {
    Optional<SuscripcionData> findByUsuarioCedula(String usuarioCedula);
}