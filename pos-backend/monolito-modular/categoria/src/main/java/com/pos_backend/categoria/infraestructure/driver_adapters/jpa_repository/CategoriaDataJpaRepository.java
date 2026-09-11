package com.pos_backend.categoria.infraestructure.driver_adapters.jpa_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoriaDataJpaRepository extends JpaRepository<CategoriaData, Long> {

    // "Visible para la empresa" = suya propia o una base compartida (empresaId null, legado).
    @Query("SELECT c FROM CategoriaData c WHERE c.empresaId = :empresaId OR c.empresaId IS NULL")
    List<CategoriaData> listarVisiblesParaEmpresa(@Param("empresaId") String empresaId);

    @Query("SELECT c FROM CategoriaData c WHERE c.activo = true AND (c.empresaId = :empresaId OR c.empresaId IS NULL)")
    List<CategoriaData> listarActivasVisiblesParaEmpresa(@Param("empresaId") String empresaId);

    @Query("SELECT c FROM CategoriaData c WHERE c.categoriaId = :id AND (c.empresaId = :empresaId OR c.empresaId IS NULL)")
    Optional<CategoriaData> buscarPorIdVisibleParaEmpresa(@Param("id") Long id, @Param("empresaId") String empresaId);

    @Query("SELECT COUNT(c) > 0 FROM CategoriaData c WHERE LOWER(c.nombre) = LOWER(:nombre) " +
            "AND (c.empresaId = :empresaId OR c.empresaId IS NULL)")
    boolean existsByNombreVisibleParaEmpresa(@Param("nombre") String nombre, @Param("empresaId") String empresaId);
}
