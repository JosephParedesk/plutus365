package com.pos_backend.contabilidad.domain.usecase;

import com.pos_backend.contabilidad.domain.model.*;
import com.pos_backend.contabilidad.domain.model.gateway.AsientoContableGateway;
import com.pos_backend.contabilidad.domain.model.gateway.CuentaContableGateway;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Reglas de mapeo automático venta/compra → cuentas del PUC.
 *
 * IMPORTANTE: estas son reglas POR DEFECTO razonables para una pyme típica,
 * no son una verdad contable universal. Cosas como "toda tarjeta va a Bancos"
 * o "todo activo fijo sin más contexto va a Equipo de cómputo" son
 * simplificaciones. Revísalas con tu contador antes de confiar en los
 * balances que salgan de aquí para declarar impuestos.
 */
@RequiredArgsConstructor
public class AsientoContableUseCase {

    private final AsientoContableGateway asientoContableGateway;
    private final CuentaContableGateway cuentaContableGateway;

    // Cuentas por defecto (código del PUC base sembrado por contabilidad-service)
    private static final String CAJA = "11050501";
    private static final String BANCO = "11100501";
    private static final String CLIENTES = "13050501";
    private static final String INVENTARIO = "14350501";
    private static final String MUEBLES_ENSERES = "15240501";
    private static final String EQUIPO_COMPUTO = "15280501";
    private static final String GASTOS_DIVERSOS = "51959501";
    private static final String PROVEEDORES = "22050501";
    private static final String IVA_POR_PAGAR = "24080101";
    private static final String VENTA_MERCANCIAS = "41350501";
    private static final String OTROS_INGRESOS = "42180501";
    private static final String ANTICIPOS_CLIENTES = "26100501";

    public List<AsientoContable> listar(String empresaId) {
        return asientoContableGateway.listar(empresaId);
    }

    public AsientoContable buscarPorId(Long asientoId, String empresaId) {
        AsientoContable asiento = asientoContableGateway.buscarPorId(asientoId, empresaId);
        if (asiento == null) throw new NoSuchElementException("Asiento no encontrado");
        return asiento;
    }

    // ─── Creación manual (partida doble validada) ──────────────────────────

    public AsientoContable crear(AsientoContable asiento, String empresaId) {
        asiento.setEmpresaId(empresaId);
        asiento.setOrigen(asiento.getOrigen() != null ? asiento.getOrigen() : "MANUAL");
        asiento.setEstado("CONTABILIZADO");
        asiento.setFecha(asiento.getFecha() != null ? asiento.getFecha() : LocalDate.now());
        validarYCompletarMovimientos(asiento, empresaId);
        asiento.setNumero(asientoContableGateway.generarSiguienteNumero(empresaId));
        return asientoContableGateway.guardar(asiento);
    }

    // ─── Generación automática desde una venta ─────────────────────────────

    public AsientoContable generarDesdeVenta(VentaParaAsiento venta, String empresaId, String creadoPor) {
        AsientoContable existente = asientoContableGateway.buscarPorOrigenYReferencia("VENTA", venta.getVentaId(), empresaId);
        if (existente != null) return existente; // idempotente: no duplica si ya se contabilizó

        List<MovimientoContable> movimientos = new ArrayList<>();

        // Debe: dinero que entra, por cada forma de pago
        for (VentaParaAsiento.FormaPagoAsiento fp : venta.getFormasPago()) {
            String cuenta = "EFECTIVO".equals(fp.getMetodo()) ? CAJA : BANCO;
            movimientos.add(new MovimientoContable(cuenta, null, fp.getValor(), 0.0, "Cobro venta " + venta.getNumeroVenta()));
        }

        // Haber: ingreso por ventas (neto de descuento) + IVA generado
        double ventaNeta = safe(venta.getSubtotal()) - safe(venta.getDescuentoTotal());
        movimientos.add(new MovimientoContable(VENTA_MERCANCIAS, null, 0.0, ventaNeta, "Venta " + venta.getNumeroVenta()));

        if (safe(venta.getTotalIva()) > 0) {
            movimientos.add(new MovimientoContable(IVA_POR_PAGAR, null, 0.0, venta.getTotalIva(), "IVA venta " + venta.getNumeroVenta()));
        }

        AsientoContable asiento = new AsientoContable();
        asiento.setEmpresaId(empresaId);
        asiento.setFecha(venta.getFecha() != null ? venta.getFecha() : LocalDate.now());
        asiento.setDescripcion("Venta " + venta.getNumeroVenta());
        asiento.setOrigen("VENTA");
        asiento.setReferenciaId(venta.getVentaId());
        asiento.setEstado("CONTABILIZADO");
        asiento.setMovimientos(movimientos);
        asiento.setCreadoPor(creadoPor);

        validarYCompletarMovimientos(asiento, empresaId);
        asiento.setNumero(asientoContableGateway.generarSiguienteNumero(empresaId));
        return asientoContableGateway.guardar(asiento);
    }

