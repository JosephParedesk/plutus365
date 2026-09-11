package com.pos_backend.contabilidad.domain.usecase;

import com.pos_backend.contabilidad.domain.model.CentroCosto;
import com.pos_backend.contabilidad.domain.model.gateway.CentroCostoGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class CentroCostoUseCaseTest {

    private CentroCostoGateway gateway;
    private CentroCostoUseCase useCase;

    private static final String EMPRESA = "empresa-1";

    @BeforeEach
    void setUp() {
        gateway = mock(CentroCostoGateway.class);
        useCase = new CentroCostoUseCase(gateway);
        when(gateway.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void crear_sinCodigo_lanzaExcepcion() {
        CentroCosto c = new CentroCosto(null, null, "", "Ventas Norte", null, null, null);
        assertThrows(RuntimeException.class, () -> useCase.crear(c, EMPRESA));
    }

    @Test
    void crear_sinNombre_lanzaExcepcion() {
        CentroCosto c = new CentroCosto(null, null, "CC-01", "", null, null, null);
        assertThrows(RuntimeException.class, () -> useCase.crear(c, EMPRESA));
    }

    @Test
    void crear_codigoDuplicado_lanzaExcepcion() {
        when(gateway.existeCodigo("CC-01", EMPRESA)).thenReturn(true);
        CentroCosto c = new CentroCosto(null, null, "CC-01", "Ventas Norte", null, null, null);
        assertThrows(RuntimeException.class, () -> useCase.crear(c, EMPRESA));
    }

    @Test
    void crear_valido_quedaActivoPorDefecto() {
        when(gateway.existeCodigo("CC-01", EMPRESA)).thenReturn(false);
        CentroCosto c = new CentroCosto(null, null, "CC-01", "Ventas Norte", null, null, null);

        CentroCosto resultado = useCase.crear(c, EMPRESA);

        assertEquals(EMPRESA, resultado.getEmpresaId());
        assertTrue(resultado.getActivo());
    }

    @Test
    void actualizar_soloCambiaCamposNoNulos_yNuncaElCodigo() {
        CentroCosto existente = new CentroCosto(1L, EMPRESA, "CC-01", "Ventas Norte", "desc vieja", "Ana", true);
        when(gateway.buscarPorId(1L, EMPRESA)).thenReturn(existente);
        CentroCosto cambios = new CentroCosto(null, null, "CC-99", null, null, "Beto", null);

        CentroCosto resultado = useCase.actualizar(1L, cambios, EMPRESA);

        assertEquals("CC-01", resultado.getCodigo()); // no cambia
        assertEquals("Ventas Norte", resultado.getNombre()); // no vino cambio, no cambia
        assertEquals("Beto", resultado.getResponsable()); // sí vino cambio
        assertEquals("desc vieja", resultado.getDescripcion());
    }

    @Test
    void actualizar_centroInexistente_lanzaNoSuchElement() {
        when(gateway.buscarPorId(99L, EMPRESA)).thenReturn(null);
        CentroCosto cambios = new CentroCosto();
        assertThrows(NoSuchElementException.class, () -> useCase.actualizar(99L, cambios, EMPRESA));
    }

    @Test
    void eliminar_centroInexistente_lanzaNoSuchElementYNoLlamaAEliminar() {
        when(gateway.buscarPorId(99L, EMPRESA)).thenReturn(null);
        assertThrows(NoSuchElementException.class, () -> useCase.eliminar(99L, EMPRESA));
        verify(gateway, never()).eliminar(anyLong(), any());
    }

    @Test
    void eliminar_centroExistente_delegaAlGateway() {
        when(gateway.buscarPorId(1L, EMPRESA)).thenReturn(new CentroCosto(1L, EMPRESA, "CC-01", "x", null, null, true));
        useCase.eliminar(1L, EMPRESA);
        verify(gateway).eliminar(1L, EMPRESA);
    }
}
