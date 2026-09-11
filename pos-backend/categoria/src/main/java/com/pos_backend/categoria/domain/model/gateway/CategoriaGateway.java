package com.pos_backend.categoria.domain.model.gateway;

import com.pos_backend.categoria.domain.model.Categoria;
import java.util.List;

public interface CategoriaGateway {
    Categoria guardarCategoria(Categoria categoria);
    Categoria buscarCategoriaPorId(Long categoriaId, String empresaId);
    List<Categoria> listarCategorias(String empresaId);
    List<Categoria> listarCategoriasActivas(String empresaId);
    void eliminarCategoria(Long categoriaId);
    boolean existePorNombre(String nombre, String empresaId);
}
