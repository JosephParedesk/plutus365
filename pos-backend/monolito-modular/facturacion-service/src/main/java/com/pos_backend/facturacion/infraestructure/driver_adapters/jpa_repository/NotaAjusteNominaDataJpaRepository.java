package com.pos_backend.facturacion.infraestructure.driver_adapters.jpa_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface NotaAjusteNominaDataJpaRepository extends JpaRepository<NotaAjusteNominaData, Long> {
    List<NotaAjusteNominaData> findByEmpresaIdOrderByNotaAjusteNominaIdDesc(String empresaId);
    Optional<NotaAjusteNominaData> findByNotaAjusteNominaIdAndEmpresaId(Long id, String empresaId);
    Optional<NotaAjusteNominaData> findTopByNominaElectronicaIdAndEmpresaIdOrderByNotaAjusteNominaIdDesc(Long nominaElectronicaId, String empresaId);
}
