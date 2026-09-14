package com.pos_backend.facturacion.infraestructure.driver_adapters.factus;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NitDvTest {

    @Test
    void calculaDvConNitsReales() {
        assertEquals(4, FactusFacturaElectronicaGatewayImpl.calcularDv("800197268")); // DIAN
        assertEquals(0, FactusFacturaElectronicaGatewayImpl.calcularDv("71452262"));  // cliente Factus
    }

    @Test
    void separaNumeroYDv() {
        assertArrayEquals(new String[]{"800197268", "4"}, FactusFacturaElectronicaGatewayImpl.nitYDv("800.197.268-4", "X"));
        assertArrayEquals(new String[]{"900123456", "8"}, FactusFacturaElectronicaGatewayImpl.nitYDv("900123456", "X"));
        assertThrows(RuntimeException.class, () -> FactusFacturaElectronicaGatewayImpl.nitYDv("900111220-0", "X"));
    }
}
