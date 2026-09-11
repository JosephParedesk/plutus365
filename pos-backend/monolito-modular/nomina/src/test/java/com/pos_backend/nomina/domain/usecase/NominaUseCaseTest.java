package com.pos_backend.nomina.domain.usecase;

import com.pos_backend.nomina.domain.model.Empleado;
import com.pos_backend.nomina.domain.model.Nomina;
import com.pos_backend.nomina.domain.model.NominaDetalle;
import com.pos_backend.nomina.domain.model.gateway.EmpleadoGateway;
import com.pos_backend.nomina.domain.model.gateway.NominaGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests de caracterización escritos ANTES de migrar este módulo al monolito
 * modular (0 tests de UseCase hoy en el standalone — la lógica de liquidación
 * es la más sensible del proyecto junto con contabilidad). Los valores
 * esperados se calcularon replicando la fórmula exacta del código en un
 * script aparte (no a mano), para no meter un error de aritmética propio
 * como "expectativa". Documentan el comportamiento ACTUAL, no un ideal.
 */
class NominaUseCaseTest {

    private NominaGateway nominaGateway;
    private EmpleadoGateway empleadoGateway;
    private NominaUseCase useCase;

    private static final String EMPRESA = "empresa-1";

    @BeforeEach
    void setUp() {
        nominaGateway = mock(NominaGateway.class);
        empleadoGateway = mock(EmpleadoGateway.class);
        useCase = new NominaUseCase(nominaGateway, empleadoGateway);
        when(nominaGateway.generarSiguienteNumero(EMPRESA)).thenReturn("NOM-00001");
        when(nominaGateway.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private Empleado empleado(Long id, double salario, boolean integral, LocalDate ingreso, LocalDate retiro, String nivelRiesgo) {
        Empleado e = new Empleado();
        e.setEmpleadoId(id);
        e.setEmpresaId(EMPRESA);
        e.setNombres("Juan");
        e.setApellidos("Pérez");
        e.setNumeroDocumento("100" + id);
        e.setCargo("Vendedor");
        e.setSalarioBase(salario);
        e.setSalarioIntegral(integral);
        e.setAuxilioTransporte(true);
        e.setNivelRiesgoArl(nivelRiesgo);
        e.setFechaIngreso(ingreso);
        e.setFechaRetiro(retiro);
        e.setActivo(true);
        return e;
    }

    private NominaDetalle detalleDe(Nomina nomina, Long empleadoId) {
        return nomina.getDetalles().stream().filter(d -> d.getEmpleadoId().equals(empleadoId)).findFirst().orElseThrow();
    }

    @Test
    void liquidar_empleadoMesCompleto_bajoElTopeDeExoneracionYAuxilio() {
        // Salario 3,000,000: tiene derecho a auxilio de transporte (<=2 SMMLV=3,501,810),
        // está exonerado de aportes (salud/sena/icbf) por estar bajo 10 SMMLV=17,509,050.
        Empleado e = empleado(1L, 3_000_000.0, false, LocalDate.of(2025, 1, 1), null, null);
        when(empleadoGateway.listarActivos(EMPRESA)).thenReturn(List.of(e));
        when(nominaGateway.existePeriodo(2026, 1, EMPRESA)).thenReturn(false);

        Nomina nomina = useCase.liquidar(2026, 1, "MENSUAL", Map.of(), EMPRESA, "tester");
        NominaDetalle d = detalleDe(nomina, 1L);

        assertEquals(30, d.getDiasTrabajados());
        assertEquals(3_000_000.0, d.getSueldo());
        assertEquals(249_095.0, d.getAuxilioTransporte());
        assertEquals(3_249_095.0, d.getTotalDevengado());
        assertEquals(3_000_000.0, d.getIbc());
        assertEquals(120_000.0, d.getSaludEmpleado());
        assertEquals(120_000.0, d.getPensionEmpleado());
        assertEquals(0.0, d.getFondoSolidaridad()); // bajo 4 SMMLV
        assertEquals(240_000.0, d.getTotalDeducciones());
        assertEquals(3_009_095.0, d.getNetoPagar());
        assertTrue(d.getExonerado());
        assertEquals(0.0, d.getSaludEmpleador());
        assertEquals(360_000.0, d.getPensionEmpleador());
        assertEquals(15_660.0, d.getArl()); // sin nivel de riesgo -> tarifa mínima I
        assertEquals(0.0, d.getSena());
        assertEquals(0.0, d.getIcbf());
        assertEquals(120_000.0, d.getCajaCompensacion()); // caja NUNCA se exonera
        assertEquals(495_660.0, d.getTotalAportesEmpleador());
        assertEquals(270_650.0, d.getProvCesantias());
        assertEquals(32_478.0, d.getProvInteresesCesantias());
        assertEquals(270_650.0, d.getProvPrima());
        assertEquals(125_100.0, d.getProvVacaciones());
        assertEquals(698_878.0, d.getTotalProvisiones());
        assertEquals(4_443_633.0, d.getCostoTotal());

        assertEquals("LIQUIDADA", nomina.getEstado());
        assertEquals("NOM-00001", nomina.getNumero());
    }

    @Test
    void liquidar_salarioIntegral_sinAuxilioNiProvisionesYSoloCotiza70Porciento() {
        // 25,000,000 >= 13 SMMLV (mínimo legal para integral).
        Empleado e = empleado(2L, 25_000_000.0, true, LocalDate.of(2025, 1, 1), null, null);
        when(empleadoGateway.listarActivos(EMPRESA)).thenReturn(List.of(e));
        when(nominaGateway.existePeriodo(2026, 1, EMPRESA)).thenReturn(false);

        Nomina nomina = useCase.liquidar(2026, 1, "MENSUAL", Map.of(), EMPRESA, "tester");
        NominaDetalle d = detalleDe(nomina, 2L);

        assertEquals(0.0, d.getAuxilioTransporte()); // el integral no tiene derecho
        assertEquals(17_500_000.0, d.getIbc()); // 70% de 25,000,000
        assertEquals(175_000.0, d.getFondoSolidaridad()); // >=4 SMMLV, <16 SMMLV -> 1%
        assertFalse(d.getExonerado()); // >=10 SMMLV
        assertEquals(1_487_500.0, d.getSaludEmpleador());
        assertEquals(0.0, d.getProvCesantias());
        assertEquals(0.0, d.getProvPrima());
        assertEquals(0.0, d.getProvVacaciones());
        assertEquals(0.0, d.getTotalProvisiones());
        assertEquals(30_253_850.0, d.getCostoTotal());
    }

    @Test
    void liquidar_salarioAltoNoExonerado_conNivelRiesgoArlIII() {
        Empleado e = empleado(3L, 20_000_000.0, false, LocalDate.of(2025, 1, 1), null, "III");
        when(empleadoGateway.listarActivos(EMPRESA)).thenReturn(List.of(e));
        when(nominaGateway.existePeriodo(2026, 1, EMPRESA)).thenReturn(false);

        Nomina nomina = useCase.liquidar(2026, 1, "MENSUAL", Map.of(), EMPRESA, "tester");
        NominaDetalle d = detalleDe(nomina, 3L);

        assertEquals(0.0, d.getAuxilioTransporte()); // > 2 SMMLV, no tiene derecho
        assertFalse(d.getExonerado());
        assertEquals(1_700_000.0, d.getSaludEmpleador());
        assertEquals(400_000.0, d.getSena());
        assertEquals(600_000.0, d.getIcbf());
        assertEquals(487_200.0, d.getArl()); // nivel III = 2.436%
        assertEquals(4_365_920.0, d.getTotalProvisiones());
        assertEquals(30_753_120.0, d.getCostoTotal());
    }

    @Test
    void liquidar_ingresoAMitadDePeriodo_prorrateaLosDias() {
        // Ingresó el 16 del mes -> del 16 al 31 son 16 días naturales, pero el
        // cálculo de "hasta" es fin de mes (31) y ChronoUnit.DAYS+1 da 16.
        Empleado e = empleado(4L, 3_000_000.0, false, LocalDate.of(2026, 1, 16), null, null);
        when(empleadoGateway.listarActivos(EMPRESA)).thenReturn(List.of(e));
        when(nominaGateway.existePeriodo(2026, 1, EMPRESA)).thenReturn(false);

        Nomina nomina = useCase.liquidar(2026, 1, "MENSUAL", Map.of(), EMPRESA, "tester");
        NominaDetalle d = detalleDe(nomina, 4L);

        assertEquals(16, d.getDiasTrabajados());
        assertEquals(1_600_000.0, d.getSueldo()); // 3,000,000/30*16
    }

    @Test
    void liquidar_diasTrabajadosExplicitosEnLaNovedad_seRespetanSobreElProrrateo() {
        Empleado e = empleado(5L, 3_000_000.0, false, LocalDate.of(2025, 1, 1), null, null);
        when(empleadoGateway.listarActivos(EMPRESA)).thenReturn(List.of(e));
        when(nominaGateway.existePeriodo(2026, 1, EMPRESA)).thenReturn(false);
        NominaDetalle novedad = new NominaDetalle();
        novedad.setDiasTrabajados(15);

        Nomina nomina = useCase.liquidar(2026, 1, "MENSUAL", Map.of(5L, novedad), EMPRESA, "tester");
        NominaDetalle d = detalleDe(nomina, 5L);

        assertEquals(15, d.getDiasTrabajados());
        assertEquals(1_500_000.0, d.getSueldo());
        assertEquals(124_547.0, d.getAuxilioTransporte());
    }

    @Test
    void liquidar_periodoQueYaExiste_lanzaExcepcion() {
        when(nominaGateway.existePeriodo(2026, 1, EMPRESA)).thenReturn(true);
        assertThrows(RuntimeException.class, () -> useCase.liquidar(2026, 1, "MENSUAL", Map.of(), EMPRESA, "tester"));
    }

    @Test
    void liquidar_sinEmpleadosActivos_lanzaExcepcion() {
        when(nominaGateway.existePeriodo(2026, 1, EMPRESA)).thenReturn(false);
        when(empleadoGateway.listarActivos(EMPRESA)).thenReturn(List.of());
        assertThrows(RuntimeException.class, () -> useCase.liquidar(2026, 1, "MENSUAL", Map.of(), EMPRESA, "tester"));
    }

    @Test
    void liquidar_periodicidadNula_defaultAMensual() {
        Empleado e = empleado(6L, 3_000_000.0, false, LocalDate.of(2025, 1, 1), null, null);
        when(empleadoGateway.listarActivos(EMPRESA)).thenReturn(List.of(e));
        when(nominaGateway.existePeriodo(2026, 1, EMPRESA)).thenReturn(false);

        Nomina nomina = useCase.liquidar(2026, 1, null, Map.of(), EMPRESA, "tester");

        assertEquals("MENSUAL", nomina.getPeriodicidad());
    }

    @Test
    void anular_nominaYaPagada_lanzaExcepcion() {
        Nomina n = new Nomina();
        n.setNominaId(1L);
        n.setEstado("PAGADA");
        when(nominaGateway.buscarPorId(1L, EMPRESA)).thenReturn(n);

        assertThrows(RuntimeException.class, () -> useCase.anular(1L, EMPRESA));
    }

    @Test
    void anular_nominaLiquidada_laMarcaAnulada() {
        Nomina n = new Nomina();
        n.setNominaId(1L);
        n.setEstado("LIQUIDADA");
        when(nominaGateway.buscarPorId(1L, EMPRESA)).thenReturn(n);

        useCase.anular(1L, EMPRESA);

        assertEquals("ANULADA", n.getEstado());
        verify(nominaGateway).guardar(n);
    }

    @Test
    void marcarPagada_nominaNoLiquidada_lanzaExcepcion() {
        Nomina n = new Nomina();
        n.setNominaId(1L);
        n.setEstado("BORRADOR");
        when(nominaGateway.buscarPorId(1L, EMPRESA)).thenReturn(n);

        assertThrows(RuntimeException.class, () -> useCase.marcarPagada(1L, EMPRESA));
    }

    @Test
    void marcarPagada_nominaLiquidada_laMarcaPagada() {
        Nomina n = new Nomina();
        n.setNominaId(1L);
        n.setEstado("LIQUIDADA");
        when(nominaGateway.buscarPorId(1L, EMPRESA)).thenReturn(n);

        Nomina resultado = useCase.marcarPagada(1L, EMPRESA);

        assertEquals("PAGADA", resultado.getEstado());
    }
}
