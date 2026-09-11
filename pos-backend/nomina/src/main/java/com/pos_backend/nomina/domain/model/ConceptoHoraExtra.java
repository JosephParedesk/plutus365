package com.pos_backend.nomina.domain.model;

/**
 * Tipos de hora extra/recargo, código DIAN según la tabla de Factus
 * (developers.factus.com.co/tablas-de-referencia/tablas-referencia-nomina/).
 *
 * Porcentajes verificados contra el Código Sustantivo del Trabajo:
 *  - Art. 168: nocturno 35%, extra diurna 25%, extra nocturna 75% (fijos, no
 *    cambiaron con la reforma laboral).
 *  - Art. 179 (recargo dominical/festivo): en implementación GRADUAL por la
 *    Ley 2466/2025 — 80% desde jul/2025, 90% desde jul/2026, 100% desde jul/2027.
 *    Los 4 tipos que combinan dominical con nocturno/extra (4, 5, 6, 7) no tienen
 *    un porcentaje único fijado en el texto de la ley — se sugiere null a
 *    propósito para que el usuario/contador lo confirme, en vez de adivinar una
 *    fórmula de acumulación.
 */
public enum ConceptoHoraExtra {

    EXTRA_DIURNA(1, "Hora extra diurna", 25.0),
    EXTRA_NOCTURNA(2, "Hora extra nocturna", 75.0),
    RECARGO_NOCTURNO(3, "Hora recargo nocturno", 35.0),
    EXTRA_DIURNA_DOMINICAL(4, "Hora extra diurna dominical y festivos", null),
    RECARGO_DIURNO_DOMINICAL(5, "Hora recargo diurno dominical y festivos", 90.0),
    EXTRA_NOCTURNA_DOMINICAL(6, "Hora extra nocturna dominical y festivos", null),
    RECARGO_NOCTURNO_DOMINICAL(7, "Hora recargo nocturno dominical y festivos", null);

    private final int codigo;
    private final String descripcion;
    private final Double porcentajeSugerido; // null = sin sugerencia confiable, que lo confirme el usuario

    ConceptoHoraExtra(int codigo, String descripcion, Double porcentajeSugerido) {
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.porcentajeSugerido = porcentajeSugerido;
    }

    public int getCodigo() { return codigo; }
    public String getDescripcion() { return descripcion; }
    public Double getPorcentajeSugerido() { return porcentajeSugerido; }

    public static ConceptoHoraExtra porCodigo(int codigo) {
        for (ConceptoHoraExtra c : values())
            if (c.codigo == codigo) return c;
        throw new IllegalArgumentException("Tipo de hora extra no válido: " + codigo);
    }
}
