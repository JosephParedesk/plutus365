package com.pos_backend.venta.infraestructure.driver_adapters.jpa_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RemisionDataJpaRepository extends JpaRepository<RemisionData, Long> {
    List<RemisionData> findByEmpresaIdOrderByRemisionIdDesc(String empresaId);
    Optional<RemisionData> findByRemisionIdAndEmpresaId(Long id, String empresaId);
    List<RemisionData> findByEmpresaIdAndFechaBetween(String empresaId, LocalDateTime desde, LocalDateTime hasta);
    long countByEmpresaId(String empresaId);
}
