package com.pos_backend.inventario.infraestructure.mapper;

import com.pos_backend.inventario.domain.model.Producto;
import com.pos_backend.inventario.infraestructure.driver_adapters.jpa_repository.ProductoData;
import org.springframework.stereotype.Component;

@Component
public class ProductoMapper {

    // Espejo del mapa en venta-service (VentaUseCase) y facturacion-service (UblXmlBuilder) —
    // no hay librería compartida entre microservicios en este proyecto.
    private static final java.util.Map<String, Double> TASA_IVA = java.util.Map.of(
            "GENERAL_19", 0.19,
            "REDUCIDO_5", 0.05,
            "EXENTO", 0.0,
            "EXCLUIDO", 0.0
    );

    public ProductoData toProductoData(Producto producto) {
        ProductoData data = new ProductoData();
        data.setSku(producto.getSku());
        data.setEmpresaId(producto.getEmpresaId()); // ← agrega esto
        data.setNombre(producto.getNombre());
        data.setDescripcion(producto.getDescripcion());
        data.setCategoriaId(producto.getCategoriaId());
        data.setProveedorId(producto.getProveedorId());
        data.setPrecioCompra(producto.getPrecioCompra() != null ? producto.getPrecioCompra() : 0.0);
        data.setPrecioVenta(producto.getPrecioVenta() != null ? producto.getPrecioVenta() : 0.0);
        data.setStock(producto.getStock() != null ? producto.getStock() : 0);
        data.setStockMinimo(producto.getStockMinimo() != null ? producto.getStockMinimo() : 0);
        data.setUnidad(producto.getUnidad());
        data.setImagenUrl(producto.getImagenUrl());
        data.setActivo(producto.getActivo() != null ? producto.getActivo() : true);
        data.setTipoIva(producto.getTipoIva() != null && !producto.getTipoIva().isBlank() ? producto.getTipoIva() : "GENERAL_19");
        return data;
    }

    public Producto toProducto(ProductoData data) {
        if (data == null) return new Producto();

        double compra = data.getPrecioCompra() != null ? data.getPrecioCompra() : 0.0;
        double venta  = data.getPrecioVenta()  != null ? data.getPrecioVenta()  : 0.0;
        String tipoIva = data.getTipoIva() != null ? data.getTipoIva() : "GENERAL_19";
        double tasa = TASA_IVA.getOrDefault(tipoIva, 0.19);
        // venta es el precio al público CON IVA — la ganancia se calcula sobre la base
        // sin IVA, porque el IVA no es utilidad del negocio, es un impuesto de paso.
        double ventaBase          = venta / (1 + tasa);
        double gananciaPesos      = ventaBase - compra;
        double gananciaPorcentaje = compra > 0 ? (gananciaPesos / compra) * 100 : 0.0;

        Producto p = new Producto();
        p.setSku(data.getSku());
        p.setEmpresaId(data.getEmpresaId()); // ← agrega esto
        p.setNombre(data.getNombre());
        p.setDescripcion(data.getDescripcion());
        p.setCategoriaId(data.getCategoriaId());
        p.setProveedorId(data.getProveedorId());
        p.setPrecioCompra(compra);
        p.setPrecioVenta(venta);
        p.setGananciaPesos(gananciaPesos);
        p.setGananciaPorcentaje(Math.round(gananciaPorcentaje * 100.0) / 100.0);
        p.setStock(data.getStock());
        p.setStockMinimo(data.getStockMinimo());
        p.setUnidad(data.getUnidad());
        p.setImagenUrl(data.getImagenUrl());
        p.setActivo(data.getActivo());
        p.setTipoIva(data.getTipoIva() != null ? data.getTipoIva() : "GENERAL_19");
        return p;
    }

    public ProductoData topruductoData(Producto producto) { return toProductoData(producto); }
    public Producto topruducto(ProductoData data)         { return toProducto(data); }
}