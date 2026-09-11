package com.pos_backend.facturacion.infraestructure.driver_adapters.jpa_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RecepcionDocumentoDataJpaRepository extends JpaRepository<RecepcionDocumentoData, Long> {
    List<RecepcionDocumentoData> findByEmpresaIdOrderByRecepcionIdDesc(String empresaId);
    Optional<RecepcionDocumentoData> findByRecepcionIdAndEmpresaId(Long id, String empresaId);
    Optional<RecepcionDocumentoData> findByCompraIdAndEmpresaId(Long compraId, String empresaId);
}
