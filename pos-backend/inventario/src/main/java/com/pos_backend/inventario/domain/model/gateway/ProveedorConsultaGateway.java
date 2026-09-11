package com.pos_backend.inventario.domain.model.gateway;

public interface ProveedorConsultaGateway {
    /** Busca el proveedorId cuyo nombre coincide (sin importar mayúsculas). Null si no existe. */
    String buscarIdPorNombre(String nombre, String empresaId);
}
