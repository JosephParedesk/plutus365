package com.pos_backend.facturacion.infraestructure.driver_adapters.jpa_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface NominaElectronicaDataJpaRepository extends JpaRepository<NominaElectronicaData, Long> {
    List<NominaElectronicaData> findByEmpresaIdOrderByNominaElectronicaIdDesc(String empresaId);
    List<NominaElectronicaData> findByNominaIdAndEmpresaId(Long nominaId, String empresaId);
    // No es Optional: cada intento fallido (ERROR/RECHAZADA) inserta una fila nueva
    // en vez de actualizar la anterior (ver guardar() en el gateway), así que puede
    // haber varias filas para el mismo (nominaId, empleadoId) — un Optional acá
    // tira NonUniqueResultException apenas hay un segundo intento.
    List<NominaElectronicaData> findByNominaIdAndEmpleadoIdAndEmpresaIdOrderByNominaElectronicaIdDesc(Long nominaId, Long empleadoId, String empresaId);
    Optional<NominaElectronicaData> findByNominaElectronicaIdAndEmpresaId(Long nominaElectronicaId, String empresaId);
    void deleteByNominaElectronicaIdAndEmpresaId(Long nominaElectronicaId, String empresaId);
}
