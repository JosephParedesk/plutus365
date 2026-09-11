package com.pos_backend.contabilidad.infraestructure.mapper;

import com.pos_backend.contabilidad.domain.model.CuentaContable;
import com.pos_backend.contabilidad.infraestructure.driver_adapters.jpa_repository.CuentaContableData;
import org.springframework.stereotype.Component;

@Component
public class CuentaContableMapper {

    public CuentaContable toDomain(CuentaContableData data) {
        if (data == null) return null;
        CuentaContable c = new CuentaContable();
        c.setId(data.getId());
        c.setEmpresaId(data.getEmpresaId());
        c.setCodigo(data.getCodigo());
        c.setNombre(data.getNombre());
        c.setNivel(data.getNivel());
        c.setCodigoPadre(data.getCodigoPadre());
        c.setNaturaleza(data.getNaturaleza());
        c.setEsTransaccional(data.getEsTransaccional());
        c.setCategoria(data.getCategoria());
        c.setDetalleSaldos(data.getDetalleSaldos());
        c.setActiva(data.getActiva());
        c.setPersonalizada(data.getPersonalizada());
        return c;
    }

    public CuentaContableData toData(CuentaContable c) {
        CuentaContableData data = new CuentaContableData();
        data.setId(c.getId());
        data.setEmpresaId(c.getEmpresaId());
        data.setCodigo(c.getCodigo());
        data.setNombre(c.getNombre());
        data.setNivel(c.getNivel());
        data.setCodigoPadre(c.getCodigoPadre());
        data.setNaturaleza(c.getNaturaleza());
        data.setEsTransaccional(c.getEsTransaccional());
        data.setCategoria(c.getCategoria());
        data.setDetalleSaldos(c.getDetalleSaldos());
        data.setActiva(c.getActiva());
        data.setPersonalizada(c.getPersonalizada());
        return data;
    }
}
