package com.pos_backend.compra.domain.usecase;

import com.pos_backend.compra.domain.model.AplicacionPago;
import com.pos_backend.compra.domain.model.Compra;
import com.pos_backend.compra.domain.model.CompraItem;
import com.pos_backend.compra.domain.model.FormaPago;
import com.pos_backend.compra.domain.model.gateway.CompraGateway;
import com.pos_backend.compra.domain.model.gateway.ContabilidadGateway;
import com.pos_backend.compra.domain.model.gateway.StockGateway;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

@RequiredArgsConstructor
public class CompraUseCase {

    private final CompraGateway compraGateway;
    private final ContabilidadGateway contabilidadGateway;
    private final StockGateway stockGateway;

    // Documentos "fuente": generan una obligación de pago con el proveedor.
    private static final Set<String> TIPOS_FUENTE = Set.of(
            "FACTURA_COMPRA", "DOCUMENTO_SOPORTE", "FACTURA_COMPRA_ELECTRONICA");

    // Documentos que aplican sobre uno o más documentos fuente ya existentes.
    private static final Set<String> TIPOS_REFERENCIA = Set.of(
            "RECIBO_PAGO", "NOTA_DEBITO", "AJUSTE_CARTERA");

    private static final Set<String> TIPOS_VALIDOS = Set.of(
            "FACTURA_COMPRA", "DOCUMENTO_SOPORTE", "FACTURA_COMPRA_ELECTRONICA",
            "ORDEN_COMPRA", "RECIBO_PAGO", "NOTA_DEBITO", "AJUSTE_CARTERA");

    public List<Compra> listarCompras(String empresaId) {
        return compraGateway.listarCompras(empresaId);
    }

    public Compra buscarPorId(Long compraId, String empresaId) {
        Compra compra = compraGateway.buscarCompraPorId(compraId, empresaId);
        if (compra == null)
            throw new NoSuchElementException("Compra no encontrada");
        return compra;
    }

    public List<Compra> buscarConFiltros(
            String empresaId, Long proveedorId, String tipoTransaccion,
            LocalDate fechaInicio, LocalDate fechaFin, String creadoPor) {
        return compraGateway.buscarConFiltros(
                empresaId, proveedorId, tipoTransaccion, fechaInicio, fechaFin, creadoPor);
    }

    // Facturas pendientes de un proveedor (para armar el recibo de pago)
    public List<Compra> pendientesPorProveedor(String empresaId, Long proveedorId) {
        return compraGateway.buscarConFiltros(empresaId, proveedorId, null, null, null, null).stream()
                .filter(c -> TIPOS_FUENTE.contains(c.getTipoTransaccion()))
                .filter(c -> "REGISTRADA".equals(c.getEstado()))
                .filter(c -> c.getSaldoPendiente() != null && c.getSaldoPendiente() > 0)
                .toList();
    }

    // Cuentas por pagar (crédito a proveedores) que vencen pronto — para la notificación
    public List<Compra> proximasAVencer(String empresaId, int dias) {
        return compraGateway.proximasAVencer(empresaId, dias);
    }

