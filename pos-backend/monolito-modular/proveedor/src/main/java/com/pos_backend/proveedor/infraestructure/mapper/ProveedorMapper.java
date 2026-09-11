package com.pos_backend.proveedor.infraestructure.mapper;

import com.pos_backend.proveedor.domain.model.Proveedor;
import com.pos_backend.proveedor.infraestructure.driver_adapters.jpa_repository.ProveedorData;
import org.springframework.stereotype.Component;

@Component
public class ProveedorMapper {

    public Proveedor toProveedor(ProveedorData data) {
        Proveedor p = new Proveedor();
        p.setProveedorId(data.getProveedorId());
        p.setNit(data.getNit());
        p.setNombre(data.getNombre());
        p.setContacto(data.getContacto());
        p.setTelefono(data.getTelefono());
        p.setCorreo(data.getCorreo());
        p.setDireccion(data.getDireccion());
        p.setCiudad(data.getCiudad());
        p.setPlazoCredito(data.getPlazoCredito());
        p.setActivo(data.getActivo());
        p.setEmpresaId(data.getEmpresaId());
        return p;
    }

    public ProveedorData toProveedorData(Proveedor p) {
        ProveedorData data = new ProveedorData();
        data.setProveedorId(p.getProveedorId());
        data.setNit(p.getNit());
        data.setNombre(p.getNombre());
        data.setContacto(p.getContacto());
        data.setTelefono(p.getTelefono());
        data.setCorreo(p.getCorreo());
        data.setDireccion(p.getDireccion());
        data.setCiudad(p.getCiudad());
        data.setPlazoCredito(p.getPlazoCredito());
        data.setActivo(p.getActivo());
        data.setEmpresaId(p.getEmpresaId());
        return data;
    }
}