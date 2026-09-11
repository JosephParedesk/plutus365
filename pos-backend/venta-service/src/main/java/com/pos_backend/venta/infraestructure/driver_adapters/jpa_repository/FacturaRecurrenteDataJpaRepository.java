package com.pos_backend.venta.infraestructure.driver_adapters.jpa_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FacturaRecurrenteDataJpaRepository extends JpaRepository<FacturaRecurrenteData, Long> {
    List<FacturaRecurrenteData> findByEmpresaIdOrderByProximaGeneracionAsc(String empresaId);
    Optional<FacturaRecurrenteData> findByRecurrenteIdAndEmpresaId(Long id, String empresaId);
}
