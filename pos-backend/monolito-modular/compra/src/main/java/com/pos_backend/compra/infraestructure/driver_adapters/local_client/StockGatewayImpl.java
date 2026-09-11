package com.pos_backend.compra.infraestructure.driver_adapters.local_client;

import com.pos_backend.compra.domain.model.gateway.StockGateway;
import com.pos_backend.inventario.domain.model.Producto;
import com.pos_backend.inventario.domain.model.RegistroCompraStock;
import com.pos_backend.inventario.domain.usecase.ProductoUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// Reemplaza el http_client original (RestClient a inventario-service): ahora
// que inventario ya está migrado, es una llamada directa al UseCase.
@Component("compraStockGatewayImpl")
@RequiredArgsConstructor
public class StockGatewayImpl implements StockGateway {

    private final ProductoUseCase productoUseCase;

    @Override
    public ResultadoStock registrarCompra(
            String sku, String nombre, Integer cantidad, Double precioCompra,
            Long proveedorId, String proveedorNombre, String unidad, String documentoOrigen, String empresaId) {

        RegistroCompraStock datos = new RegistroCompraStock(
                sku, nombre, cantidad, precioCompra,
                proveedorId != null ? String.valueOf(proveedorId) : null,
                proveedorNombre, null, unidad, documentoOrigen);

        ProductoUseCase.ResultadoRegistroStock resultado = productoUseCase.registrarCompra(datos, empresaId);
        Producto producto = resultado.producto();
        return new ResultadoStock(resultado.creado(), producto != null ? producto.getNombre() : nombre);
    }

    @Override
    public void revertir(String sku, Integer cantidad, String empresaId) {
        try {
            productoUseCase.descontarStock(sku, empresaId, cantidad);
        } catch (Exception ignored) {
            // best-effort: si esto falla, ya se está propagando una excepción más importante que esta
        }
    }
}
