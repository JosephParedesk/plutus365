package com.pos_backend.venta.infraestructure.driver_adapters.local_client;

import com.pos_backend.contabilidad.domain.usecase.AsientoContableUseCase;
import com.pos_backend.venta.domain.model.FormaPago;
import com.pos_backend.venta.domain.model.Venta;
import com.pos_backend.venta.domain.model.gateway.ContabilidadGateway;
import com.pos_backend.contabilidad.domain.model.VentaParaAsiento;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

// Reemplaza el http_client original (RestClient a contabilidad-service, POST
// /desde-venta): ahora que contabilidad-service ya está migrado, es una
// llamada directa a AsientoContableUseCase.generarDesdeVenta. Ya no hace
// falta el try/catch de conversión de excepción — el UseCase ya lanza
// RuntimeException con el mismo mensaje que antes había que extraer del body.
@Component("ventaContabilidadGatewayImpl")
@RequiredArgsConstructor
public class ContabilidadGatewayImpl implements ContabilidadGateway {

    private final AsientoContableUseCase asientoContableUseCase;

    @Override
    public void generarAsientoVenta(Venta venta, String empresaId) {
        VentaParaAsiento datos = new VentaParaAsiento();
        datos.setVentaId(venta.getVentaId());
        datos.setNumeroVenta(venta.getNumeroVenta());
        datos.setFecha(venta.getFecha().toLocalDate());
        datos.setClienteId(venta.getClienteId());
        datos.setSubtotal(venta.getSubtotal());
        datos.setDescuentoTotal(venta.getDescuentoTotal() != null ? venta.getDescuentoTotal() : 0.0);
        datos.setTotalIva(venta.getTotalIva() != null ? venta.getTotalIva() : 0.0);
        datos.setTotal(venta.getTotal());
        datos.setFormasPago(formasPagoAsiento(venta.getFormasPago()));

        asientoContableUseCase.generarDesdeVenta(
                datos, empresaId, venta.getCreadoPor() != null ? venta.getCreadoPor() : "Sistema");
    }

    private List<VentaParaAsiento.FormaPagoAsiento> formasPagoAsiento(List<FormaPago> formasPago) {
        List<VentaParaAsiento.FormaPagoAsiento> formas = new ArrayList<>();
        for (FormaPago fp : formasPago)
            formas.add(new VentaParaAsiento.FormaPagoAsiento(fp.getMetodo(), fp.getValor()));
        return formas;
    }
}
