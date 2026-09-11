package com.pos_backend.venta.infraestructure.driver_adapters.jpa_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NotaDebitoVentaDataJpaRepository extends JpaRepository<NotaDebitoVentaData, Long> {
    List<NotaDebitoVentaData> findByEmpresaIdOrderByNotaDebitoIdDesc(String empresaId);
    Optional<NotaDebitoVentaData> findByNotaDebitoIdAndEmpresaId(Long id, String empresaId);
    List<NotaDebitoVentaData> findByEmpresaIdAndFechaBetween(String empresaId, LocalDateTime desde, LocalDateTime hasta);
    long countByEmpresaId(String empresaId);
}