    /**
     * Saldo inicial de inventario (carga de arranque, una sola vez por empresa):
     * Debe → Inventario, Haber → la cuenta puente que el usuario/contador elija
     * (patrimonio o la que corresponda). Es el mismo patrón que usa Siigo: una
     * cuenta puente temporal solo para dejar cuadrado el balance de apertura.
     * Idempotente por (SALDO_INICIAL_INVENTARIO, empresaId) con referenciaId fijo
     * en 0L — esta operación es de una sola vez, no por lote ni por producto.
     */
    public AsientoContable generarDesdeSaldoInicialInventario(SaldoInicialInventarioParaAsiento datos, String empresaId, String creadoPor) {
        AsientoContable existente = asientoContableGateway.buscarPorOrigenYReferencia("SALDO_INICIAL_INVENTARIO", 0L, empresaId);
        if (existente != null)
            throw new RuntimeException("Ya cargaste el saldo inicial de inventario de esta empresa (asiento " +
                    existente.getNumero() + "). Para corregirlo, hacé un ajuste manual en vez de recargar.");

        if (datos.getValorTotal() == null || datos.getValorTotal() <= 0)
            throw new RuntimeException("El valor total del saldo inicial debe ser mayor a 0");
        if (datos.getCuentaContrapartida() == null || datos.getCuentaContrapartida().isBlank())
            throw new RuntimeException("Debes indicar la cuenta contrapartida (la que definiste con tu contador)");

        List<MovimientoContable> movimientos = new ArrayList<>();
        movimientos.add(new MovimientoContable(INVENTARIO, null, datos.getValorTotal(), 0.0, "Saldo inicial de inventario"));
        movimientos.add(new MovimientoContable(datos.getCuentaContrapartida(), null, 0.0, datos.getValorTotal(), "Saldo inicial de inventario"));

        AsientoContable asiento = new AsientoContable();
        asiento.setEmpresaId(empresaId);
        asiento.setFecha(datos.getFechaCorte() != null ? datos.getFechaCorte() : LocalDate.now());
        asiento.setDescripcion("Saldo inicial de inventario");
        asiento.setOrigen("SALDO_INICIAL_INVENTARIO");
        asiento.setReferenciaId(0L);
        asiento.setEstado("CONTABILIZADO");
        asiento.setMovimientos(movimientos);
        asiento.setCreadoPor(creadoPor);

        validarYCompletarMovimientos(asiento, empresaId);
        asiento.setNumero(asientoContableGateway.generarSiguienteNumero(empresaId));
        return asientoContableGateway.guardar(asiento);
    }

