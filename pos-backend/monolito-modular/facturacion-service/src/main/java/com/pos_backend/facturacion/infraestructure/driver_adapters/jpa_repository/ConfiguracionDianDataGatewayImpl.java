package com.pos_backend.facturacion.infraestructure.driver_adapters.jpa_repository;

import com.pos_backend.facturacion.domain.model.ConfiguracionDian;
import com.pos_backend.facturacion.domain.model.gateway.ConfiguracionDianGateway;
import com.pos_backend.facturacion.infraestructure.driver_adapters.certificado.CertificadoCrypto;
import com.pos_backend.facturacion.infraestructure.mapper.ConfiguracionDianMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ConfiguracionDianDataGatewayImpl implements ConfiguracionDianGateway {

    private final ConfiguracionDianDataJpaRepository repository;
    private final ConfiguracionDianMapper mapper;
    private final CertificadoCrypto crypto;

    @Override
    public ConfiguracionDian guardar(ConfiguracionDian configuracion) {
        ConfiguracionDianData data = mapper.toData(configuracion);
        data.setFactusClientSecret(crypto.encriptar(configuracion.getFactusClientSecret()));
        data.setFactusPassword(crypto.encriptar(configuracion.getFactusPassword()));
        return descifrar(mapper.toDomain(repository.save(data)));
    }

    @Override
    public ConfiguracionDian buscarPorEmpresaId(String empresaId) {
        return repository.findById(empresaId).map(mapper::toDomain).map(this::descifrar).orElse(null);
    }

    private ConfiguracionDian descifrar(ConfiguracionDian c) {
        if (c == null) return null;
        // Filas viejas (previas a la migración a Factus) tienen estos campos en NULL
        // — no había nada que cifrar todavía. crypto.desencriptar(null) explota.
        if (c.getFactusClientSecret() != null) c.setFactusClientSecret(crypto.desencriptar(c.getFactusClientSecret()));
        if (c.getFactusPassword() != null) c.setFactusPassword(crypto.desencriptar(c.getFactusPassword()));
        return c;
    }
}
