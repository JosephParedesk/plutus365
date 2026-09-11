package com.pos_backend.cliente.infraestructure.driver_adapters.jpa_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ClienteDataJpaRepository extends JpaRepository<ClienteData, Long> {
    List<ClienteData> findByEmpresaId(String empresaId);
    List<ClienteData> findByEmpresaIdAndActivoTrue(String empresaId);
    Optional<ClienteData> findByClienteIdAndEmpresaId(Long clienteId, String empresaId);
    Optional<ClienteData> findByNumeroDocumentoAndEmpresaId(String numeroDocumento, String empresaId);
    boolean existsByNumeroDocumentoIgnoreCaseAndEmpresaId(String numeroDocumento, String empresaId);
}
