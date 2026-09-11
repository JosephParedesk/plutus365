package com.pos_backend.proveedor.infraestructure.driver_adapters.jpa_repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProveedorDataJpaRepository extends JpaRepository<ProveedorData, Long> {

    // "Visible para la empresa" = suyo propio o un proveedor base compartido (empresaId null, legado).
    @Query("SELECT p FROM ProveedorData p WHERE p.empresaId = :empresaId OR p.empresaId IS NULL")
    List<ProveedorData> listarVisiblesParaEmpresa(@Param("empresaId") String empresaId);

    @Query("SELECT p FROM ProveedorData p WHERE p.activo = true AND (p.empresaId = :empresaId OR p.empresaId IS NULL)")
    List<ProveedorData> listarActivosVisiblesParaEmpresa(@Param("empresaId") String empresaId);

    @Query("SELECT p FROM ProveedorData p WHERE p.proveedorId = :id AND (p.empresaId = :empresaId OR p.empresaId IS NULL)")
    Optional<ProveedorData> buscarPorIdVisibleParaEmpresa(@Param("id") Long id, @Param("empresaId") String empresaId);

    @Query("SELECT p FROM ProveedorData p WHERE p.nit = :nit AND (p.empresaId = :empresaId OR p.empresaId IS NULL)")
    Optional<ProveedorData> buscarPorNitVisibleParaEmpresa(@Param("nit") String nit, @Param("empresaId") String empresaId);

    @Query("SELECT COUNT(p) > 0 FROM ProveedorData p WHERE p.nit = :nit AND (p.empresaId = :empresaId OR p.empresaId IS NULL)")
    boolean existsByNitVisibleParaEmpresa(@Param("nit") String nit, @Param("empresaId") String empresaId);

    @Query("SELECT COUNT(p) > 0 FROM ProveedorData p WHERE LOWER(p.nombre) = LOWER(:nombre) AND (p.empresaId = :empresaId OR p.empresaId IS NULL)")
    boolean existsByNombreVisibleParaEmpresa(@Param("nombre") String nombre, @Param("empresaId") String empresaId);
}
