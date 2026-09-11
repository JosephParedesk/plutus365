package com.pos_backend.compra.infraestructure.driver_adapters.local_client;

import com.pos_backend.compra.domain.model.Compra;
import com.pos_backend.compra.domain.model.CompraItem;
import com.pos_backend.compra.domain.model.FormaPago;
import com.pos_backend.compra.domain.model.gateway.ContabilidadGateway;
import com.pos_backend.contabilidad.domain.model.CompraParaAsiento;
import com.pos_backend.contabilidad.domain.usecase.AsientoContableUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

// Reemplaza el http_client original (RestClient a contabilidad-service, POST
// /desde-compra): ahora que contabilidad-service ya está migrado, es una
// llamada directa a AsientoContableUseCase.generarDesdeCompra. Ya no hace
// falta el try/catch que extraía "message" del body HTTP de error — el
// UseCase ya lanza RuntimeException con ese mismo mensaje directamente.
@Component("compraContabilidadGatewayImpl")
@RequiredArgsConstructor
public class ContabilidadGatewayImpl implements ContabilidadGateway {

    private final AsientoContableUseCase asientoContableUseCase;

    @Override
    public void generarAsientoCompra(Compra compra, String empresaId) {
        CompraParaAsiento datos = new CompraParaAsiento();
        datos.setCompraId(compra.getCompraId());
        datos.setTipoTransaccion(compra.getTipoTransaccion());
        datos.setNumeroComprobante(compra.getNumeroComprobante());
        datos.setFecha(compra.getFechaElaboracion());
        datos.setProveedorId(compra.getProveedorId());
        datos.setTotalPagar(compra.getTotalPagar());
        datos.setTotalIva(compra.getTotalIva() != null ? compra.getTotalIva() : 0.0);
        datos.setTieneCreditoProveedor(Boolean.TRUE.equals(compra.getTieneCreditoProveedor()));
        datos.setCompraReferenciaId(compra.getCompraReferenciaId());
        datos.setItems(itemsAsiento(compra));
        datos.setFormasPago(formasPagoAsiento(compra));

        asientoContableUseCase.generarDesdeCompra(
                datos, empresaId, compra.getCreadoPor() != null ? compra.getCreadoPor() : "Sistema");
    }

    private List<CompraParaAsiento.ItemAsiento> itemsAsiento(Compra compra) {
        if (compra.getItems() == null) return List.of();
        List<CompraParaAsiento.ItemAsiento> items = new ArrayList<>();
        for (CompraItem i : compra.getItems()) {
            items.add(new CompraParaAsiento.ItemAsiento(
                    i.getTipo() != null ? i.getTipo() : "GASTO_CUENTA",
                    i.getDescripcion() != null ? i.getDescripcion() : "",
                    i.getValorTotal() != null ? i.getValorTotal() : 0.0,
                    i.getCuentaContableCodigo()));
        }
        return items;
    }

    // RECIBO_PAGO no usa formasPago (usa origenDinero, texto libre); se traduce
    // aquí a una forma de pago sintética para que contabilidad-service decida la cuenta.
    private List<CompraParaAsiento.FormaPagoAsiento> formasPagoAsiento(Compra compra) {
        if (compra.getFormasPago() != null && !compra.getFormasPago().isEmpty()) {
            List<CompraParaAsiento.FormaPagoAsiento> formas = new ArrayList<>();
            for (FormaPago fp : compra.getFormasPago())
                formas.add(new CompraParaAsiento.FormaPagoAsiento(fp.getMetodo(), fp.getValor()));
            return formas;
        }
        if (compra.getOrigenDinero() != null) {
            String metodo = compra.getOrigenDinero().toLowerCase().contains("efectivo") ? "EFECTIVO" : "TRANSFERENCIA";
            return List.of(new CompraParaAsiento.FormaPagoAsiento(metodo, compra.getTotalPagar()));
        }
        return List.of();
    }
}
