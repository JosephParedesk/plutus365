package com.pos_backend.venta.infraestructure.driver_adapters.jpa_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VentaDataJpaRepository extends JpaRepository<VentaData, Long> {

    List<VentaData> findByEmpresaId(String empresaId);

    Optional<VentaData> findByVentaIdAndEmpresaId(Long ventaId, String empresaId);

    List<VentaData> findByEmpresaIdAndClienteId(String empresaId, Long clienteId);

    List<VentaData> findByEmpresaIdAndFechaBetween(
            String empresaId, LocalDateTime fechaInicio, LocalDateTime fechaFin);

    long countByEmpresaId(String empresaId);
}
