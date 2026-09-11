package com.pos_backend.proveedor.domain.model.gateway;

import com.pos_backend.proveedor.domain.model.Proveedor;
import java.util.List;

public interface ProveedorGateway {
    Proveedor guardarProveedor(Proveedor proveedor);
    Proveedor buscarProveedorPorId(Long proveedorId, String empresaId);
    Proveedor buscarProveedorPorNit(String nit, String empresaId);
    List<Proveedor> listarProveedores(String empresaId);
    List<Proveedor> listarProveedoresActivos(String empresaId);
    void eliminarProveedor(Long proveedorId);
    boolean existePorNit(String nit, String empresaId);
    boolean existePorNombre(String nombre, String empresaId);
}
