package com.pos_backend.venta.infraestructure.mapper;

import com.pos_backend.venta.domain.model.CajaSesion;
import com.pos_backend.venta.infraestructure.driver_adapters.jpa_repository.CajaSesionData;
import org.springframework.stereotype.Component;

@Component
public class CajaSesionMapper {

    public CajaSesion toDomain(CajaSesionData data) {
        if (data == null) return null;
        CajaSesion c = new CajaSesion();
        c.setCajaId(data.getCajaId());
        c.setEmpresaId(data.getEmpresaId());
        c.setFechaApertura(data.getFechaApertura());
        c.setFechaCierre(data.getFechaCierre());
        c.setMontoApertura(data.getMontoApertura());
        c.setMontoCierreDeclarado(data.getMontoCierreDeclarado());
        c.setMontoCierreCalculado(data.getMontoCierreCalculado());
        c.setDiferencia(data.getDiferencia());
        c.setTotalVentasEfectivo(data.getTotalVentasEfectivo());
        c.setTotalVentasOtros(data.getTotalVentasOtros());
        c.setNumeroVentas(data.getNumeroVentas());
        c.setEstado(data.getEstado());
        c.setUsuarioApertura(data.getUsuarioApertura());
        c.setUsuarioCierre(data.getUsuarioCierre());
        c.setObservaciones(data.getObservaciones());
        return c;
    }

    public CajaSesionData toData(CajaSesion c) {
        CajaSesionData data = new CajaSesionData();
        data.setCajaId(c.getCajaId());
        data.setEmpresaId(c.getEmpresaId());
        data.setFechaApertura(c.getFechaApertura());
        data.setFechaCierre(c.getFechaCierre());
        data.setMontoApertura(c.getMontoApertura());
        data.setMontoCierreDeclarado(c.getMontoCierreDeclarado());
        data.setMontoCierreCalculado(c.getMontoCierreCalculado());
        data.setDiferencia(c.getDiferencia());
        data.setTotalVentasEfectivo(c.getTotalVentasEfectivo());
        data.setTotalVentasOtros(c.getTotalVentasOtros());
        data.setNumeroVentas(c.getNumeroVentas());
        data.setEstado(c.getEstado());
        data.setUsuarioApertura(c.getUsuarioApertura());
        data.setUsuarioCierre(c.getUsuarioCierre());
        data.setObservaciones(c.getObservaciones());
        return data;
    }
}
