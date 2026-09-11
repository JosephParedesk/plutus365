package com.pos_backend.inventario.infraestructure.driver_adapters.jpa_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface MovimientoInventarioDataJpaRepository extends JpaRepository<MovimientoInventarioData, Long> {
    List<MovimientoInventarioData> findByEmpresaIdAndSkuOrderByFechaAsc(String empresaId, String sku);
    List<MovimientoInventarioData> findByEmpresaIdAndFechaBetweenOrderByFechaAsc(
            String empresaId, LocalDateTime desde, LocalDateTime hasta);
}
