package com.pos_backend.empresa.infraestructure.mapper;

import com.pos_backend.empresa.domain.model.Empresa;
import com.pos_backend.empresa.infraestructure.driver_adapters.jpa_repository.EmpresaData;
import org.springframework.stereotype.Component;

@Component
public class EmpresaMapper {

    public Empresa toEmpresa(EmpresaData data) {
        if (data == null) return new Empresa();

        Empresa e = new Empresa();
        e.setEmpresaId(data.getEmpresaId());
        e.setTipoPersona(data.getTipoPersona());
        e.setTipoDocumento(data.getTipoDocumento());
        e.setNumeroDocumento(data.getNumeroDocumento());
        e.setDv(data.getDv());
        e.setRegimenFiscal(data.getRegimenFiscal());
        e.setPeriodicidadIva(data.getPeriodicidadIva());
        e.setAgenteRetenedor(data.getAgenteRetenedor() != null && data.getAgenteRetenedor());
        e.setRazonSocial(data.getRazonSocial());
        e.setNombres(data.getNombres());
        e.setApellidos(data.getApellidos());
        e.setNombreComercial(data.getNombreComercial());
        e.setCorreo(data.getCorreo());
        e.setTelefono(data.getTelefono());
        e.setDireccion(data.getDireccion());
        e.setCiudad(data.getCiudad());
        e.setDepartamento(data.getDepartamento());
        e.setPais(data.getPais());
        e.setLogoUrl(data.getLogoUrl());
        e.setColorPrincipal(data.getColorPrincipal());
        e.setMoneda(data.getMoneda());
        e.setPosHabilitado(data.getPosHabilitado() != null && data.getPosHabilitado());
        return e;
    }

    public EmpresaData toEmpresaData(Empresa e) {
        EmpresaData data = new EmpresaData();
        data.setEmpresaId(e.getEmpresaId());
        data.setTipoPersona(e.getTipoPersona());
        data.setTipoDocumento(e.getTipoDocumento());
        data.setNumeroDocumento(e.getNumeroDocumento());
        data.setDv(e.getDv());
        data.setRegimenFiscal(e.getRegimenFiscal());
        data.setPeriodicidadIva(e.getPeriodicidadIva());
        data.setAgenteRetenedor(e.getAgenteRetenedor() != null && e.getAgenteRetenedor());
        data.setRazonSocial(e.getRazonSocial());
        data.setNombres(e.getNombres());
        data.setApellidos(e.getApellidos());
        data.setNombreComercial(e.getNombreComercial());
        data.setCorreo(e.getCorreo());
        data.setTelefono(e.getTelefono());
        data.setDireccion(e.getDireccion());
        data.setCiudad(e.getCiudad());
        data.setDepartamento(e.getDepartamento());
        data.setPais(e.getPais());
        data.setLogoUrl(e.getLogoUrl());
        data.setColorPrincipal(e.getColorPrincipal());
        data.setMoneda(e.getMoneda());
        data.setPosHabilitado(e.getPosHabilitado() != null && e.getPosHabilitado());
        return data;
    }
}
