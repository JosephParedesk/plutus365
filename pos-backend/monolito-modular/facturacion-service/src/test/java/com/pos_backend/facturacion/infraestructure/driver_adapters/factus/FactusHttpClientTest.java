package com.pos_backend.facturacion.infraestructure.driver_adapters.factus;

import com.pos_backend.facturacion.domain.model.ConfiguracionDian;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Cubre el switch sandbox/producción por empresa (agregado hoy en el panel de
// super admin) — factusSandbox=true debe mandar a PRUEBAS, false/null a
// PRODUCCION. baseUrl() usa la misma condición pero es privado; este es el
// equivalente público, ver el mismo comentario en FactusHttpClient.
class FactusHttpClientTest {

    private ConfiguracionDian config(Boolean factusSandbox) {
        ConfiguracionDian c = new ConfiguracionDian();
        c.setFactusSandbox(factusSandbox);
        return c;
    }

    @Test
    void ambiente_sandboxTrue_esPruebas() {
        assertEquals("PRUEBAS", FactusHttpClient.ambiente(config(true)));
    }

    @Test
    void ambiente_sandboxFalse_esProduccion() {
        assertEquals("PRODUCCION", FactusHttpClient.ambiente(config(false)));
    }

    @Test
    void ambiente_sandboxNull_esProduccionPorDefecto() {
        assertEquals("PRODUCCION", FactusHttpClient.ambiente(config(null)));
    }
}