    public Compra registrarCompra(Compra compra, String empresaId, String creadoPor) {
        validar(compra);

        compra.setEmpresaId(empresaId);
        compra.setCreadoPor(creadoPor);
        compra.setFechaElaboracion(
                compra.getFechaElaboracion() != null ? compra.getFechaElaboracion() : LocalDate.now());
        compra.setEstado("REGISTRADA");
        compra.setNumeroComprobante(
                compraGateway.generarSiguienteNumero(empresaId, compra.getTipoTransaccion()));

        if (TIPOS_FUENTE.contains(compra.getTipoTransaccion()) || compra.getTipoTransaccion().equals("ORDEN_COMPRA")) {
            calcularTotales(compra);
        }

        if (TIPOS_FUENTE.contains(compra.getTipoTransaccion())) {
            aplicarCreditoProveedor(compra);
        } else if (compra.getTipoTransaccion().equals("RECIBO_PAGO")) {
            procesarReciboPago(compra, empresaId);
        } else if (compra.getTipoTransaccion().equals("NOTA_DEBITO") || compra.getTipoTransaccion().equals("AJUSTE_CARTERA")) {
            aplicarSobreDocumentoReferencia(compra, empresaId);
        }

        Compra guardada = compraGateway.guardarCompra(compra);

        // Actualiza el inventario (crea el producto si no existía). Si falla,
        // se revierte lo que ya se alcanzó a sumar y se marca la compra con error.
        List<ItemStockActualizado> stockActualizado = new ArrayList<>();
        List<String> productosCreados = new ArrayList<>();
        if (TIPOS_FUENTE.contains(compra.getTipoTransaccion())) {
            try {
                actualizarInventario(guardada, empresaId, stockActualizado, productosCreados);
            } catch (RuntimeException e) {
                revertirStock(stockActualizado, empresaId);
                guardada.setEstado("ERROR_INVENTARIO");
                compraGateway.guardarCompra(guardada);
                throw new RuntimeException("No se pudo actualizar el inventario y la compra fue revertida: " + e.getMessage());
            }
        }

        // Contabilización obligatoria (salvo Orden de compra, que no es una
        // obligación contable todavía). Si falla, se revierte el efecto sobre
        // la compra referenciada y el inventario ya actualizado, y se marca con error.
        if (!compra.getTipoTransaccion().equals("ORDEN_COMPRA")) {
            try {
                contabilidadGateway.generarAsientoCompra(guardada, empresaId);
            } catch (RuntimeException e) {
                revertirEfectoSobreReferencias(guardada, empresaId);
                revertirStock(stockActualizado, empresaId);
                guardada.setEstado("ERROR_CONTABILIZACION");
                compraGateway.guardarCompra(guardada);
                throw new RuntimeException("La compra no se pudo contabilizar y fue revertida: " + e.getMessage());
            }
        }

        if (!productosCreados.isEmpty()) {
            String nota = "Se crearon automáticamente en inventario: " + String.join(", ", productosCreados) +
                    " — revisa su precio de venta, categoría y stock mínimo.";
            guardada.setObservaciones(guardada.getObservaciones() != null ? guardada.getObservaciones() + " " + nota : nota);
            guardada = compraGateway.guardarCompra(guardada);
        }

        return guardada;
    }

    /** Suma al inventario cada ítem tipo PRODUCTO con SKU; crea el producto si no existía todavía. */
    private void actualizarInventario(Compra compra, String empresaId, List<ItemStockActualizado> aplicados, List<String> productosCreados) {
        if (compra.getItems() == null) return;

        for (CompraItem item : compra.getItems()) {
            if (!"PRODUCTO".equals(item.getTipo())) continue;
            if (item.getProductoSku() == null || item.getProductoSku().isBlank()) continue;

            int cantidad = item.getCantidad() != null ? item.getCantidad().intValue() : 0;
            if (cantidad <= 0) continue;

            StockGateway.ResultadoStock resultado = stockGateway.registrarCompra(
                    item.getProductoSku(), item.getDescripcion(), cantidad, item.getValorUnitario(),
                    compra.getProveedorId(), compra.getProveedorNombre(), null, compra.getNumeroComprobante(), empresaId
            );

            aplicados.add(new ItemStockActualizado(item.getProductoSku(), cantidad));
            if (resultado.creado()) productosCreados.add(resultado.nombreProducto());
        }
    }

    private void revertirStock(List<ItemStockActualizado> aplicados, String empresaId) {
        for (ItemStockActualizado item : aplicados) {
            stockGateway.revertir(item.sku(), item.cantidad(), empresaId);
        }
    }

    private record ItemStockActualizado(String sku, Integer cantidad) {}

    public void anularCompra(Long compraId, String empresaId) {
        Compra existente = compraGateway.buscarCompraPorId(compraId, empresaId);
        if (existente == null)
            throw new NoSuchElementException("Compra no encontrada");

        revertirEfectoSobreReferencias(existente, empresaId);
        compraGateway.anularCompra(compraId, empresaId);
    }

