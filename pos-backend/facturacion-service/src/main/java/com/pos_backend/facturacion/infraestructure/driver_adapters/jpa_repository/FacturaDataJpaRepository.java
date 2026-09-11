package com.pos_backend.facturacion.infraestructure.driver_adapters.jpa_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FacturaDataJpaRepository extends JpaRepository<FacturaData, Long> {
    Optional<FacturaData> findByFacturaIdAndEmpresaId(Long facturaId, String empresaId);
    Optional<FacturaData> findFirstByVentaIdAndEmpresaIdOrderByFacturaIdDesc(Long ventaId, String empresaId);
    List<FacturaData> findByEmpresaId(String empresaId);
    void deleteByFacturaIdAndEmpresaId(Long facturaId, String empresaId);
}
