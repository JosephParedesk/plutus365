package com.pos_backend.nomina.domain.usecase;

import com.pos_backend.nomina.domain.model.AcumuladoInicial;
import com.pos_backend.nomina.domain.model.Empleado;
import com.pos_backend.nomina.domain.model.gateway.AcumuladoInicialGateway;
import com.pos_backend.nomina.domain.model.gateway.EmpleadoGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AcumuladoInicialUseCaseTest {

    private AcumuladoInicialGateway acumuladoGateway;
    private EmpleadoGateway empleadoGateway;
    private AcumuladoInicialUseCase useCase;

    private static final String EMPRESA = "empresa-1";

    @BeforeEach
    void setUp() {
        acumuladoGateway = mock(AcumuladoInicialGateway.class);
        empleadoGateway = mock(EmpleadoGateway.class);
        useCase = new AcumuladoInicialUseCase(acumuladoGateway, empleadoGateway);
        when(acumuladoGateway.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private Empleado empleado() {
        Empleado e = new Empleado();
        e.setEmpleadoId(1L);
        e.setNombres("Ana");
        e.setApellidos("Gómez");
        return e;
    }

    @Test
    void guardar_sinEmpleadoId_lanzaExcepcion() {
        AcumuladoInicial a = new AcumuladoInicial();
        a.setAnio(2026);
        assertThrows(RuntimeException.class, () -> useCase.guardar(a, EMPRESA));
    }

    @Test
    void guardar_sinAnio_lanzaExcepcion() {
        AcumuladoInicial a = new AcumuladoInicial();
        a.setEmpleadoId(1L);
        assertThrows(RuntimeException.class, () -> useCase.guardar(a, EMPRESA));
    }

    @Test
    void guardar_empleadoInexistente_lanzaNoSuchElement() {
        when(empleadoGateway.buscarPorId(1L, EMPRESA)).thenReturn(null);
        AcumuladoInicial a = new AcumuladoInicial();
        a.setEmpleadoId(1L);
        a.setAnio(2026);
        assertThrows(NoSuchElementException.class, () -> useCase.guardar(a, EMPRESA));
    }

    @Test
    void guardar_completaElNombreDelEmpleadoAutomaticamente() {
        when(empleadoGateway.buscarPorId(1L, EMPRESA)).thenReturn(empleado());
        AcumuladoInicial a = new AcumuladoInicial();
        a.setEmpleadoId(1L);
        a.setAnio(2026);

        AcumuladoInicial resultado = useCase.guardar(a, EMPRESA);

        assertEquals("Ana Gómez", resultado.getNombreEmpleado());
        assertEquals(EMPRESA, resultado.getEmpresaId());
    }

    @Test
    void guardar_yaExisteParaEseEmpleadoYAnio_actualizaEnVezDeDuplicar() {
        when(empleadoGateway.buscarPorId(1L, EMPRESA)).thenReturn(empleado());
        AcumuladoInicial existente = new AcumuladoInicial();
        existente.setAcumuladoId(99L);
        when(acumuladoGateway.buscarPorEmpleadoYAnio(1L, 2026, EMPRESA)).thenReturn(existente);

        AcumuladoInicial nuevo = new AcumuladoInicial();
        nuevo.setEmpleadoId(1L);
        nuevo.setAnio(2026);

        AcumuladoInicial resultado = useCase.guardar(nuevo, EMPRESA);

        assertEquals(99L, resultado.getAcumuladoId());
    }
}
