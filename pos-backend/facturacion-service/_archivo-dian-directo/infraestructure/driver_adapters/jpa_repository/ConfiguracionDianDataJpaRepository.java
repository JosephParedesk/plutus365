package com.pos_backend.facturacion.infraestructure.driver_adapters.jpa_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ConfiguracionDianDataJpaRepository extends JpaRepository<ConfiguracionDianData, String> {

    // Proyecciones escalares: NO pasan por el caché de primer nivel de Hibernate (a
    // diferencia de findById), así que siempre leen el valor real de la BD, sin
    // importar si esta empresa ya se cargó como entidad antes en el mismo request.
    @Query("SELECT c.consecutivoActual FROM ConfiguracionDianData c WHERE c.empresaId = :empresaId")
    Long leerConsecutivoActual(@Param("empresaId") String empresaId);

    @Query("SELECT c.rangoHasta FROM ConfiguracionDianData c WHERE c.empresaId = :empresaId")
    Long leerRangoHasta(@Param("empresaId") String empresaId);

    // UPDATE atómico compare-and-swap: solo avanza el consecutivo si sigue
    // valiendo lo que se leyó. Si otra factura concurrente ya lo movió, esto
    // afecta 0 filas y el llamador reintenta con el valor fresco.
    @Modifying
    @Transactional
    @Query("UPDATE ConfiguracionDianData c SET c.consecutivoActual = :nuevo " +
            "WHERE c.empresaId = :empresaId AND c.consecutivoActual = :actual")
    int avanzarConsecutivoSiCoincide(@Param("empresaId") String empresaId, @Param("actual") long actual, @Param("nuevo") long nuevo);
}
