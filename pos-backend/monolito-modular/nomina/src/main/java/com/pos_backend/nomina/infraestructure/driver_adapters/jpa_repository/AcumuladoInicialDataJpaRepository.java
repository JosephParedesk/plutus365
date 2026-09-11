package com.pos_backend.nomina.infraestructure.driver_adapters.jpa_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AcumuladoInicialDataJpaRepository extends JpaRepository<AcumuladoInicialData, Long> {
    List<AcumuladoInicialData> findByEmpresaIdAndAnio(String empresaId, Integer anio);
    Optional<AcumuladoInicialData> findByEmpleadoIdAndAnioAndEmpresaId(Long empleadoId, Integer anio, String empresaId);
    Optional<AcumuladoInicialData> findByAcumuladoIdAndEmpresaId(Long id, String empresaId);
}
