package com.pos_backend.facturacion.infraestructure.driver_adapters.jpa_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface NominaElectronicaDataJpaRepository extends JpaRepository<NominaElectronicaData, Long> {
    List<NominaElectronicaData> findByEmpresaIdOrderByNominaElectronicaIdDesc(String empresaId);
    List<NominaElectronicaData> findByNominaIdAndEmpresaId(Long nominaId, String empresaId);
    Optional<NominaElectronicaData> findByNominaIdAndEmpleadoIdAndEmpresaId(Long nominaId, Long empleadoId, String empresaId);
    Optional<NominaElectronicaData> findByNominaElectronicaIdAndEmpresaId(Long nominaElectronicaId, String empresaId);
    void deleteByNominaElectronicaIdAndEmpresaId(Long nominaElectronicaId, String empresaId);
}
