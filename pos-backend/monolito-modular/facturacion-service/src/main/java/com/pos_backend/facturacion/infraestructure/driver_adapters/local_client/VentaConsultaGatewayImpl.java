package com.pos_backend.facturacion.infraestructure.driver_adapters.local_client;

import com.pos_backend.facturacion.domain.model.VentaRemota;
import com.pos_backend.facturacion.domain.model.gateway.VentaConsultaGateway;
import com.pos_backend.venta.domain.model.Venta;
import com.pos_backend.venta.domain.model.VentaItem;
import com.pos_backend.venta.domain.usecase.VentaUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

// Reemplaza el http_client original (RestClient a venta-service): ahora que
// venta-service ya está migrado, es una llamada directa al UseCase.
@Component("facturacionVentaConsultaGatewayImpl")
@RequiredArgsConstructor
public class VentaConsultaGatewayImpl implements VentaConsultaGateway {

    private final VentaUseCase ventaUseCase;

    @Override
    public VentaRemota buscarVenta(Long ventaId, String empresaId) {
        Venta v;
        try {
            v = ventaUseCase.buscarPorId(ventaId, empresaId);
        } catch (NoSuchElementException e) {
            return null;
        }
        return new VentaRemota(
                v.getVentaId(), v.getNumeroVenta(), v.getClienteId(), v.getClienteNombre(),
                v.getFecha() != null ? v.getFecha().toString() : null, v.getEstado(),
                itemsRemotos(v.getItems()), v.getSubtotal(), v.getDescuentoTotal(), v.getTotalIva(), v.getTotal());
    }

    private List<VentaRemota.ItemRemoto> itemsRemotos(List<VentaItem> items) {
        if (items == null) return List.of();
        List<VentaRemota.ItemRemoto> remotos = new ArrayList<>();
        for (VentaItem i : items) {
            remotos.add(new VentaRemota.ItemRemoto(
                    i.getSku(), i.getNombreProducto(), i.getCantidad(), i.getPrecioUnitario(), i.getDescuento(),
                    i.getValorTotal(), i.getTipoIva(), i.getValorIva()));
        }
        return remotos;
    }
}