    /** Revierte el efecto que un RECIBO_PAGO / NOTA_DEBITO / AJUSTE_CARTERA tuvo
     *  sobre el saldo pendiente de la(s) compra(s) que referencia. Se usa tanto
     *  al anular como para compensar si la contabilización llega a fallar. */
    private void revertirEfectoSobreReferencias(Compra existente, String empresaId) {
        if (existente.getTipoTransaccion().equals("RECIBO_PAGO") && existente.getAplicaciones() != null) {
            for (AplicacionPago aplicacion : existente.getAplicaciones()) {
                Compra original = compraGateway.buscarCompraPorId(aplicacion.getCompraReferenciaId(), empresaId);
                if (original != null) {
                    double saldoActual = original.getSaldoPendiente() != null ? original.getSaldoPendiente() : 0;
                    original.setSaldoPendiente(saldoActual + aplicacion.getValorAplicado());
                    compraGateway.guardarCompra(original);
                }
            }
        } else if (TIPOS_REFERENCIA.contains(existente.getTipoTransaccion()) && existente.getCompraReferenciaId() != null) {
            Compra original = compraGateway.buscarCompraPorId(existente.getCompraReferenciaId(), empresaId);
            if (original != null) {
                double saldoActual = original.getSaldoPendiente() != null ? original.getSaldoPendiente() : 0;
                original.setSaldoPendiente(saldoActual - existente.getTotalPagar());
                compraGateway.guardarCompra(original);
            }
        }
    }

    // ─── Lógica de crédito a proveedores ────────────────────────────────────

    private void aplicarCreditoProveedor(Compra compra) {
        FormaPago credito = compra.getFormasPago() == null ? null : compra.getFormasPago().stream()
                .filter(f -> "CREDITO_PROVEEDOR".equals(f.getMetodo()))
                .findFirst().orElse(null);

        if (credito != null) {
            if (credito.getFechaVencimiento() == null)
                throw new RuntimeException("Debes indicar la fecha de vencimiento del crédito a proveedores");

            compra.setTieneCreditoProveedor(true);
            compra.setFechaVencimientoCredito(credito.getFechaVencimiento());
            compra.setSaldoPendiente(compra.getTotalPagar());
        } else {
            compra.setTieneCreditoProveedor(false);
            compra.setSaldoPendiente(0.0);
        }
    }

    // ─── RECIBO_PAGO: abono a deuda (una o varias facturas), anticipo, o avanzado ──

    private void procesarReciboPago(Compra compra, String empresaId) {
        String tipoRecibo = compra.getTipoRecibo() != null ? compra.getTipoRecibo() : "ABONO_DEUDA";
        compra.setTipoRecibo(tipoRecibo);

        if (tipoRecibo.equals("ANTICIPO")) {
            // Anticipo: no aplica sobre ninguna factura existente todavía, es un saldo
            // a favor con el proveedor. No reduce saldoPendiente de ninguna compra.
            if (compra.getTotalPagar() == null || compra.getTotalPagar() <= 0)
                throw new RuntimeException("El valor pagado debe ser mayor a 0");
            return;
        }

        // ABONO_DEUDA o AVANZADO: aplica sobre una o varias facturas del mismo proveedor.
        if (compra.getAplicaciones() == null || compra.getAplicaciones().isEmpty())
            throw new RuntimeException("Selecciona al menos una factura a pagar");

        double sumaAplicaciones = 0;
        for (AplicacionPago aplicacion : compra.getAplicaciones()) {
            if (aplicacion.getCompraReferenciaId() == null || aplicacion.getValorAplicado() == null || aplicacion.getValorAplicado() <= 0)
                throw new RuntimeException("Cada factura a pagar debe tener un valor mayor a 0");

            Compra original = compraGateway.buscarCompraPorId(aplicacion.getCompraReferenciaId(), empresaId);
            if (original == null)
                throw new NoSuchElementException("Una de las facturas seleccionadas no existe");
            if (!TIPOS_FUENTE.contains(original.getTipoTransaccion()))
                throw new RuntimeException("Solo puedes pagar facturas de compra, documentos soporte o facturas electrónicas");
            if (!original.getProveedorId().equals(compra.getProveedorId()))
                throw new RuntimeException("Todas las facturas del recibo deben ser del mismo proveedor");

            double saldoActual = original.getSaldoPendiente() != null ? original.getSaldoPendiente() : 0;
            if (aplicacion.getValorAplicado() > saldoActual)
                throw new RuntimeException("El valor aplicado a " + original.getNumeroComprobante() +
                        " (" + aplicacion.getValorAplicado() + ") es mayor a su saldo pendiente (" + saldoActual + ")");

            original.setSaldoPendiente(Math.max(0, saldoActual - aplicacion.getValorAplicado()));
            compraGateway.guardarCompra(original);

            sumaAplicaciones += aplicacion.getValorAplicado();
        }

        // El total pagado del recibo es la suma de lo aplicado a cada factura.
        compra.setTotalPagar(sumaAplicaciones);
    }

