package com.pos_backend.venta.infraestructure.driver_adapters.jpa_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReciboCajaDataJpaRepository extends JpaRepository<ReciboCajaData, Long> {
    List<ReciboCajaData> findByEmpresaIdOrderByReciboIdDesc(String empresaId);
    Optional<ReciboCajaData> findByReciboIdAndEmpresaId(Long id, String empresaId);
    List<ReciboCajaData> findByEmpresaIdAndFechaBetween(String empresaId, LocalDateTime desde, LocalDateTime hasta);
    long countByEmpresaId(String empresaId);
}
