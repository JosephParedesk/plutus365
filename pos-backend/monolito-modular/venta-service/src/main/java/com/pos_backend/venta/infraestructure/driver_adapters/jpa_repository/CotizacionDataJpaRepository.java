package com.pos_backend.venta.infraestructure.driver_adapters.jpa_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CotizacionDataJpaRepository extends JpaRepository<CotizacionData, Long> {
    List<CotizacionData> findByEmpresaIdOrderByCotizacionIdDesc(String empresaId);
    Optional<CotizacionData> findByCotizacionIdAndEmpresaId(Long id, String empresaId);
    List<CotizacionData> findByEmpresaIdAndFechaBetween(String empresaId, LocalDateTime desde, LocalDateTime hasta);
    long countByEmpresaId(String empresaId);
}
