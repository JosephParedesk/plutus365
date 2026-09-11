package com.pos_backend.empresa.infraestructure.driver_adapters.local_client;

import com.pos_backend.contabilidad.domain.usecase.CuentaContableUseCase;
import com.pos_backend.empresa.domain.model.gateway.ContabilidadGateway;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

// Reemplaza el http_client original (RestClient a contabilidad-service): ahora
// que contabilidad-service ya está migrado al monolito, es una llamada directa
// al UseCase en vez de una llamada de red. Mismo comportamiento (CuentaContableUseCase.listar
// ya dispara la siembra del PUC internamente si la empresa no tiene cuentas
// todavía — ver CuentaContableUseCase.sembrarBaseSiEsNecesario), mismo criterio
// best-effort (nunca debe tumbar la configuración de la empresa).
// Nombre de bean explícito: venta-service y compra también tienen una clase
// ContabilidadGatewayImpl — ver el comentario en
// categoria/application/config/UseCaseConfig.java.
@Component("empresaContabilidadGatewayImpl")
@RequiredArgsConstructor
public class ContabilidadGatewayImpl implements ContabilidadGateway {

    private static final Logger log = LoggerFactory.getLogger(ContabilidadGatewayImpl.class);

    private final CuentaContableUseCase cuentaContableUseCase;

    @Override
    public void sembrarPlanDeCuentas(String empresaId) {
        try {
            cuentaContableUseCase.listar(empresaId);
        } catch (Exception e) {
            log.warn("No se pudo sembrar el plan de cuentas para empresa {} al configurarla: {}", empresaId, e.getMessage());
        }
    }
}
