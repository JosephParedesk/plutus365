package com.pos_backend.venta.infraestructure.driver_adapters.local_client;

import com.pos_backend.contabilidad.domain.model.ReciboCajaParaAsiento;
import com.pos_backend.contabilidad.domain.usecase.AsientoContableUseCase;
import com.pos_backend.venta.domain.model.ReciboCaja;
import com.pos_backend.venta.domain.model.gateway.ReciboContabilidadGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// Reemplaza el http_client original (RestClient a contabilidad-service, POST
// /desde-recibo-caja): ver el mismo comentario en ContabilidadGatewayImpl de
// este mismo paquete.
@Component("ventaReciboContabilidadGatewayImpl")
@RequiredArgsConstructor
public class ReciboContabilidadGatewayImpl implements ReciboContabilidadGateway {

    private final AsientoContableUseCase asientoContableUseCase;

    @Override
    public void generarAsientoRecibo(ReciboCaja recibo, String empresaId) {
        ReciboCajaParaAsiento datos = new ReciboCajaParaAsiento();
        datos.setReciboId(recibo.getReciboId());
        datos.setNumeroRecibo(recibo.getNumeroRecibo());
        datos.setFecha(recibo.getFechaRecibido());
        datos.setTotal(recibo.getTotalRecibido());
        datos.setOrigenDinero(recibo.getOrigenDinero());
        datos.setEsAnticipo("ANTICIPO".equals(recibo.getTipoRecibo()));

        asientoContableUseCase.generarDesdeReciboCaja(
                datos, empresaId, recibo.getCreadoPor() != null ? recibo.getCreadoPor() : "Sistema");
    }
}
