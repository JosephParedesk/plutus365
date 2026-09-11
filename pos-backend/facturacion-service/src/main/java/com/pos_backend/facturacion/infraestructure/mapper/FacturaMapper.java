package com.pos_backend.facturacion.infraestructure.mapper;

import com.pos_backend.facturacion.domain.model.Factura;
import com.pos_backend.facturacion.infraestructure.driver_adapters.jpa_repository.FacturaData;
import org.springframework.stereotype.Component;

@Component
public class FacturaMapper {

    public Factura toDomain(FacturaData data) {
        if (data == null) return null;
        Factura f = new Factura();
        f.setFacturaId(data.getFacturaId());
        f.setEmpresaId(data.getEmpresaId());
        f.setVentaId(data.getVentaId());
        f.setReferenceCode(data.getReferenceCode());
        f.setNumeroFactura(data.getNumeroFactura());
        f.setCufe(data.getCufe());
        f.setQrUrl(data.getQrUrl());
        f.setUrlDocumento(data.getUrlDocumento());
        f.setAmbiente(data.getAmbiente());
        f.setEstado(data.getEstado());
        f.setRespuestaDian(data.getRespuestaDian());
        f.setFechaEmision(data.getFechaEmision());
        f.setFechaEnvio(data.getFechaEnvio());
        return f;
    }

    public FacturaData toData(Factura f) {
        FacturaData data = new FacturaData();
        data.setFacturaId(f.getFacturaId());
        data.setEmpresaId(f.getEmpresaId());
        data.setVentaId(f.getVentaId());
        data.setReferenceCode(f.getReferenceCode());
        data.setNumeroFactura(f.getNumeroFactura());
        data.setCufe(f.getCufe());
        data.setQrUrl(f.getQrUrl());
        data.setUrlDocumento(f.getUrlDocumento());
        data.setAmbiente(f.getAmbiente());
        data.setEstado(f.getEstado());
        data.setRespuestaDian(f.getRespuestaDian());
        data.setFechaEmision(f.getFechaEmision());
        data.setFechaEnvio(f.getFechaEnvio());
        return data;
    }
}