    /**
     * Nota crédito de venta: reversa (total o parcialmente) una venta ya facturada.
     * Es el espejo exacto del asiento de venta:
     *   Debe  → Venta de mercancías (baja el ingreso) + IVA por pagar (baja el IVA a pagar)
     *   Haber → Caja o Bancos (sale la plata que se le devuelve al cliente)
     */
    public AsientoContable generarDesdeNotaCredito(NotaCreditoParaAsiento nota, String empresaId, String creadoPor) {
        AsientoContable existente = asientoContableGateway.buscarPorOrigenYReferencia(
                "NOTA_CREDITO", nota.getNotaCreditoId(), empresaId);
        if (existente != null) return existente;

        List<MovimientoContable> movimientos = new ArrayList<>();
        movimientos.add(new MovimientoContable(VENTA_MERCANCIAS, null, safe(nota.getSubtotal()), 0.0,
                "Reversión venta - " + nota.getNumeroNota()));

        if (safe(nota.getTotalIva()) > 0) {
            movimientos.add(new MovimientoContable(IVA_POR_PAGAR, null, nota.getTotalIva(), 0.0,
                    "Reversión IVA - " + nota.getNumeroNota()));
        }

        String cuentaSalida = "EFECTIVO".equals(nota.getMetodoPagoOriginal()) ? CAJA : BANCO;
        movimientos.add(new MovimientoContable(cuentaSalida, null, 0.0, safe(nota.getTotal()),
                "Devolución al cliente - " + nota.getNumeroNota()));

        AsientoContable asiento = new AsientoContable();
        asiento.setEmpresaId(empresaId);
        asiento.setFecha(nota.getFecha() != null ? nota.getFecha() : LocalDate.now());
        asiento.setDescripcion("Nota crédito " + nota.getNumeroNota());
        asiento.setOrigen("NOTA_CREDITO");
        asiento.setReferenciaId(nota.getNotaCreditoId());
        asiento.setEstado("CONTABILIZADO");
        asiento.setMovimientos(movimientos);
        asiento.setCreadoPor(creadoPor);

        validarYCompletarMovimientos(asiento, empresaId);
        asiento.setNumero(asientoContableGateway.generarSiguienteNumero(empresaId));
        return asientoContableGateway.guardar(asiento);
    }

    /**
     * Recibo de caja: el cliente paga. Entra plata y baja la cartera.
     *   Debe  → Caja o Bancos
     *   Haber → Clientes (o Anticipos de clientes si aún no hay factura)
     */
    public AsientoContable generarDesdeReciboCaja(ReciboCajaParaAsiento recibo, String empresaId, String creadoPor) {
        AsientoContable existente = asientoContableGateway.buscarPorOrigenYReferencia(
                "RECIBO_CAJA", recibo.getReciboId(), empresaId);
        if (existente != null) return existente;

        String cuentaEntrada = "EFECTIVO".equals(recibo.getOrigenDinero()) ? CAJA : BANCO;
        // Un anticipo todavía no cancela cartera: es un pasivo con el cliente.
        String cuentaContra = Boolean.TRUE.equals(recibo.getEsAnticipo()) ? ANTICIPOS_CLIENTES : CLIENTES;

        List<MovimientoContable> movimientos = List.of(
                new MovimientoContable(cuentaEntrada, null, safe(recibo.getTotal()), 0.0, recibo.getNumeroRecibo()),
                new MovimientoContable(cuentaContra, null, 0.0, safe(recibo.getTotal()), recibo.getNumeroRecibo())
        );

        AsientoContable asiento = new AsientoContable();
        asiento.setEmpresaId(empresaId);
        asiento.setFecha(recibo.getFecha() != null ? recibo.getFecha() : LocalDate.now());
        asiento.setDescripcion("Recibo de caja " + recibo.getNumeroRecibo());
        asiento.setOrigen("RECIBO_CAJA");
        asiento.setReferenciaId(recibo.getReciboId());
        asiento.setEstado("CONTABILIZADO");
        asiento.setMovimientos(new ArrayList<>(movimientos));
        asiento.setCreadoPor(creadoPor);

        validarYCompletarMovimientos(asiento, empresaId);
        asiento.setNumero(asientoContableGateway.generarSiguienteNumero(empresaId));
        return asientoContableGateway.guardar(asiento);
    }

    // ─── Generación automática desde una compra ────────────────────────────

