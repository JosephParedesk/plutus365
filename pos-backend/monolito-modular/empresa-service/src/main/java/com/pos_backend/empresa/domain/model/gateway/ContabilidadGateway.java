package com.pos_backend.empresa.domain.model.gateway;

public interface ContabilidadGateway {

    /**
     * Dispara la siembra del plan de cuentas (PUC) de la empresa si todavía no
     * la tiene. Es best-effort: si contabilidad-service no responde, no debe
     * bloquear la configuración de la empresa (la siembra igual se dispara sola
     * la próxima vez que alguien liste las cuentas).
     */
    void sembrarPlanDeCuentas(String empresaId);
}
