package com.pos_backend.contabilidad.infraestructure.driver_adapters.jpa_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CuentaContableDataJpaRepository extends JpaRepository<CuentaContableData, Long> {
    List<CuentaContableData> findByEmpresaIdOrderByCodigoAsc(String empresaId);
    Optional<CuentaContableData> findByEmpresaIdAndCodigo(String empresaId, String codigo);
    List<CuentaContableData> findByEmpresaIdAndCodigoPadre(String empresaId, String codigoPadre);
    boolean existsByEmpresaId(String empresaId);
    boolean existsByEmpresaIdAndCodigo(String empresaId, String codigo);
    void deleteByEmpresaIdAndCodigo(String empresaId, String codigo);
}