    public AsientoContable generarDesdeCompra(CompraParaAsiento compra, String empresaId, String creadoPor) {
        if ("ORDEN_COMPRA".equals(compra.getTipoTransaccion()))
            return null; // una orden de compra no es todavía una obligación contable

        AsientoContable existente = asientoContableGateway.buscarPorOrigenYReferencia("COMPRA", compra.getCompraId(), empresaId);
        if (existente != null) return existente;

        List<MovimientoContable> movimientos = switch (compra.getTipoTransaccion()) {
            case "FACTURA_COMPRA", "DOCUMENTO_SOPORTE", "FACTURA_COMPRA_ELECTRONICA" -> movimientosCompraFuente(compra);
            case "RECIBO_PAGO" -> movimientosReciboPago(compra);
            case "NOTA_DEBITO" -> movimientosNotaDebito(compra);
            case "AJUSTE_CARTERA" -> movimientosAjusteCartera(compra);
            default -> throw new RuntimeException("Tipo de transacción no contabilizable: " + compra.getTipoTransaccion());
        };

        AsientoContable asiento = new AsientoContable();
        asiento.setEmpresaId(empresaId);
        asiento.setFecha(compra.getFecha() != null ? compra.getFecha() : LocalDate.now());
        asiento.setDescripcion("Compra " + compra.getNumeroComprobante());
        asiento.setOrigen("COMPRA");
        asiento.setReferenciaId(compra.getCompraId());
        asiento.setEstado("CONTABILIZADO");
        asiento.setMovimientos(movimientos);
        asiento.setCreadoPor(creadoPor);

        validarYCompletarMovimientos(asiento, empresaId);
        asiento.setNumero(asientoContableGateway.generarSiguienteNumero(empresaId));
        return asientoContableGateway.guardar(asiento);
    }

    private List<MovimientoContable> movimientosCompraFuente(CompraParaAsiento compra) {
        List<MovimientoContable> movimientos = new ArrayList<>();

        // Debe: según el tipo de cada ítem (activo fijo / inventario / gasto)
        for (CompraParaAsiento.ItemAsiento item : compra.getItems()) {
            String cuenta = cuentaSegunTipoItem(item);
            movimientos.add(new MovimientoContable(cuenta, null, item.getValorTotal(), 0.0, compra.getNumeroComprobante()));
        }

        // El IVA pagado en la compra es descontable. Se usa la misma cuenta de IVA
        // que la venta (24080101), en el sentido contrario: la compra la debita
        // (reduce el IVA neto a pagar), la venta la acredita (lo aumenta). Es la
        // simplificación de "cuenta única" que usa la mayoría del software contable
        // de pymes en vez de llevar IVA generado e IVA descontable por separado.
        if (compra.getTotalIva() != null && compra.getTotalIva() > 0) {
            movimientos.add(new MovimientoContable(IVA_POR_PAGAR, null, compra.getTotalIva(), 0.0, compra.getNumeroComprobante()));
        }

        // Haber: proveedor (si queda a crédito) o caja/bancos (si se pagó de una)
        if (Boolean.TRUE.equals(compra.getTieneCreditoProveedor())) {
            movimientos.add(new MovimientoContable(PROVEEDORES, null, 0.0, compra.getTotalPagar(), compra.getNumeroComprobante()));
        } else {
            for (CompraParaAsiento.FormaPagoAsiento fp : compra.getFormasPago()) {
                String cuenta = "EFECTIVO".equals(fp.getMetodo()) ? CAJA : BANCO;
                movimientos.add(new MovimientoContable(cuenta, null, 0.0, fp.getValor(), compra.getNumeroComprobante()));
            }
        }
        return movimientos;
    }

    private List<MovimientoContable> movimientosReciboPago(CompraParaAsiento compra) {
        // Abono a deuda: se reduce lo que le debemos al proveedor y sale dinero de caja/banco.
        String cuentaOrigen = origenDineroHeuristico(compra);
        return List.of(
                new MovimientoContable(PROVEEDORES, null, compra.getTotalPagar(), 0.0, compra.getNumeroComprobante()),
                new MovimientoContable(cuentaOrigen, null, 0.0, compra.getTotalPagar(), compra.getNumeroComprobante())
        );
    }

