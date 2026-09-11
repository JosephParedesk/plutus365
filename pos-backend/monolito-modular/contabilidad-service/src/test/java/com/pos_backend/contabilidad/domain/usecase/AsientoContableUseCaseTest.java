package com.pos_backend.contabilidad.domain.usecase;

import com.pos_backend.contabilidad.domain.model.*;
import com.pos_backend.contabilidad.domain.model.gateway.AsientoContableGateway;
import com.pos_backend.contabilidad.domain.model.gateway.CuentaContableGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests de caracterización escritos ANTES de migrar este módulo al monolito
 * modular (0 tests hoy en el standalone, es el módulo con más lógica
 * sensible del proyecto: idempotencia contable, partida doble). Documentan
 * el comportamiento ACTUAL, no cómo "debería" ser — si algo acá parece raro,
 * es fiel al código, no un ideal.
 */
class AsientoContableUseCaseTest {

    private AsientoContableGateway asientoGateway;
    private CuentaContableGateway cuentaGateway;
    private AsientoContableUseCase useCase;

    private static final String EMPRESA = "empresa-1";

    @BeforeEach
    void setUp() {
        asientoGateway = mock(AsientoContableGateway.class);
        cuentaGateway = mock(CuentaContableGateway.class);
        useCase = new AsientoContableUseCase(asientoGateway, cuentaGateway);

        // Todas las cuentas usadas por las reglas de mapeo existen y son transaccionales
        // por default, salvo que un test la sobreescriba explícitamente.
        when(cuentaGateway.buscarPorCodigo(anyString(), eq(EMPRESA)))
                .thenAnswer(inv -> cuentaTransaccional(inv.getArgument(0)));
        when(asientoGateway.generarSiguienteNumero(EMPRESA)).thenReturn("AS-00001");
        when(asientoGateway.guardar(any(AsientoContable.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private CuentaContable cuentaTransaccional(String codigo) {
        CuentaContable c = new CuentaContable();
        c.setCodigo(codigo);
        c.setNombre("Cuenta " + codigo);
        c.setEsTransaccional(true);
        c.setNaturaleza("DEBITO");
        return c;
    }

    // ─── generarDesdeVenta ──────────────────────────────────────────────

    @Test
    void generarDesdeVenta_esIdempotente_siYaExisteNoDuplica() {
        VentaParaAsiento venta = new VentaParaAsiento(1L, "V-001", LocalDate.now(), 10L,
                100000.0, 0.0, 19000.0, 119000.0,
                List.of(new VentaParaAsiento.FormaPagoAsiento("EFECTIVO", 119000.0)));

        AsientoContable existente = new AsientoContable();
        existente.setNumero("AS-00099");
        when(asientoGateway.buscarPorOrigenYReferencia("VENTA", 1L, EMPRESA)).thenReturn(existente);

        AsientoContable resultado = useCase.generarDesdeVenta(venta, EMPRESA, "tester");

        assertSame(existente, resultado);
        verify(asientoGateway, never()).guardar(any());
    }

    @Test
    void generarDesdeVenta_pagoEnEfectivo_vaACaja() {
        VentaParaAsiento venta = new VentaParaAsiento(2L, "V-002", LocalDate.now(), 10L,
                100000.0, 0.0, 19000.0, 119000.0,
                List.of(new VentaParaAsiento.FormaPagoAsiento("EFECTIVO", 119000.0)));
        when(asientoGateway.buscarPorOrigenYReferencia("VENTA", 2L, EMPRESA)).thenReturn(null);

        AsientoContable asiento = useCase.generarDesdeVenta(venta, EMPRESA, "tester");

        assertEquals("11050501", asiento.getMovimientos().get(0).getCuentaCodigo()); // CAJA
        assertEquals(119000.0, asiento.getMovimientos().get(0).getDebe());
        assertEquals(119000.0, asiento.getTotalDebe());
        assertEquals(119000.0, asiento.getTotalHaber());
    }

    @Test
    void generarDesdeVenta_pagoConTarjeta_vaABancos() {
        VentaParaAsiento venta = new VentaParaAsiento(3L, "V-003", LocalDate.now(), 10L,
                100000.0, 0.0, 19000.0, 119000.0,
                List.of(new VentaParaAsiento.FormaPagoAsiento("TARJETA", 119000.0)));
        when(asientoGateway.buscarPorOrigenYReferencia("VENTA", 3L, EMPRESA)).thenReturn(null);

        AsientoContable asiento = useCase.generarDesdeVenta(venta, EMPRESA, "tester");

        assertEquals("11100501", asiento.getMovimientos().get(0).getCuentaCodigo()); // BANCO
    }

    @Test
    void generarDesdeVenta_sinIva_noAgregaLineaDeIva() {
        VentaParaAsiento venta = new VentaParaAsiento(4L, "V-004", LocalDate.now(), 10L,
                50000.0, 0.0, 0.0, 50000.0,
                List.of(new VentaParaAsiento.FormaPagoAsiento("EFECTIVO", 50000.0)));
        when(asientoGateway.buscarPorOrigenYReferencia("VENTA", 4L, EMPRESA)).thenReturn(null);

        AsientoContable asiento = useCase.generarDesdeVenta(venta, EMPRESA, "tester");

        boolean tieneLineaIva = asiento.getMovimientos().stream()
                .anyMatch(m -> "24080101".equals(m.getCuentaCodigo()));
        assertFalse(tieneLineaIva);
    }

    @Test
    void generarDesdeVenta_netoDeDescuento_esLoQueVaAVentaMercancias() {
        VentaParaAsiento venta = new VentaParaAsiento(5L, "V-005", LocalDate.now(), 10L,
                100000.0, 10000.0, 0.0, 90000.0,
                List.of(new VentaParaAsiento.FormaPagoAsiento("EFECTIVO", 90000.0)));
        when(asientoGateway.buscarPorOrigenYReferencia("VENTA", 5L, EMPRESA)).thenReturn(null);

        AsientoContable asiento = useCase.generarDesdeVenta(venta, EMPRESA, "tester");

        MovimientoContable ventaMercancias = asiento.getMovimientos().stream()
                .filter(m -> "41350501".equals(m.getCuentaCodigo())).findFirst().orElseThrow();
        assertEquals(90000.0, ventaMercancias.getHaber());
    }

    // ─── generarDesdeCompra ─────────────────────────────────────────────

    @Test
    void generarDesdeCompra_ordenDeCompra_noContabilizaYDevuelveNull() {
        CompraParaAsiento compra = new CompraParaAsiento();
        compra.setTipoTransaccion("ORDEN_COMPRA");

        AsientoContable resultado = useCase.generarDesdeCompra(compra, EMPRESA, "tester");

        assertNull(resultado);
        verifyNoInteractions(asientoGateway);
    }

    @Test
    void generarDesdeCompra_esIdempotente() {
        CompraParaAsiento compra = new CompraParaAsiento();
        compra.setCompraId(20L);
        compra.setTipoTransaccion("FACTURA_COMPRA");
        AsientoContable existente = new AsientoContable();
        when(asientoGateway.buscarPorOrigenYReferencia("COMPRA", 20L, EMPRESA)).thenReturn(existente);

        AsientoContable resultado = useCase.generarDesdeCompra(compra, EMPRESA, "tester");

        assertSame(existente, resultado);
    }

    @Test
    void generarDesdeCompra_facturaCompra_productoVaAInventarioYQuedaACredito() {
        CompraParaAsiento compra = new CompraParaAsiento();
        compra.setCompraId(21L);
        compra.setTipoTransaccion("FACTURA_COMPRA");
        compra.setNumeroComprobante("FC-001");
        compra.setTotalIva(0.0);
        compra.setTieneCreditoProveedor(true);
        compra.setTotalPagar(50000.0);
        CompraParaAsiento.ItemAsiento item = new CompraParaAsiento.ItemAsiento("PRODUCTO", "Mercancía", 50000.0, null);
        compra.setItems(List.of(item));
        when(asientoGateway.buscarPorOrigenYReferencia("COMPRA", 21L, EMPRESA)).thenReturn(null);

        AsientoContable asiento = useCase.generarDesdeCompra(compra, EMPRESA, "tester");

        assertEquals("14350501", asiento.getMovimientos().get(0).getCuentaCodigo()); // INVENTARIO
        assertEquals("22050501", asiento.getMovimientos().get(1).getCuentaCodigo()); // PROVEEDORES
    }

    @Test
    void generarDesdeCompra_activoFijoConDescripcionComputador_vaAEquipoDeComputo() {
        CompraParaAsiento compra = new CompraParaAsiento();
        compra.setCompraId(22L);
        compra.setTipoTransaccion("FACTURA_COMPRA");
        compra.setNumeroComprobante("FC-002");
        compra.setTotalIva(0.0);
        compra.setTieneCreditoProveedor(true);
        compra.setTotalPagar(2000000.0);
        compra.setItems(List.of(new CompraParaAsiento.ItemAsiento("ACTIVO_FIJO", "Laptop Dell", 2000000.0, null)));
        when(asientoGateway.buscarPorOrigenYReferencia("COMPRA", 22L, EMPRESA)).thenReturn(null);

        AsientoContable asiento = useCase.generarDesdeCompra(compra, EMPRESA, "tester");

        assertEquals("15280501", asiento.getMovimientos().get(0).getCuentaCodigo()); // EQUIPO_COMPUTO
    }

    @Test
    void generarDesdeCompra_activoFijoSinPistaEnDescripcion_vaAMueblesYEnseres() {
        CompraParaAsiento compra = new CompraParaAsiento();
        compra.setCompraId(23L);
        compra.setTipoTransaccion("FACTURA_COMPRA");
        compra.setNumeroComprobante("FC-003");
        compra.setTotalIva(0.0);
        compra.setTieneCreditoProveedor(true);
        compra.setTotalPagar(500000.0);
        compra.setItems(List.of(new CompraParaAsiento.ItemAsiento("ACTIVO_FIJO", "Escritorio", 500000.0, null)));
        when(asientoGateway.buscarPorOrigenYReferencia("COMPRA", 23L, EMPRESA)).thenReturn(null);

        AsientoContable asiento = useCase.generarDesdeCompra(compra, EMPRESA, "tester");

        assertEquals("15240501", asiento.getMovimientos().get(0).getCuentaCodigo()); // MUEBLES_ENSERES
    }

    @Test
    void generarDesdeCompra_cuentaExplicitaDelItem_seRespetaSobreLaHeuristica() {
        CompraParaAsiento compra = new CompraParaAsiento();
        compra.setCompraId(24L);
        compra.setTipoTransaccion("FACTURA_COMPRA");
        compra.setNumeroComprobante("FC-004");
        compra.setTotalIva(0.0);
        compra.setTieneCreditoProveedor(true);
        compra.setTotalPagar(30000.0);
        compra.setItems(List.of(new CompraParaAsiento.ItemAsiento("GASTO_CUENTA", "Papelería", 30000.0, "51959901")));
        when(asientoGateway.buscarPorOrigenYReferencia("COMPRA", 24L, EMPRESA)).thenReturn(null);

        AsientoContable asiento = useCase.generarDesdeCompra(compra, EMPRESA, "tester");

        assertEquals("51959901", asiento.getMovimientos().get(0).getCuentaCodigo());
    }

    @Test
    void generarDesdeCompra_pagadaDeContado_vaACajaOBancosEnVezDeProveedores() {
        CompraParaAsiento compra = new CompraParaAsiento();
        compra.setCompraId(25L);
        compra.setTipoTransaccion("FACTURA_COMPRA");
        compra.setNumeroComprobante("FC-005");
        compra.setTotalIva(0.0);
        compra.setTieneCreditoProveedor(false);
        compra.setFormasPago(List.of(new CompraParaAsiento.FormaPagoAsiento("EFECTIVO", 40000.0)));
        compra.setItems(List.of(new CompraParaAsiento.ItemAsiento("GASTO_CUENTA", "Aseo", 40000.0, null)));
        when(asientoGateway.buscarPorOrigenYReferencia("COMPRA", 25L, EMPRESA)).thenReturn(null);

        AsientoContable asiento = useCase.generarDesdeCompra(compra, EMPRESA, "tester");

        boolean tieneCaja = asiento.getMovimientos().stream().anyMatch(m -> "11050501".equals(m.getCuentaCodigo()));
        boolean tieneProveedores = asiento.getMovimientos().stream().anyMatch(m -> "22050501".equals(m.getCuentaCodigo()));
        assertTrue(tieneCaja);
        assertFalse(tieneProveedores);
    }

    @Test
    void generarDesdeCompra_tipoDesconocido_lanzaExcepcion() {
        CompraParaAsiento compra = new CompraParaAsiento();
        compra.setCompraId(26L);
        compra.setTipoTransaccion("TIPO_QUE_NO_EXISTE");
        when(asientoGateway.buscarPorOrigenYReferencia("COMPRA", 26L, EMPRESA)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> useCase.generarDesdeCompra(compra, EMPRESA, "tester"));
    }

    // ─── generarDesdeNotaCredito ────────────────────────────────────────

    @Test
    void generarDesdeNotaCredito_esIdempotente() {
        NotaCreditoParaAsiento nota = new NotaCreditoParaAsiento(30L, "NC-001", LocalDate.now(), 100000.0, 19000.0, 119000.0, "EFECTIVO");
        AsientoContable existente = new AsientoContable();
        when(asientoGateway.buscarPorOrigenYReferencia("NOTA_CREDITO", 30L, EMPRESA)).thenReturn(existente);

        assertSame(existente, useCase.generarDesdeNotaCredito(nota, EMPRESA, "tester"));
    }

    @Test
    void generarDesdeNotaCredito_reversaVentaYDevuelveDineroDeCaja() {
        NotaCreditoParaAsiento nota = new NotaCreditoParaAsiento(31L, "NC-002", LocalDate.now(), 100000.0, 19000.0, 119000.0, "EFECTIVO");
        when(asientoGateway.buscarPorOrigenYReferencia("NOTA_CREDITO", 31L, EMPRESA)).thenReturn(null);

        AsientoContable asiento = useCase.generarDesdeNotaCredito(nota, EMPRESA, "tester");

        MovimientoContable salidaCaja = asiento.getMovimientos().stream()
                .filter(m -> "11050501".equals(m.getCuentaCodigo())).findFirst().orElseThrow();
        assertEquals(119000.0, salidaCaja.getHaber());
        assertEquals(119000.0, asiento.getTotalDebe());
    }

    // ─── generarDesdeReciboCaja ─────────────────────────────────────────

    @Test
    void generarDesdeReciboCaja_esIdempotente() {
        ReciboCajaParaAsiento recibo = new ReciboCajaParaAsiento(40L, "RC-001", LocalDate.now(), 50000.0, "EFECTIVO", false);
        AsientoContable existente = new AsientoContable();
        when(asientoGateway.buscarPorOrigenYReferencia("RECIBO_CAJA", 40L, EMPRESA)).thenReturn(existente);

        assertSame(existente, useCase.generarDesdeReciboCaja(recibo, EMPRESA, "tester"));
    }

    @Test
    void generarDesdeReciboCaja_normal_abonaACuentaClientes() {
        ReciboCajaParaAsiento recibo = new ReciboCajaParaAsiento(41L, "RC-002", LocalDate.now(), 50000.0, "EFECTIVO", false);
        when(asientoGateway.buscarPorOrigenYReferencia("RECIBO_CAJA", 41L, EMPRESA)).thenReturn(null);

        AsientoContable asiento = useCase.generarDesdeReciboCaja(recibo, EMPRESA, "tester");

        assertEquals("13050501", asiento.getMovimientos().get(1).getCuentaCodigo()); // CLIENTES
    }

    @Test
    void generarDesdeReciboCaja_anticipo_vaAAnticiposDeClientesNoAClientes() {
        ReciboCajaParaAsiento recibo = new ReciboCajaParaAsiento(42L, "RC-003", LocalDate.now(), 50000.0, "TRANSFERENCIA", true);
        when(asientoGateway.buscarPorOrigenYReferencia("RECIBO_CAJA", 42L, EMPRESA)).thenReturn(null);

        AsientoContable asiento = useCase.generarDesdeReciboCaja(recibo, EMPRESA, "tester");

        assertEquals("26100501", asiento.getMovimientos().get(1).getCuentaCodigo()); // ANTICIPOS_CLIENTES
        assertEquals("11100501", asiento.getMovimientos().get(0).getCuentaCodigo()); // BANCO (no EFECTIVO)
    }

    // ─── generarDesdeSaldoInicialInventario ─────────────────────────────

    @Test
    void generarDesdeSaldoInicialInventario_segundaVez_lanzaExcepcionEnVezDeDevolverElExistente() {
        // A diferencia de venta/compra/nota/recibo, esta operación NO es idempotente
        // silenciosa: si ya se cargó, lanza — es deliberadamente de una sola vez.
        SaldoInicialInventarioParaAsiento datos = new SaldoInicialInventarioParaAsiento(LocalDate.now(), 1000000.0, "31500501");
        AsientoContable existente = new AsientoContable();
        existente.setNumero("AS-00005");
        when(asientoGateway.buscarPorOrigenYReferencia("SALDO_INICIAL_INVENTARIO", 0L, EMPRESA)).thenReturn(existente);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> useCase.generarDesdeSaldoInicialInventario(datos, EMPRESA, "tester"));
        assertTrue(ex.getMessage().contains("AS-00005"));
    }

    @Test
    void generarDesdeSaldoInicialInventario_valorCero_lanzaExcepcion() {
        SaldoInicialInventarioParaAsiento datos = new SaldoInicialInventarioParaAsiento(LocalDate.now(), 0.0, "31500501");
        when(asientoGateway.buscarPorOrigenYReferencia("SALDO_INICIAL_INVENTARIO", 0L, EMPRESA)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> useCase.generarDesdeSaldoInicialInventario(datos, EMPRESA, "tester"));
    }

    @Test
    void generarDesdeSaldoInicialInventario_sinCuentaContrapartida_lanzaExcepcion() {
        SaldoInicialInventarioParaAsiento datos = new SaldoInicialInventarioParaAsiento(LocalDate.now(), 1000000.0, null);
        when(asientoGateway.buscarPorOrigenYReferencia("SALDO_INICIAL_INVENTARIO", 0L, EMPRESA)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> useCase.generarDesdeSaldoInicialInventario(datos, EMPRESA, "tester"));
    }

    @Test
    void generarDesdeSaldoInicialInventario_valido_generaAsientoBalanceado() {
        SaldoInicialInventarioParaAsiento datos = new SaldoInicialInventarioParaAsiento(LocalDate.now(), 1000000.0, "31500501");
        when(asientoGateway.buscarPorOrigenYReferencia("SALDO_INICIAL_INVENTARIO", 0L, EMPRESA)).thenReturn(null);

        AsientoContable asiento = useCase.generarDesdeSaldoInicialInventario(datos, EMPRESA, "tester");

        assertEquals(1000000.0, asiento.getTotalDebe());
        assertEquals(1000000.0, asiento.getTotalHaber());
        assertEquals(Long.valueOf(0L), asiento.getReferenciaId());
    }

    // ─── crear (manual) ──────────────────────────────────────────────────

    @Test
    void crear_sinOrigen_defaultAManual() {
        AsientoContable asiento = new AsientoContable();
        asiento.setMovimientos(List.of(
                new MovimientoContable("11050501", null, 1000.0, 0.0, "x"),
                new MovimientoContable("41350501", null, 0.0, 1000.0, "x")
        ));

        AsientoContable resultado = useCase.crear(asiento, EMPRESA);

        assertEquals("MANUAL", resultado.getOrigen());
        assertEquals("CONTABILIZADO", resultado.getEstado());
        assertNotNull(resultado.getFecha());
    }

    @Test
    void crear_movimientosVacios_lanzaExcepcion() {
        AsientoContable asiento = new AsientoContable();
        asiento.setMovimientos(List.of());

        assertThrows(RuntimeException.class, () -> useCase.crear(asiento, EMPRESA));
    }

    @Test
    void crear_cuentaQueNoExiste_lanzaNoSuchElement() {
        when(cuentaGateway.buscarPorCodigo("99999999", EMPRESA)).thenReturn(null);
        AsientoContable asiento = new AsientoContable();
        asiento.setMovimientos(List.of(new MovimientoContable("99999999", null, 1000.0, 0.0, "x")));

        assertThrows(NoSuchElementException.class, () -> useCase.crear(asiento, EMPRESA));
    }

    @Test
    void crear_cuentaNoTransaccional_lanzaExcepcion() {
        CuentaContable noTransaccional = new CuentaContable();
        noTransaccional.setCodigo("1105");
        noTransaccional.setNombre("Caja (grupo)");
        noTransaccional.setEsTransaccional(false);
        when(cuentaGateway.buscarPorCodigo("1105", EMPRESA)).thenReturn(noTransaccional);
        AsientoContable asiento = new AsientoContable();
        asiento.setMovimientos(List.of(new MovimientoContable("1105", null, 1000.0, 0.0, "x")));

        assertThrows(RuntimeException.class, () -> useCase.crear(asiento, EMPRESA));
    }

    @Test
    void crear_debeYHaberNoCuadran_lanzaExcepcion() {
        AsientoContable asiento = new AsientoContable();
        asiento.setMovimientos(List.of(
                new MovimientoContable("11050501", null, 1000.0, 0.0, "x"),
                new MovimientoContable("41350501", null, 0.0, 500.0, "x")
        ));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> useCase.crear(asiento, EMPRESA));
        assertTrue(ex.getMessage().contains("no cuadra"));
    }

    @Test
    void crear_diferenciaDeUnPesoOMenos_seTolera() {
        AsientoContable asiento = new AsientoContable();
        asiento.setMovimientos(List.of(
                new MovimientoContable("11050501", null, 1000.5, 0.0, "x"),
                new MovimientoContable("41350501", null, 0.0, 1000.0, "x")
        ));

        assertDoesNotThrow(() -> useCase.crear(asiento, EMPRESA));
    }

    @Test
    void buscarPorId_noEncontrado_lanzaNoSuchElement() {
        when(asientoGateway.buscarPorId(999L, EMPRESA)).thenReturn(null);
        assertThrows(NoSuchElementException.class, () -> useCase.buscarPorId(999L, EMPRESA));
    }
}