    // ─── NOTA_DEBITO / AJUSTE_CARTERA: aplican sobre una sola compra ────────

    private void aplicarSobreDocumentoReferencia(Compra compra, String empresaId) {
        if (compra.getCompraReferenciaId() == null)
            throw new RuntimeException("Debes indicar la compra sobre la que aplica este documento");

        Compra original = compraGateway.buscarCompraPorId(compra.getCompraReferenciaId(), empresaId);
        if (original == null)
            throw new NoSuchElementException("La compra referenciada no existe");
        if (!TIPOS_FUENTE.contains(original.getTipoTransaccion()))
            throw new RuntimeException("Solo puedes aplicar este documento sobre una factura de compra, documento soporte o factura electrónica");

        double saldoActual = original.getSaldoPendiente() != null ? original.getSaldoPendiente() : 0;
        double valor = compra.getTotalPagar() != null ? compra.getTotalPagar() : 0;

        double nuevoSaldo = compra.getTipoTransaccion().equals("NOTA_DEBITO")
                ? saldoActual + valor                        // aumenta lo que se debe
                : Math.max(0, saldoActual + valor);           // ajuste: valor libre (puede ser negativo)

        original.setSaldoPendiente(nuevoSaldo);
        compraGateway.guardarCompra(original);

        compra.setProveedorId(original.getProveedorId());
        compra.setProveedorNombre(original.getProveedorNombre());
    }

    private void calcularTotales(Compra compra) {
        if (compra.getItems() == null || compra.getItems().isEmpty()) {
            throw new RuntimeException("La compra debe tener al menos un ítem");
        }

        double totalBruto = 0;
        for (CompraItem item : compra.getItems()) {
            double cantidad = item.getCantidad() != null ? item.getCantidad() : 1;
            double valorUnitario = item.getValorUnitario() != null ? item.getValorUnitario() : 0;
            double descuento = item.getDescuento() != null ? item.getDescuento() : 0;
            double valorTotal = (cantidad * valorUnitario) - descuento;
            item.setValorTotal(valorTotal);
            totalBruto += valorTotal;
        }

        double totalDescuentos = compra.getTotalDescuentos() != null ? compra.getTotalDescuentos() : 0;
        double subtotal = totalBruto - totalDescuentos;
        double totalIva = compra.getTotalIva() != null ? compra.getTotalIva() : 0;
        double totalRetencion = compra.getTotalRetencion() != null ? compra.getTotalRetencion() : 0;

        compra.setTotalBruto(totalBruto);
        compra.setSubtotal(subtotal);
        compra.setTotalPagar(subtotal + totalIva - totalRetencion);
    }

    private void validar(Compra compra) {
        if (compra.getTipoTransaccion() == null || !TIPOS_VALIDOS.contains(compra.getTipoTransaccion()))
            throw new RuntimeException("Tipo de transacción inválido");

        if (compra.getProveedorId() == null)
            throw new RuntimeException("El proveedor es obligatorio");

        if (TIPOS_REFERENCIA.contains(compra.getTipoTransaccion()) && !compra.getTipoTransaccion().equals("RECIBO_PAGO")) {
            if (compra.getTotalPagar() == null)
                throw new RuntimeException("El valor es obligatorio");
        }
    }
}
