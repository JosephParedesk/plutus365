package com.pos_backend.facturacion.infraestructure.driver_adapters.jpa_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DocumentoSoporteDataJpaRepository extends JpaRepository<DocumentoSoporteData, Long> {
    List<DocumentoSoporteData> findByEmpresaIdOrderByDocumentoSoporteIdDesc(String empresaId);
    Optional<DocumentoSoporteData> findByDocumentoSoporteIdAndEmpresaId(Long id, String empresaId);
    // Puede haber varios intentos en ERROR por compra — se toma el último, igual que facturas.
    Optional<DocumentoSoporteData> findFirstByCompraIdAndEmpresaIdOrderByDocumentoSoporteIdDesc(Long compraId, String empresaId);
    void deleteByDocumentoSoporteIdAndEmpresaId(Long documentoSoporteId, String empresaId);
}
