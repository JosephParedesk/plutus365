package com.pos_backend.facturacion.infraestructure.driver_adapters.jpa_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface NotaCreditoDataJpaRepository extends JpaRepository<NotaCreditoData, Long> {
    List<NotaCreditoData> findByEmpresaIdOrderByNotaCreditoIdDesc(String empresaId);
    List<NotaCreditoData> findByFacturaIdAndEmpresaId(Long facturaId, String empresaId);
    Optional<NotaCreditoData> findByNotaCreditoIdAndEmpresaId(Long id, String empresaId);
    long countByEmpresaId(String empresaId);
    void deleteByNotaCreditoIdAndEmpresaId(Long notaCreditoId, String empresaId);
}
