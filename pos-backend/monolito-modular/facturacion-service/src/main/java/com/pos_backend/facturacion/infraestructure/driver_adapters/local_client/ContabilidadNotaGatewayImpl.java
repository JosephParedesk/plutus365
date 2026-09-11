package com.pos_backend.facturacion.infraestructure.driver_adapters.local_client;

import com.pos_backend.contabilidad.domain.model.NotaCreditoParaAsiento;
import com.pos_backend.contabilidad.domain.usecase.AsientoContableUseCase;
import com.pos_backend.facturacion.domain.model.NotaCredito;
import com.pos_backend.facturacion.domain.model.gateway.ContabilidadNotaGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// Reemplaza el http_client original (RestClient a contabilidad-service, POST
// /desde-nota-credito): ya no hace falta el try/catch de conversión de
// excepción — el UseCase ya lanza RuntimeException con el mismo mensaje.
@Component("facturacionContabilidadNotaGatewayImpl")
@RequiredArgsConstructor
public class ContabilidadNotaGatewayImpl implements ContabilidadNotaGateway {

    private final AsientoContableUseCase asientoContableUseCase;

    @Override
    public void generarAsiento(NotaCredito nota, String metodoPagoOriginal, String empresaId) {
        NotaCreditoParaAsiento datos = new NotaCreditoParaAsiento(
                nota.getNotaCreditoId(), nota.getNumeroNota(),
                nota.getFechaEmision() != null ? nota.getFechaEmision().toLocalDate() : null,
                nota.getSubtotal(), nota.getTotalIva(), nota.getTotal(), metodoPagoOriginal);

        asientoContableUseCase.generarDesdeNotaCredito(
                datos, empresaId, nota.getCreadoPor() != null ? nota.getCreadoPor() : "Sistema");
    }
}
