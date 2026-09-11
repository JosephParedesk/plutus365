package com.pos_backend.categoria.infraestructure.driver_adapters.jpa_repository;


import com.pos_backend.categoria.domain.model.Categoria;
import com.pos_backend.categoria.domain.model.gateway.CategoriaGateway;
import com.pos_backend.categoria.infraestructure.mapper.CategoriaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CategoriaDataGatewayImpl implements CategoriaGateway {

    private final CategoriaDataJpaRepository categoriaDataJpaRepository;
    private final CategoriaMapper categoriaMapper;

    @Override
    public Categoria guardarCategoria(Categoria categoria) {
        CategoriaData saved = categoriaDataJpaRepository
                .save(categoriaMapper.toCategoriaData(categoria));
        return categoriaMapper.toCategoria(saved);
    }

    @Override
    public Categoria buscarCategoriaPorId(Long categoriaId, String empresaId) {
        return categoriaDataJpaRepository.buscarPorIdVisibleParaEmpresa(categoriaId, empresaId)
                .map(categoriaMapper::toCategoria)
                .orElse(null);
    }

    @Override
    public List<Categoria> listarCategorias(String empresaId) {
        return categoriaDataJpaRepository.listarVisiblesParaEmpresa(empresaId)
                .stream().map(categoriaMapper::toCategoria).toList();
    }

    @Override
    public List<Categoria> listarCategoriasActivas(String empresaId) {
        return categoriaDataJpaRepository.listarActivasVisiblesParaEmpresa(empresaId)
                .stream().map(categoriaMapper::toCategoria).toList();
    }

    @Override
    public void eliminarCategoria(Long categoriaId) {
        categoriaDataJpaRepository.deleteById(categoriaId);
    }

    @Override
    public boolean existePorNombre(String nombre, String empresaId) {
        return categoriaDataJpaRepository.existsByNombreVisibleParaEmpresa(nombre, empresaId);
    }
}
