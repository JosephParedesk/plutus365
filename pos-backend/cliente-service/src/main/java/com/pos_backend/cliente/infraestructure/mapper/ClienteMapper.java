package com.pos_backend.cliente.infraestructure.mapper;

import com.pos_backend.cliente.domain.model.Cliente;
import com.pos_backend.cliente.infraestructure.driver_adapters.jpa_repository.ClienteData;
import org.springframework.stereotype.Component;

@Component
public class ClienteMapper {

    public Cliente toCliente(ClienteData data) {
        if (data == null) return new Cliente();

        Cliente c = new Cliente();
        c.setClienteId(data.getClienteId());
        c.setTipoPersona(data.getTipoPersona());
        c.setTipoDocumento(data.getTipoDocumento());
        c.setNumeroDocumento(data.getNumeroDocumento());
        c.setDv(data.getDv());
        c.setRegimenFiscal(data.getRegimenFiscal());
        c.setNombres(data.getNombres());
        c.setApellidos(data.getApellidos());
        c.setRazonSocial(data.getRazonSocial());
        c.setCorreo(data.getCorreo());
        c.setTelefono(data.getTelefono());
        c.setDireccion(data.getDireccion());
        c.setCiudad(data.getCiudad());
        c.setDepartamento(data.getDepartamento());
        c.setPais(data.getPais());
        c.setCodigoPostal(data.getCodigoPostal());
        c.setActivo(data.getActivo());
        c.setEmpresaId(data.getEmpresaId());
        return c;
    }

    public ClienteData toClienteData(Cliente c) {
        ClienteData data = new ClienteData();
        data.setClienteId(c.getClienteId());
        data.setTipoPersona(c.getTipoPersona());
        data.setTipoDocumento(c.getTipoDocumento());
        data.setNumeroDocumento(c.getNumeroDocumento());
        data.setDv(c.getDv());
        data.setRegimenFiscal(c.getRegimenFiscal());
        data.setNombres(c.getNombres());
        data.setApellidos(c.getApellidos());
        data.setRazonSocial(c.getRazonSocial());
        data.setCorreo(c.getCorreo());
        data.setTelefono(c.getTelefono());
        data.setDireccion(c.getDireccion());
        data.setCiudad(c.getCiudad());
        data.setDepartamento(c.getDepartamento());
        data.setPais(c.getPais());
        data.setCodigoPostal(c.getCodigoPostal());
        data.setActivo(c.getActivo() != null ? c.getActivo() : true);
        data.setEmpresaId(c.getEmpresaId());
        return data;
    }
}
