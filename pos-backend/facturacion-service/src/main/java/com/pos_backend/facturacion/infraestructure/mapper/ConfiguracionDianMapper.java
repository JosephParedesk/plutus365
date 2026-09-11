package com.pos_backend.facturacion.infraestructure.mapper;

import com.pos_backend.facturacion.domain.model.ConfiguracionDian;
import com.pos_backend.facturacion.infraestructure.driver_adapters.jpa_repository.ConfiguracionDianData;
import org.springframework.stereotype.Component;

// OJO: factusClientSecret/factusPassword viajan cifrados en este mapeo — se cifran
// y descifran en ConfiguracionDianDataGatewayImpl, no acá (mapper se mantiene simple).
@Component
public class ConfiguracionDianMapper {

    public ConfiguracionDian toDomain(ConfiguracionDianData data) {
        if (data == null) return null;
        ConfiguracionDian c = new ConfiguracionDian();
        c.setEmpresaId(data.getEmpresaId());
        c.setFactusClientId(data.getFactusClientId());
        c.setFactusClientSecret(data.getFactusClientSecret());
        c.setFactusUsername(data.getFactusUsername());
        c.setFactusPassword(data.getFactusPassword());
        c.setFacturaNumberingRangeId(data.getFacturaNumberingRangeId());
        c.setNotaCreditoNumberingRangeId(data.getNotaCreditoNumberingRangeId());
        c.setNotaDebitoNumberingRangeId(data.getNotaDebitoNumberingRangeId());
        c.setDocumentoSoporteNumberingRangeId(data.getDocumentoSoporteNumberingRangeId());
        c.setNominaNumberingRangeId(data.getNominaNumberingRangeId());
        c.setNotaAjusteDocumentoSoporteNumberingRangeId(data.getNotaAjusteDocumentoSoporteNumberingRangeId());
        c.setNotaAjusteNominaNumberingRangeId(data.getNotaAjusteNominaNumberingRangeId());
        c.setActivo(data.getActivo());
        return c;
    }

    public ConfiguracionDianData toData(ConfiguracionDian c) {
        ConfiguracionDianData data = new ConfiguracionDianData();
        data.setEmpresaId(c.getEmpresaId());
        data.setFactusClientId(c.getFactusClientId());
        data.setFactusClientSecret(c.getFactusClientSecret());
        data.setFactusUsername(c.getFactusUsername());
        data.setFactusPassword(c.getFactusPassword());
        data.setFacturaNumberingRangeId(c.getFacturaNumberingRangeId());
        data.setNotaCreditoNumberingRangeId(c.getNotaCreditoNumberingRangeId());
        data.setNotaDebitoNumberingRangeId(c.getNotaDebitoNumberingRangeId());
        data.setDocumentoSoporteNumberingRangeId(c.getDocumentoSoporteNumberingRangeId());
        data.setNominaNumberingRangeId(c.getNominaNumberingRangeId());
        data.setNotaAjusteDocumentoSoporteNumberingRangeId(c.getNotaAjusteDocumentoSoporteNumberingRangeId());
        data.setNotaAjusteNominaNumberingRangeId(c.getNotaAjusteNominaNumberingRangeId());
        data.setActivo(c.getActivo());
        return data;
    }
}
