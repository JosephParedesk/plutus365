package com.pos_backend.nomina.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConceptoHoraExtraTest {

    @Test
    void losTresTiposFijosPorLeyTraenPorcentajeSugerido() {
        assertEquals(25.0, ConceptoHoraExtra.porCodigo(1).getPorcentajeSugerido()); // extra diurna
        assertEquals(75.0, ConceptoHoraExtra.porCodigo(2).getPorcentajeSugerido()); // extra nocturna
        assertEquals(35.0, ConceptoHoraExtra.porCodigo(3).getPorcentajeSugerido()); // recargo nocturno
    }

    @Test
    void losTiposCombinadosConDominicalNoTraenPorcentajeInventado() {
        // 4 y 6 combinan extra con dominical/festivo — sin fórmula única en la ley,
        // no hay que adivinar.
        assertNull(ConceptoHoraExtra.porCodigo(4).getPorcentajeSugerido());
        assertNull(ConceptoHoraExtra.porCodigo(6).getPorcentajeSugerido());
        assertNull(ConceptoHoraExtra.porCodigo(7).getPorcentajeSugerido());
    }

    @Test
    void rechazaUnCodigoDesconocido() {
        assertThrows(IllegalArgumentException.class, () -> ConceptoHoraExtra.porCodigo(99));
    }
}
