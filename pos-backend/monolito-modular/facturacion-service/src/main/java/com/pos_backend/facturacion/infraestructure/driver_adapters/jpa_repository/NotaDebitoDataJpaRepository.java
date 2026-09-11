package com.pos_backend.facturacion.infraestructure.driver_adapters.jpa_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface NotaDebitoDataJpaRepository extends JpaRepository<NotaDebitoData, Long> {
    List<NotaDebitoData> findByEmpresaIdOrderByNotaDebitoIdDesc(String empresaId);
    List<NotaDebitoData> findByFacturaIdAndEmpresaId(Long facturaId, String empresaId);
    Optional<NotaDebitoData> findByNotaDebitoIdAndEmpresaId(Long id, String empresaId);
    void deleteByNotaDebitoIdAndEmpresaId(Long notaDebitoId, String empresaId);
}
