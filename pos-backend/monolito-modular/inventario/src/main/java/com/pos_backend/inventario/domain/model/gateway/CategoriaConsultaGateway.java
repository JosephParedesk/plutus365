package com.pos_backend.inventario.domain.model.gateway;

public interface CategoriaConsultaGateway {
    /** Busca el categoriaId cuyo nombre coincide (sin importar mayúsculas). Null si no existe. */
    String buscarIdPorNombre(String nombre, String empresaId);
}
