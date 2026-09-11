package com.pos_backend.facturacion.infraestructure.mapper;

import com.pos_backend.facturacion.domain.model.ConfiguracionDian;
import com.pos_backend.facturacion.infraestructure.driver_adapters.jpa_repository.ConfiguracionDianData;
import org.springframework.stereotype.Component;

@Component
public class ConfiguracionDianMapper {

    public ConfiguracionDian toDomain(ConfiguracionDianData data) {
        if (data == null) return null;
        ConfiguracionDian c = new ConfiguracionDian();
        c.setEmpresaId(data.getEmpresaId());
        c.setResolucionNumero(data.getResolucionNumero());
        c.setResolucionFechaInicio(data.getResolucionFechaInicio());
        c.setResolucionFechaFin(data.getResolucionFechaFin());
        c.setPrefijo(data.getPrefijo());
        c.setRangoDesde(data.getRangoDesde());
        c.setRangoHasta(data.getRangoHasta());
        c.setConsecutivoActual(data.getConsecutivoActual());
        c.setSoftwareId(data.getSoftwareId());
        c.setSoftwarePin(data.getSoftwarePin());
        c.setClaveTecnica(data.getClaveTecnica());
        c.setAmbiente(data.getAmbiente());
        c.setTestSetId(data.getTestSetId());
        c.setCertificadoNombreArchivo(data.getCertificadoNombreArchivo());
        c.setCertificadoCargadoEn(data.getCertificadoCargadoEn());
        return c;
    }

    public ConfiguracionDianData toData(ConfiguracionDian c) {
        ConfiguracionDianData data = new ConfiguracionDianData();
        data.setEmpresaId(c.getEmpresaId());
        data.setResolucionNumero(c.getResolucionNumero());
        data.setResolucionFechaInicio(c.getResolucionFechaInicio());
        data.setResolucionFechaFin(c.getResolucionFechaFin());
        data.setPrefijo(c.getPrefijo());
        data.setRangoDesde(c.getRangoDesde());
        data.setRangoHasta(c.getRangoHasta());
        data.setConsecutivoActual(c.getConsecutivoActual());
        data.setSoftwareId(c.getSoftwareId());
        data.setSoftwarePin(c.getSoftwarePin());
        data.setClaveTecnica(c.getClaveTecnica());
        data.setAmbiente(c.getAmbiente());
        data.setTestSetId(c.getTestSetId());
        data.setCertificadoNombreArchivo(c.getCertificadoNombreArchivo());
        data.setCertificadoCargadoEn(c.getCertificadoCargadoEn());
        return data;
    }
}
