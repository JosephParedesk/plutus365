package com.pos_backend.contabilidad.infraestructure.driver_adapters.jpa_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AsientoContableDataJpaRepository extends JpaRepository<AsientoContableData, Long> {
    List<AsientoContableData> findByEmpresaIdOrderByFechaDescAsientoIdDesc(String empresaId);
    Optional<AsientoContableData> findByAsientoIdAndEmpresaId(Long asientoId, String empresaId);
    Optional<AsientoContableData> findByOrigenAndReferenciaIdAndEmpresaId(String origen, Long referenciaId, String empresaId);
    long countByEmpresaId(String empresaId);
}
