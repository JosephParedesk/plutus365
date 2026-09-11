package com.pos_backend.facturacion.domain.model.gateway;

import com.pos_backend.facturacion.domain.model.ConfiguracionDian;

public interface ConfiguracionDianGateway {
    ConfiguracionDian guardar(ConfiguracionDian configuracion);
    ConfiguracionDian buscarPorEmpresaId(String empresaId);

    /**
     * Reserva atómicamente el siguiente consecutivo de facturación electrónica
     * y lo devuelve. Nunca entrega el mismo número dos veces así se llame
     * concurrentemente para la misma empresa (usa un UPDATE condicionado en
     * BD, no un objeto en memoria — sirve incluso con varias instancias del
     * servicio corriendo).
     */
    long reservarSiguienteConsecutivo(String empresaId);
}
