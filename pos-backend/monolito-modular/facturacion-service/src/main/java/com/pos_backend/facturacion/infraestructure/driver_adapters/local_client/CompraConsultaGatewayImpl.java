package com.pos_backend.facturacion.infraestructure.driver_adapters.local_client;

import com.pos_backend.compra.domain.model.Compra;
import com.pos_backend.compra.domain.model.CompraItem;
import com.pos_backend.compra.domain.usecase.CompraUseCase;
import com.pos_backend.facturacion.domain.model.CompraRemota;
import com.pos_backend.facturacion.domain.model.gateway.CompraConsultaGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

// Reemplaza el http_client original (RestClient a compra-service): ahora que
// compra ya está migrado, es una llamada directa al UseCase.
@Component("facturacionCompraConsultaGatewayImpl")
@RequiredArgsConstructor
public class CompraConsultaGatewayImpl implements CompraConsultaGateway {

    private final CompraUseCase compraUseCase;

    @Override
    public CompraRemota buscarCompra(Long compraId, String empresaId) {
        Compra c;
        try {
            c = compraUseCase.buscarPorId(compraId, empresaId);
        } catch (NoSuchElementException e) {
            return null;
        }
        return new CompraRemota(
                c.getCompraId(), c.getNumeroComprobante(), c.getTipoTransaccion(), c.getProveedorId(),
                c.getProveedorNombre(), c.getEstado(), itemsRemotos(c.getItems()), c.getTotalPagar(),
                c.getCufeProveedor(), c.getTieneCreditoProveedor());
    }

    private List<CompraRemota.ItemRemoto> itemsRemotos(List<CompraItem> items) {
        if (items == null) return List.of();
        List<CompraRemota.ItemRemoto> remotos = new ArrayList<>();
        for (CompraItem i : items) {
            remotos.add(new CompraRemota.ItemRemoto(
                    i.getProductoSku(), i.getDescripcion(), i.getCantidad(), i.getValorUnitario(),
                    i.getDescuento(), i.getImpuestoCargo(), i.getImpuestoRetencion()));
        }
        return remotos;
    }
}
