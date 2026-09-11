package com.pos_backend.nomina.domain.usecase;

import com.pos_backend.nomina.domain.model.Empleado;
import com.pos_backend.nomina.domain.model.ParametrosNomina;
import com.pos_backend.nomina.domain.model.gateway.EmpleadoGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EmpleadoUseCaseTest {

    private EmpleadoGateway gateway;
    private EmpleadoUseCase useCase;

    private static final String EMPRESA = "empresa-1";

    @BeforeEach
    void setUp() {
        gateway = mock(EmpleadoGateway.class);
        useCase = new EmpleadoUseCase(gateway);
        when(gateway.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private Empleado empleadoValido() {
        Empleado e = new Empleado();
        e.setNombres("Ana");
        e.setApellidos("Gómez");
        e.setNumeroDocumento("123456");
        e.setSalarioBase(ParametrosNomina.SMMLV);
        e.setFechaIngreso(LocalDate.now());
        return e;
    }

    @Test
    void crear_valido_quedaActivoPorDefecto() {
        Empleado resultado = useCase.crear(empleadoValido(), EMPRESA);
        assertTrue(resultado.getActivo());
        assertEquals(EMPRESA, resultado.getEmpresaId());
    }

    @Test
    void crear_sinNombre_lanzaExcepcion() {
        Empleado e = empleadoValido();
        e.setNombres(null);
        assertThrows(RuntimeException.class, () -> useCase.crear(e, EMPRESA));
    }

    @Test
    void crear_sinDocumento_lanzaExcepcion() {
        Empleado e = empleadoValido();
        e.setNumeroDocumento("");
        assertThrows(RuntimeException.class, () -> useCase.crear(e, EMPRESA));
    }

    @Test
    void crear_salarioCero_lanzaExcepcion() {
        Empleado e = empleadoValido();
        e.setSalarioBase(0.0);
        assertThrows(RuntimeException.class, () -> useCase.crear(e, EMPRESA));
    }

    @Test
    void crear_sinFechaIngreso_lanzaExcepcion() {
        Empleado e = empleadoValido();
        e.setFechaIngreso(null);
        assertThrows(RuntimeException.class, () -> useCase.crear(e, EMPRESA));
    }

    @Test
    void crear_salarioPorDebajoDelMinimo_lanzaExcepcion() {
        Empleado e = empleadoValido();
        e.setSalarioBase(ParametrosNomina.SMMLV - 1);
        assertThrows(RuntimeException.class, () -> useCase.crear(e, EMPRESA));
    }

    @Test
    void crear_salarioIntegralPorDebajoDe13Smmlv_lanzaExcepcion() {
        Empleado e = empleadoValido();
        e.setSalarioIntegral(true);
        e.setSalarioBase(ParametrosNomina.MINIMO_SALARIO_INTEGRAL - 1);
        assertThrows(RuntimeException.class, () -> useCase.crear(e, EMPRESA));
    }

    @Test
    void crear_salarioIntegralValido_noLanza() {
        Empleado e = empleadoValido();
        e.setSalarioIntegral(true);
        e.setSalarioBase(ParametrosNomina.MINIMO_SALARIO_INTEGRAL);
        assertDoesNotThrow(() -> useCase.crear(e, EMPRESA));
    }

    @Test
    void crear_documentoDuplicado_lanzaExcepcion() {
        when(gateway.buscarPorDocumento("123456", EMPRESA)).thenReturn(new Empleado());
        assertThrows(RuntimeException.class, () -> useCase.crear(empleadoValido(), EMPRESA));
    }

    @Test
    void actualizar_noRevalidaDocumentoDuplicadoContraSiMismo() {
        // esNuevo=false en actualizar(): no debe llamar a buscarPorDocumento.
        Empleado existente = empleadoValido();
        existente.setEmpleadoId(1L);
        when(gateway.buscarPorId(1L, EMPRESA)).thenReturn(existente);

        Empleado cambios = empleadoValido();
        useCase.actualizar(1L, cambios, EMPRESA);

        verify(gateway, never()).buscarPorDocumento(any(), any());
    }

    @Test
    void actualizar_inexistente_lanzaNoSuchElement() {
        when(gateway.buscarPorId(99L, EMPRESA)).thenReturn(null);
        assertThrows(NoSuchElementException.class, () -> useCase.actualizar(99L, empleadoValido(), EMPRESA));
    }

    @Test
    void eliminar_inexistente_lanzaNoSuchElementYNoLlamaAlGateway() {
        when(gateway.buscarPorId(99L, EMPRESA)).thenReturn(null);
        assertThrows(NoSuchElementException.class, () -> useCase.eliminar(99L, EMPRESA));
        verify(gateway, never()).eliminar(any(), any());
    }
}
