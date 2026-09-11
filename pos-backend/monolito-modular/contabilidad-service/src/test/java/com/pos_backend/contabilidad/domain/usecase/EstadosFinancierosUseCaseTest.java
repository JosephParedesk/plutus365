package com.pos_backend.contabilidad.domain.usecase;

import com.pos_backend.contabilidad.domain.model.*;
import com.pos_backend.contabilidad.domain.model.gateway.AsientoContableGateway;
import com.pos_backend.contabilidad.domain.model.gateway.CuentaContableGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests de caracterización de escenario (no exhaustivos: el módulo es grande
 * y esto es lo mínimo razonable antes de migrar). Cubren la aritmética base
 * de cada estado financiero contra un set de cuentas/asientos de ejemplo.
 */
class EstadosFinancierosUseCaseTest {

    private AsientoContableGateway asientoGateway;
    private CuentaContableGateway cuentaGateway;
    private EstadosFinancierosUseCase useCase;

    private static final String EMPRESA = "empresa-1";

    @BeforeEach
    void setUp() {
        asientoGateway = mock(AsientoContableGateway.class);
        cuentaGateway = mock(CuentaContableGateway.class);
        useCase = new EstadosFinancierosUseCase(asientoGateway, cuentaGateway);
    }

    private CuentaContable cuenta(String codigo, String naturaleza) {
        CuentaContable c = new CuentaContable();
        c.setCodigo(codigo);
        c.setNombre("Cuenta " + codigo);
        c.setNaturaleza(naturaleza);
        return c;
    }

    private AsientoContable asiento(LocalDate fecha, MovimientoContable... movs) {
        AsientoContable a = new AsientoContable();
        a.setFecha(fecha);
        a.setEstado("CONTABILIZADO");
        a.setMovimientos(List.of(movs));
        return a;
    }

    @Test
    void balanceGeneral_activoIgualAPatrimonio_cuadra() {
        when(cuentaGateway.listar(EMPRESA)).thenReturn(List.of(
                cuenta("11050501", "DEBITO"),   // Caja - activo
                cuenta("31500501", "CREDITO")   // Capital - patrimonio
        ));
        when(asientoGateway.listar(EMPRESA)).thenReturn(List.of(
                asiento(LocalDate.of(2026, 1, 1),
                        new MovimientoContable("11050501", null, 100000.0, 0.0, "aporte"),
                        new MovimientoContable("31500501", null, 0.0, 100000.0, "aporte"))
        ));

        BalanceGeneral balance = useCase.balanceGeneral(EMPRESA, LocalDate.of(2026, 1, 31));

        assertEquals(100000.0, balance.getTotalActivo());
        assertEquals(100000.0, balance.getTotalPatrimonio());
        assertEquals(0.0, balance.getTotalPasivo());
        assertTrue(balance.getCuadra());
    }

    @Test
    void balanceGeneral_ignoraAsientosDespuesDeLaFechaDeCorte() {
        when(cuentaGateway.listar(EMPRESA)).thenReturn(List.of(
                cuenta("11050501", "DEBITO"),
                cuenta("31500501", "CREDITO")
        ));
        when(asientoGateway.listar(EMPRESA)).thenReturn(List.of(
                asiento(LocalDate.of(2026, 1, 1),
                        new MovimientoContable("11050501", null, 100000.0, 0.0, "aporte"),
                        new MovimientoContable("31500501", null, 0.0, 100000.0, "aporte")),
                asiento(LocalDate.of(2026, 3, 1), // después del corte, no debe contar
                        new MovimientoContable("11050501", null, 999999.0, 0.0, "tarde"),
                        new MovimientoContable("31500501", null, 0.0, 999999.0, "tarde"))
        ));

        BalanceGeneral balance = useCase.balanceGeneral(EMPRESA, LocalDate.of(2026, 1, 31));

        assertEquals(100000.0, balance.getTotalActivo());
    }

