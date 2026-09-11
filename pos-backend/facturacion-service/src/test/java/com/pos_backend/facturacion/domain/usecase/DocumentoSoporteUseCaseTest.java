package com.pos_backend.facturacion.domain.usecase;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Check mínimo del parser de porcentajes de compra (texto libre de un dropdown fijo,
 * ver NuevaCompraPage.tsx) — es lo que más fácil se rompe con una coma en vez de punto
 * o un formato inesperado.
 */
class DocumentoSoporteUseCaseTest {

    @Test
    void extraeElPorcentajeConPuntoDecimal() {
        assertEquals(0.19, DocumentoSoporteUseCase.porcentaje("IVA 19%"), 0.0001);
        assertEquals(0.05, DocumentoSoporteUseCase.porcentaje("IVA 5%"), 0.0001);
        assertEquals(0.0, DocumentoSoporteUseCase.porcentaje("IVA 0%"), 0.0001);
    }

    @Test
    void extraeElPorcentajeConComaDecimalColombiana() {
        assertEquals(0.035, DocumentoSoporteUseCase.porcentaje("Retefuente 3,5% Arrendamientos"), 0.0001);
        assertEquals(0.025, DocumentoSoporteUseCase.porcentaje("Retefuente 2,5% Compras"), 0.0001);
        assertEquals(0.01, DocumentoSoporteUseCase.porcentaje("Retefuente 1% Compras"), 0.0001);
    }

    @Test
    void devuelveCeroSinPorcentajeOSinTexto() {
        assertEquals(0.0, DocumentoSoporteUseCase.porcentaje("Sin impuesto"), 0.0001);
        assertEquals(0.0, DocumentoSoporteUseCase.porcentaje("Sin retención"), 0.0001);
        assertEquals(0.0, DocumentoSoporteUseCase.porcentaje(null), 0.0001);
    }
}
