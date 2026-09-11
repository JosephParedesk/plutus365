package com.pos_backend.inventario.infraestructure.driver_adapters.jpa_repository;

import com.pos_backend.inventario.domain.model.Producto;
import com.pos_backend.inventario.domain.model.gateway.ProductoGateway;
import com.pos_backend.inventario.infraestructure.mapper.ProductoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;


@Repository
@RequiredArgsConstructor
public class ProductoDataGatewayImpl implements ProductoGateway {

    private final ProductoDataJpaRepository productoDataJpaRepository;
    private final ProductoMapper productoMapper;

    @Override
    public Producto guardarProducto(Producto producto) {
        ProductoData saved = productoDataJpaRepository
                .save(productoMapper.toProductoData(producto));
        return productoMapper.toProducto(saved);
    }

    @Override
    public Producto buscarProductoPorSku(String sku, String empresaId) {
        return productoDataJpaRepository.findBySkuAndEmpresaId(sku, empresaId)
                .map(productoMapper::toProducto)
                .orElse(null);
    }

    @Override
    public void eliminarProductoPorSku(String sku, String empresaId) {
        productoDataJpaRepository.findBySkuAndEmpresaId(sku, empresaId)
                .ifPresent(productoDataJpaRepository::delete);
    }

    @Override
    public List<Producto> listarProductos(String empresaId) {
        return productoDataJpaRepository.findByEmpresaId(empresaId)
                .stream().map(productoMapper::toProducto).toList();
    }

    @Override
    public List<Producto> listarPorCategoria(String categoriaId, String empresaId) {
        return productoDataJpaRepository.findByEmpresaIdAndCategoriaId(empresaId, categoriaId)
                .stream().map(productoMapper::toProducto).toList();
    }

    @Override
    public List<Producto> listarPorProveedor(String proveedorId, String empresaId) {
        return productoDataJpaRepository.findByEmpresaIdAndProveedorId(empresaId, proveedorId)
                .stream().map(productoMapper::toProducto).toList();
    }

    @Override
    public List<Producto> listarStockBajo(String empresaId) {
        return productoDataJpaRepository.findByEmpresaId(empresaId).stream()
                .filter(p -> p.getStockMinimo() != null && p.getStock() <= p.getStockMinimo())
                .map(productoMapper::toProducto).toList();
    }

    @Override
    public Producto descontarStock(String sku, String empresaId, Integer cantidad) {
        ProductoData data = productoDataJpaRepository.findBySkuAndEmpresaId(sku, empresaId)
                .orElseThrow(() -> new java.util.NoSuchElementException("Producto no encontrado: " + sku));
        data.setStock(data.getStock() - cantidad);
        ProductoData saved = productoDataJpaRepository.save(data);
        return productoMapper.toProducto(saved);
    }

    @Override
    public Producto incrementarStock(String sku, String empresaId, Integer cantidad) {
        ProductoData data = productoDataJpaRepository.findBySkuAndEmpresaId(sku, empresaId)
                .orElseThrow(() -> new java.util.NoSuchElementException("Producto no encontrado: " + sku));
        data.setStock(data.getStock() + cantidad);
        ProductoData saved = productoDataJpaRepository.save(data);
        return productoMapper.toProducto(saved);
    }
}