    @Test
    void estadoResultados_ingresosMenosCostosMenosGastos_esLaUtilidad() {
        when(cuentaGateway.listar(EMPRESA)).thenReturn(List.of(
                cuenta("41350501", "CREDITO"), // ingreso por ventas
                cuenta("61350501", "DEBITO"),  // costo de ventas
                cuenta("51959501", "DEBITO")   // gasto diverso
        ));
        when(asientoGateway.listar(EMPRESA)).thenReturn(List.of(
                asiento(LocalDate.of(2026, 1, 15),
                        new MovimientoContable("41350501", null, 0.0, 1000.0, "venta")),
                asiento(LocalDate.of(2026, 1, 16),
                        new MovimientoContable("61350501", null, 200.0, 0.0, "costo")),
                asiento(LocalDate.of(2026, 1, 17),
                        new MovimientoContable("51959501", null, 300.0, 0.0, "gasto"))
        ));

        EstadoResultados er = useCase.estadoResultados(EMPRESA, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        assertEquals(1000.0, er.getTotalIngresos());
        assertEquals(200.0, er.getTotalCostos());
        assertEquals(300.0, er.getTotalGastos());
        assertEquals(500.0, er.getUtilidad());
    }

    @Test
    void estadoResultados_soloContabilizaAsientosDentroDelRangoDeFechas() {
        when(cuentaGateway.listar(EMPRESA)).thenReturn(List.of(cuenta("41350501", "CREDITO")));
        when(asientoGateway.listar(EMPRESA)).thenReturn(List.of(
                asiento(LocalDate.of(2025, 12, 31), // antes del rango
                        new MovimientoContable("41350501", null, 0.0, 5000.0, "venta año pasado")),
                asiento(LocalDate.of(2026, 1, 15),
                        new MovimientoContable("41350501", null, 0.0, 1000.0, "venta de enero"))
        ));

        EstadoResultados er = useCase.estadoResultados(EMPRESA, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        assertEquals(1000.0, er.getTotalIngresos());
    }

    @Test
    void flujoEfectivo_utilidadDelPeriodoEncabezaElFlujoDeOperacion() {
        when(cuentaGateway.listar(EMPRESA)).thenReturn(List.of(
                cuenta("11050501", "DEBITO"),
                cuenta("41350501", "CREDITO")
        ));
        when(asientoGateway.listar(EMPRESA)).thenReturn(List.of(
                asiento(LocalDate.of(2026, 1, 15),
                        new MovimientoContable("11050501", null, 1000.0, 0.0, "venta contado"),
                        new MovimientoContable("41350501", null, 0.0, 1000.0, "venta contado"))
        ));

        FlujoEfectivo flujo = useCase.flujoEfectivo(EMPRESA, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        assertEquals(1000.0, flujo.getUtilidadPeriodo());
        assertEquals("Utilidad del período", flujo.getOperacion().get(0).getConcepto());
        assertTrue(flujo.getCuadra());
    }

    @Test
    void cambiosPatrimonio_capturaLaVariacionEntrePeriodos() {
        when(cuentaGateway.listar(EMPRESA)).thenReturn(List.of(cuenta("31500501", "CREDITO")));
        when(asientoGateway.listar(EMPRESA)).thenReturn(List.of(
                asiento(LocalDate.of(2025, 12, 15), // antes del período: cuenta como saldo inicial
                        new MovimientoContable("31500501", null, 0.0, 100000.0, "aporte inicial")),
                asiento(LocalDate.of(2026, 1, 20), // dentro del período: parte de la variación
                        new MovimientoContable("31500501", null, 0.0, 50000.0, "aporte adicional"))
        ));

        CambiosPatrimonio cambios = useCase.cambiosPatrimonio(EMPRESA, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        CambiosPatrimonio.LineaPatrimonio linea = cambios.getLineas().stream()
                .filter(l -> "31500501".equals(l.getCodigo())).findFirst().orElseThrow();
        assertEquals(100000.0, linea.getSaldoInicial());
        assertEquals(50000.0, linea.getVariacion());
        assertEquals(150000.0, linea.getSaldoFinal());
    }
}
