package com.pos_backend.venta.infraestructure.driver_adapters.jpa_repository;

import com.pos_backend.venta.domain.model.ConfiguracionRecibo;
import com.pos_backend.venta.domain.model.gateway.ConfiguracionReciboGateway;
import com.pos_backend.venta.infraestructure.mapper.ConfiguracionReciboMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ConfiguracionReciboDataGatewayImpl implements ConfiguracionReciboGateway {

    private final ConfiguracionReciboDataJpaRepository repository;
    private final ConfiguracionReciboMapper mapper;

    @Override
    public ConfiguracionRecibo buscarPorEmpresaId(String empresaId) {
        return repository.findById(empresaId).map(mapper::toDomain).orElse(null);
    }

    @Override
    public ConfiguracionRecibo guardar(ConfiguracionRecibo configuracion) {
        return mapper.toDomain(repository.save(mapper.toData(configuracion)));
    }
}
