package com.pos_backend.facturacion.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConceptoNotaDebitoTest {

    @Test
    void mapeaLosCuatroCodigosDeFactus() {
        assertEquals(ConceptoNotaDebito.INTERESES, ConceptoNotaDebito.porCodigo("1"));
        assertEquals(ConceptoNotaDebito.GASTOS_POR_COBRAR, ConceptoNotaDebito.porCodigo("2"));
        assertEquals(ConceptoNotaDebito.CAMBIO_VALOR, ConceptoNotaDebito.porCodigo("3"));
        assertEquals(ConceptoNotaDebito.OTROS, ConceptoNotaDebito.porCodigo("4"));
    }

    @Test
    void rechazaUnCodigoDesconocidoEnVezDeAdivinar() {
        assertThrows(IllegalArgumentException.class, () -> ConceptoNotaDebito.porCodigo("99"));
    }
}
