package com.pos_backend.nomina.infraestructure.driver_adapters.jpa_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface NominaDataJpaRepository extends JpaRepository<NominaData, Long> {
    List<NominaData> findByEmpresaIdOrderByAnioDescMesDesc(String empresaId);
    Optional<NominaData> findByNominaIdAndEmpresaId(Long nominaId, String empresaId);
    boolean existsByAnioAndMesAndEmpresaIdAndEstadoNot(Integer anio, Integer mes, String empresaId, String estado);
    long countByEmpresaId(String empresaId);
}
