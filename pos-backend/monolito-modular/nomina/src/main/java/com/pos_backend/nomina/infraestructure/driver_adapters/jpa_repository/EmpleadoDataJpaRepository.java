package com.pos_backend.nomina.infraestructure.driver_adapters.jpa_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EmpleadoDataJpaRepository extends JpaRepository<EmpleadoData, Long> {
    List<EmpleadoData> findByEmpresaIdOrderByNombresAsc(String empresaId);
    List<EmpleadoData> findByEmpresaIdAndActivoTrue(String empresaId);
    Optional<EmpleadoData> findByEmpleadoIdAndEmpresaId(Long empleadoId, String empresaId);
    Optional<EmpleadoData> findByNumeroDocumentoAndEmpresaId(String numeroDocumento, String empresaId);
}
