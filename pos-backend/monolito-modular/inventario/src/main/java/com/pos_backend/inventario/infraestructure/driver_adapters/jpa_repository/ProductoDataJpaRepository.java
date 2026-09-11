package com.pos_backend.inventario.infraestructure.driver_adapters.jpa_repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductoDataJpaRepository extends JpaRepository<ProductoData, String> {
    List<ProductoData> findByEmpresaId(String empresaId);
    List<ProductoData> findByEmpresaIdAndCategoriaId(String empresaId, String categoriaId);
    List<ProductoData> findByEmpresaIdAndProveedorId(String empresaId, String proveedorId);
    Optional<ProductoData> findBySkuAndEmpresaId(String sku, String empresaId);
}