    private List<MovimientoContable> movimientosNotaDebito(CompraParaAsiento compra) {
        // Simplificación: el cargo adicional se lleva a gastos diversos.
        return List.of(
                new MovimientoContable(GASTOS_DIVERSOS, null, compra.getTotalPagar(), 0.0, compra.getNumeroComprobante()),
                new MovimientoContable(PROVEEDORES, null, 0.0, compra.getTotalPagar(), compra.getNumeroComprobante())
        );
    }

    private List<MovimientoContable> movimientosAjusteCartera(CompraParaAsiento compra) {
        double valor = safe(compra.getTotalPagar());
        if (valor >= 0) {
            return List.of(
                    new MovimientoContable(GASTOS_DIVERSOS, null, valor, 0.0, compra.getNumeroComprobante()),
                    new MovimientoContable(PROVEEDORES, null, 0.0, valor, compra.getNumeroComprobante())
            );
        }
        double abs = Math.abs(valor);
        return List.of(
                new MovimientoContable(PROVEEDORES, null, abs, 0.0, compra.getNumeroComprobante()),
                new MovimientoContable(OTROS_INGRESOS, null, 0.0, abs, compra.getNumeroComprobante())
        );
    }

    private String cuentaSegunTipoItem(CompraParaAsiento.ItemAsiento item) {
        // Si el usuario ya eligió la cuenta explícitamente en el formulario, se respeta esa.
        if (item.getCuentaContableCodigo() != null && !item.getCuentaContableCodigo().isBlank())
            return item.getCuentaContableCodigo();

        if ("PRODUCTO".equals(item.getTipo())) return INVENTARIO;
        if ("GASTO_CUENTA".equals(item.getTipo())) return GASTOS_DIVERSOS;
        // ACTIVO_FIJO: heurística simple por descripción, ante falta de más detalle
        String desc = item.getDescripcion() != null ? item.getDescripcion().toLowerCase() : "";
        if (desc.contains("comput") || desc.contains("portátil") || desc.contains("portatil") || desc.contains("laptop"))
            return EQUIPO_COMPUTO;
        return MUEBLES_ENSERES;
    }

    private String origenDineroHeuristico(CompraParaAsiento compra) {
        // RECIBO_PAGO usa "origenDinero" (texto libre) en vez de formasPago estructuradas.
        if (compra.getFormasPago() != null && !compra.getFormasPago().isEmpty()) {
            String metodo = compra.getFormasPago().get(0).getMetodo();
            return "EFECTIVO".equals(metodo) ? CAJA : BANCO;
        }
        return BANCO;
    }

    // ─── Validación de partida doble ────────────────────────────────────────

    private void validarYCompletarMovimientos(AsientoContable asiento, String empresaId) {
        if (asiento.getMovimientos() == null || asiento.getMovimientos().isEmpty())
            throw new RuntimeException("El asiento debe tener al menos dos movimientos");

        double totalDebe = 0, totalHaber = 0;
        for (MovimientoContable mov : asiento.getMovimientos()) {
            CuentaContable cuenta = cuentaContableGateway.buscarPorCodigo(mov.getCuentaCodigo(), empresaId);
            if (cuenta == null)
                throw new NoSuchElementException("La cuenta " + mov.getCuentaCodigo() + " no existe en tu plan de cuentas");
            if (!Boolean.TRUE.equals(cuenta.getEsTransaccional()))
                throw new RuntimeException("La cuenta " + mov.getCuentaCodigo() + " (" + cuenta.getNombre() +
                        ") no es transaccional — no puede recibir movimientos directos");

            mov.setCuentaNombre(cuenta.getNombre());
            totalDebe += safe(mov.getDebe());
            totalHaber += safe(mov.getHaber());
        }

        // Redondeo de centavos: se tolera una diferencia mínima antes de rechazar.
        if (Math.abs(totalDebe - totalHaber) > 1.0)
            throw new RuntimeException("El asiento no cuadra: Debe " + totalDebe + " vs Haber " + totalHaber);

        asiento.setTotalDebe(totalDebe);
        asiento.setTotalHaber(totalHaber);
    }

    private double safe(Double valor) {
        return valor != null ? valor : 0.0;
    }
}
