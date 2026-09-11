package com.pos_backend.venta.infraestructure.driver_adapters.jpa_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CajaSesionDataJpaRepository extends JpaRepository<CajaSesionData, Long> {
    Optional<CajaSesionData> findByEmpresaIdAndEstado(String empresaId, String estado);
    Optional<CajaSesionData> findByCajaIdAndEmpresaId(Long cajaId, String empresaId);
    List<CajaSesionData> findByEmpresaIdOrderByFechaAperturaDesc(String empresaId);
}
