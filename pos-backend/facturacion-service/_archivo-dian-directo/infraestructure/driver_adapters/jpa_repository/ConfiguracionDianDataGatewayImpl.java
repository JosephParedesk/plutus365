package com.pos_backend.facturacion.infraestructure.driver_adapters.jpa_repository;

import com.pos_backend.facturacion.domain.model.ConfiguracionDian;
import com.pos_backend.facturacion.domain.model.gateway.ConfiguracionDianGateway;
import com.pos_backend.facturacion.infraestructure.mapper.ConfiguracionDianMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ConfiguracionDianDataGatewayImpl implements ConfiguracionDianGateway {

    private static final Logger log = LoggerFactory.getLogger(ConfiguracionDianDataGatewayImpl.class);

    private final ConfiguracionDianDataJpaRepository repository;
    private final ConfiguracionDianMapper mapper;

    @Override
    public ConfiguracionDian guardar(ConfiguracionDian configuracion) {
        return mapper.toDomain(repository.save(mapper.toData(configuracion)));
    }

    @Override
    public ConfiguracionDian buscarPorEmpresaId(String empresaId) {
        return repository.findById(empresaId).map(mapper::toDomain).orElse(null);
    }

    @Override
    public long reservarSiguienteConsecutivo(String empresaId) {
        for (int intento = 0; intento < 20; intento++) {
            Long consecutivoActual = repository.leerConsecutivoActual(empresaId);
            Long rangoHasta = repository.leerRangoHasta(empresaId);
            if (rangoHasta == null)
                throw new RuntimeException("Configura primero la facturación electrónica de tu empresa (resolución DIAN)");
            // consecutivoActual siempre queda seteado (nunca null) desde que se guarda la
            // configuración por primera vez — ver ConfiguracionDianUseCase.guardar().
            if (consecutivoActual == null)
                throw new RuntimeException("La configuración DIAN de la empresa está incompleta (falta el consecutivo actual)");

            if (consecutivoActual > rangoHasta)
                throw new RuntimeException("Se agotó el rango de numeración autorizado por la DIAN (hasta " +
                        rangoHasta + "). Solicita una nueva resolución.");

            if (repository.avanzarConsecutivoSiCoincide(empresaId, consecutivoActual, consecutivoActual + 1) == 1) {
                long restantes = rangoHasta - consecutivoActual; // ya sin contar el número que se acaba de reservar
                if (restantes <= ConfiguracionDian.UMBRAL_ALERTA_RANGO)
                    log.warn("Empresa {} se está quedando sin numeración DIAN: quedan {} números (hasta {})",
                            empresaId, restantes, rangoHasta);
                return consecutivoActual;
            }
            // otra factura concurrente ya avanzó el consecutivo entre la lectura y el UPDATE: reintenta con el valor fresco
        }
        throw new RuntimeException("No se pudo reservar el número de factura por alta concurrencia, intenta de nuevo.");
    }
}
