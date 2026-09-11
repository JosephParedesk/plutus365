package com.pos_backend.contabilidad.domain.usecase;

import com.pos_backend.contabilidad.domain.model.CuentaContable;
import com.pos_backend.contabilidad.domain.model.gateway.CuentaContableGateway;
import com.pos_backend.contabilidad.domain.model.gateway.PucBaseGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CuentaContableUseCaseTest {

    private CuentaContableGateway cuentaGateway;
    private PucBaseGateway pucBaseGateway;
    private CuentaContableUseCase useCase;

    private static final String EMPRESA = "empresa-1";

    @BeforeEach
    void setUp() {
        cuentaGateway = mock(CuentaContableGateway.class);
        pucBaseGateway = mock(PucBaseGateway.class);
        useCase = new CuentaContableUseCase(cuentaGateway, pucBaseGateway);
        when(cuentaGateway.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private CuentaContable cuenta(String codigo, String naturaleza) {
        CuentaContable c = new CuentaContable();
        c.setCodigo(codigo);
        c.setNaturaleza(naturaleza);
        return c;
    }

    // ─── Siembra del PUC base ────────────────────────────────────────────

    @Test
    void listar_empresaSinCuentas_siembraElPucBaseAntesDeListar() {
        when(cuentaGateway.existeAlgunaCuenta(EMPRESA)).thenReturn(false);
        when(pucBaseGateway.cargarBase()).thenReturn(List.of(cuenta("1", "DEBITO")));

        useCase.listar(EMPRESA);

        verify(cuentaGateway).guardarTodas(anyList());
    }

    @Test
    void listar_empresaConCuentas_noVuelveASembrar() {
        when(cuentaGateway.existeAlgunaCuenta(EMPRESA)).thenReturn(true);

        useCase.listar(EMPRESA);

        verify(cuentaGateway, never()).guardarTodas(anyList());
        verifyNoInteractions(pucBaseGateway);
    }

    // ─── Inferencia de nivel por longitud de código ─────────────────────

    @Test
    void crear_codigoDeUnDigito_esClaseYNoEsTransaccional() {
        CuentaContable nueva = cuenta("9", null);
        nueva.setNombre("Cuentas de orden");
        when(cuentaGateway.existeCodigo("9", EMPRESA)).thenReturn(false);

        CuentaContable resultado = useCase.crear(nueva, EMPRESA);

        assertEquals("CLASE", resultado.getNivel());
        assertFalse(resultado.getEsTransaccional());
        assertNull(resultado.getCodigoPadre());
    }

    @Test
    void crear_codigoDeSieteDigitos_esAuxiliarYEsTransaccional() {
        CuentaContable padre = cuenta("110505", "DEBITO");
        when(cuentaGateway.buscarPorCodigo("110505", EMPRESA)).thenReturn(padre);
        when(cuentaGateway.existeCodigo("1105059", EMPRESA)).thenReturn(false);

        CuentaContable nueva = cuenta("1105059", null);
        nueva.setNombre("Caja menor sucursal norte");
        CuentaContable resultado = useCase.crear(nueva, EMPRESA);

        assertEquals("AUXILIAR", resultado.getNivel());
        assertTrue(resultado.getEsTransaccional());
        assertEquals("110505", resultado.getCodigoPadre());
        assertEquals("DEBITO", resultado.getNaturaleza()); // heredada del padre
    }

    @Test
    void crear_sinCuentaPadre_lanzaNoSuchElement() {
        when(cuentaGateway.buscarPorCodigo("110505", EMPRESA)).thenReturn(null);
        when(cuentaGateway.existeCodigo("1105059", EMPRESA)).thenReturn(false);

        CuentaContable nueva = cuenta("1105059", null);
        nueva.setNombre("x");

        assertThrows(NoSuchElementException.class, () -> useCase.crear(nueva, EMPRESA));
    }

    @Test
    void crear_codigoDuplicado_lanzaExcepcion() {
        when(cuentaGateway.existeCodigo("9", EMPRESA)).thenReturn(true);
        CuentaContable nueva = cuenta("9", null);
        nueva.setNombre("x");

        assertThrows(RuntimeException.class, () -> useCase.crear(nueva, EMPRESA));
    }

    @Test
    void crear_codigoConLetras_lanzaExcepcion() {
        CuentaContable nueva = cuenta("11A", null);
        nueva.setNombre("x");

        assertThrows(RuntimeException.class, () -> useCase.crear(nueva, EMPRESA));
    }

    @Test
    void crear_codigoDeMasDeDoceDigitos_lanzaExcepcion() {
        CuentaContable nueva = cuenta("1234567890123", null);
        nueva.setNombre("x");

        assertThrows(RuntimeException.class, () -> useCase.crear(nueva, EMPRESA));
    }

    // ─── Eliminar ────────────────────────────────────────────────────────

    @Test
    void eliminar_cuentaDelPucBase_lanzaExcepcion() {
        CuentaContable base = cuenta("110505", "DEBITO");
        base.setPersonalizada(false);
        when(cuentaGateway.buscarPorCodigo("110505", EMPRESA)).thenReturn(base);

        assertThrows(RuntimeException.class, () -> useCase.eliminar("110505", EMPRESA));
    }

    @Test
    void eliminar_cuentaConSubcuentas_lanzaExcepcion() {
        CuentaContable personalizada = cuenta("1105059", "DEBITO");
        personalizada.setPersonalizada(true);
        when(cuentaGateway.buscarPorCodigo("1105059", EMPRESA)).thenReturn(personalizada);
        when(cuentaGateway.listarHijas("1105059", EMPRESA)).thenReturn(List.of(cuenta("11050591", "DEBITO")));

        assertThrows(RuntimeException.class, () -> useCase.eliminar("1105059", EMPRESA));
    }

    @Test
    void eliminar_cuentaPersonalizadaSinHijas_eliminaSinProblema() {
        CuentaContable personalizada = cuenta("1105059", "DEBITO");
        personalizada.setPersonalizada(true);
        when(cuentaGateway.buscarPorCodigo("1105059", EMPRESA)).thenReturn(personalizada);
        when(cuentaGateway.listarHijas("1105059", EMPRESA)).thenReturn(List.of());

        assertDoesNotThrow(() -> useCase.eliminar("1105059", EMPRESA));
        verify(cuentaGateway).eliminar("1105059", EMPRESA);
    }
}
