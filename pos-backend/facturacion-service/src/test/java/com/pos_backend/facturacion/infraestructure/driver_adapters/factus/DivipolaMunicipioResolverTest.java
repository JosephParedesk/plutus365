package com.pos_backend.facturacion.infraestructure.driver_adapters.factus;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Check mínimo del catálogo DIVIPOLA embebido (1122 municipios, tomado de
 * developers.factus.com.co/tablas-de-referencia/municipios/) — lo que más fácil se
 * rompe es la normalización de tildes/mayúsculas y el caso ambiguo (mismo nombre de
 * ciudad en más de un departamento).
 */
class DivipolaMunicipioResolverTest {

    private final DivipolaMunicipioResolver resolver = new DivipolaMunicipioResolver();

    @Test
    void resuelveCiudadesGrandesSinImportarTildesNiMayusculas() {
        assertEquals("05001", resolver.resolver("Antioquia", "medellin"));
        assertEquals("05001", resolver.resolver("ANTIOQUIA", "MEDELLÍN"));
        assertEquals("76001", resolver.resolver("Valle del Cauca", "Cali"));
        assertEquals("08001", resolver.resolver(null, "Barranquilla"));
    }

    @Test
    void resuelveBogotaAunqueFalteElSufijoDC() {
        assertEquals("11001", resolver.resolver("Bogotá", "Bogotá"));
        assertEquals("11001", resolver.resolver(null, "Bogota"));
    }

    @Test
    void devuelveNullSiLaCiudadEsAmbiguaEntreDepartamentos() {
        // "La Union" existe en varios departamentos — sin departamento no debe adivinar.
        assertNull(resolver.resolver(null, "La Union"));
        // con el departamento correcto sí debe resolver.
        assertNotNull(resolver.resolver("Nariño", "La Union"));
    }

    @Test
    void devuelveNullSiNoHayCiudad() {
        assertNull(resolver.resolver("Antioquia", null));
        assertNull(resolver.resolver("Antioquia", ""));
    }

    @Test
    void devuelveNullParaUnaCiudadInexistente() {
        assertNull(resolver.resolver("Antioquia", "Ciudad Que No Existe"));
    }
}
