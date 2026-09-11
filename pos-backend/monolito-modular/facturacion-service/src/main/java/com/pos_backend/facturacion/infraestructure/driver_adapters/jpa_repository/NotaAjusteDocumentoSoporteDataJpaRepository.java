package com.pos_backend.facturacion.infraestructure.driver_adapters.jpa_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface NotaAjusteDocumentoSoporteDataJpaRepository extends JpaRepository<NotaAjusteDocumentoSoporteData, Long> {
    List<NotaAjusteDocumentoSoporteData> findByEmpresaIdOrderByNotaAjusteIdDesc(String empresaId);
    List<NotaAjusteDocumentoSoporteData> findByDocumentoSoporteIdAndEmpresaIdOrderByNotaAjusteIdDesc(Long documentoSoporteId, String empresaId);
    Optional<NotaAjusteDocumentoSoporteData> findByNotaAjusteIdAndEmpresaId(Long id, String empresaId);
    void deleteByNotaAjusteIdAndEmpresaId(Long id, String empresaId);
}
