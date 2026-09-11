package com.pos_backend.categoria.domain.usecase;

import com.pos_backend.categoria.domain.model.Categoria;
import com.pos_backend.categoria.domain.model.gateway.CategoriaGateway;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
public class CategoriaUseCase {

    private final CategoriaGateway categoriaGateway;

    public List<Categoria> listarCategorias(String empresaId) {
        return categoriaGateway.listarCategoriasActivas(empresaId);
    }

    public Categoria buscarCategoriaPorId(Long categoriaId, String empresaId) {
        if (categoriaId == null)
            throw new RuntimeException("El id de la categoría es obligatorio");
        Categoria categoria = categoriaGateway.buscarCategoriaPorId(categoriaId, empresaId);
        if (categoria == null)
            throw new NoSuchElementException("Categoría no encontrada");
        return categoria;
    }

    public Categoria guardarCategoria(Categoria categoria, String empresaId) {
        if (categoria.getNombre() == null || categoria.getNombre().isBlank())
            throw new RuntimeException("El nombre de la categoría es obligatorio");

        if (categoriaGateway.existePorNombre(categoria.getNombre(), empresaId))
            throw new RuntimeException("Ya existe una categoría con el nombre: " + categoria.getNombre());

        if (categoria.getActivo() == null)
            categoria.setActivo(true);
        categoria.setEmpresaId(empresaId);

        return categoriaGateway.guardarCategoria(categoria);
    }

    public Categoria actualizarCategoria(Long categoriaId, Categoria categoria, String empresaId) {
        Categoria existente = buscarCategoriaPorId(categoriaId, empresaId);
        validarEsPropia(existente);

        // Solo valida duplicado si el nombre cambió
        if (!existente.getNombre().equalsIgnoreCase(categoria.getNombre())) {
            if (categoriaGateway.existePorNombre(categoria.getNombre(), empresaId))
                throw new RuntimeException("Ya existe una categoría con el nombre: " + categoria.getNombre());
        }

        categoria.setCategoriaId(categoriaId);
        categoria.setEmpresaId(empresaId);
        return categoriaGateway.guardarCategoria(categoria);
    }

    public void eliminarCategoria(Long categoriaId, String empresaId) {
        Categoria existente = buscarCategoriaPorId(categoriaId, empresaId);
        validarEsPropia(existente);
        categoriaGateway.eliminarCategoria(categoriaId);
    }

    // Las categorías base compartidas (empresaId null, legado) se pueden ver y usar,
    // pero no editar ni eliminar — si alguien las cambiara, afectaría a todas las
    // demás empresas que también las ven. Para personalizar, hay que crear una propia.
    private void validarEsPropia(Categoria categoria) {
        if (categoria.getEmpresaId() == null)
            throw new RuntimeException("Esta es una categoría base compartida, no se puede editar ni eliminar. Creá una categoría propia en su lugar.");
    }
}
