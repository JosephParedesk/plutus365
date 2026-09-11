package com.pos_backend.inventario.infraestructure.driver_adapters.local_client;

import com.pos_backend.contabilidad.domain.model.AsientoContable;
import com.pos_backend.contabilidad.domain.model.SaldoInicialInventarioParaAsiento;
import com.pos_backend.contabilidad.domain.usecase.AsientoContableUseCase;
import com.pos_backend.inventario.domain.model.gateway.ContabilidadGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

// Reemplaza el http_client original (RestClient a contabilidad-service, POST
// /desde-saldo-inicial-inventario): ahora que contabilidad-service ya está
// migrado, es una llamada directa al UseCase. El original extraía el campo
// "message" del body de error HTTP y lo relanzaba como RuntimeException; acá
// el UseCase ya lanza RuntimeException con ese mismo mensaje directamente, así
// que no hace falta ningún try/catch de conversión — se deja propagar tal cual.
@Component("inventarioContabilidadGatewayImpl")
@RequiredArgsConstructor
public class ContabilidadGatewayImpl implements ContabilidadGateway {

    private final AsientoContableUseCase asientoContableUseCase;

    @Override
    public String registrarSaldoInicialInventario(double valorTotal, String cuentaContrapartida, LocalDate fechaCorte,
                                                    String empresaId, String creadoPor) {
        SaldoInicialInventarioParaAsiento datos = new SaldoInicialInventarioParaAsiento(
                fechaCorte != null ? fechaCorte : LocalDate.now(), valorTotal, cuentaContrapartida);
        AsientoContable asiento = asientoContableUseCase.generarDesdeSaldoInicialInventario(
                datos, empresaId, creadoPor != null ? creadoPor : "Sistema");
        return asiento != null ? asiento.getNumero() : null;
    }
}
