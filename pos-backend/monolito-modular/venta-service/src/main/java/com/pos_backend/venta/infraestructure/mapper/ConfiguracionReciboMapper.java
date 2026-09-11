package com.pos_backend.venta.infraestructure.mapper;

import com.pos_backend.venta.domain.model.ConfiguracionRecibo;
import com.pos_backend.venta.infraestructure.driver_adapters.jpa_repository.ConfiguracionReciboData;
import org.springframework.stereotype.Component;

@Component
public class ConfiguracionReciboMapper {

    public ConfiguracionRecibo toDomain(ConfiguracionReciboData data) {
        if (data == null) return null;
        ConfiguracionRecibo c = new ConfiguracionRecibo();
        c.setEmpresaId(data.getEmpresaId());
        c.setAnchoPapel(data.getAnchoPapel());
        c.setTamanioFuente(data.getTamanioFuente());
        c.setMensajePie(data.getMensajePie());
        c.setMostrarLogo(data.getMostrarLogo());
        c.setMostrarDireccionEmpresa(data.getMostrarDireccionEmpresa());
        c.setMostrarAtendidoPor(data.getMostrarAtendidoPor());
        return c;
    }

    public ConfiguracionReciboData toData(ConfiguracionRecibo c) {
        ConfiguracionReciboData data = new ConfiguracionReciboData();
        data.setEmpresaId(c.getEmpresaId());
        data.setAnchoPapel(c.getAnchoPapel());
        data.setTamanioFuente(c.getTamanioFuente());
        data.setMensajePie(c.getMensajePie());
        data.setMostrarLogo(c.getMostrarLogo());
        data.setMostrarDireccionEmpresa(c.getMostrarDireccionEmpresa());
        data.setMostrarAtendidoPor(c.getMostrarAtendidoPor());
        return data;
    }
}
