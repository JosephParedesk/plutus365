package com.pos_backend.contabilidad.infraestructure.driver_adapters.jpa_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CentroCostoDataJpaRepository extends JpaRepository<CentroCostoData, Long> {
    List<CentroCostoData> findByEmpresaIdOrderByCodigoAsc(String empresaId);
    Optional<CentroCostoData> findByCentroCostoIdAndEmpresaId(Long id, String empresaId);
    boolean existsByCodigoAndEmpresaId(String codigo, String empresaId);
}
