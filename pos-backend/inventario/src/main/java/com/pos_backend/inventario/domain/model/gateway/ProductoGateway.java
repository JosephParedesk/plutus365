package com.pos_backend.inventario.domain.model.gateway;

import com.pos_backend.inventario.domain.model.Producto;
import java.util.List;

public interface ProductoGateway {
    Producto guardarProducto(Producto producto);
    Producto buscarProductoPorSku(String sku, String empresaId);
    void eliminarProductoPorSku(String sku, String empresaId);
    List<Producto> listarProductos(String empresaId);
    List<Producto> listarPorCategoria(String categoriaId, String empresaId);
    List<Producto> listarPorProveedor(String proveedorId, String empresaId);
    List<Producto> listarStockBajo(String empresaId);
    Producto descontarStock(String sku, String empresaId, Integer cantidad);
    Producto incrementarStock(String sku, String empresaId, Integer cantidad);
}