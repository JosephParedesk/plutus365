package com.pos_backend.venta.domain.usecase;

import com.pos_backend.venta.domain.model.EmpresaRemota;
import com.pos_backend.venta.domain.model.FormaPago;
import com.pos_backend.venta.domain.model.Venta;
import com.pos_backend.venta.domain.model.VentaItem;
import com.pos_backend.venta.domain.model.gateway.ContabilidadGateway;
import com.pos_backend.venta.domain.model.gateway.EmailGateway;
import com.pos_backend.venta.domain.model.gateway.EmpresaConsultaGateway;
import com.pos_backend.venta.domain.model.gateway.StockGateway;
import com.pos_backend.venta.domain.model.gateway.VentaGateway;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class VentaUseCase {

    private final VentaGateway ventaGateway;
    private final StockGateway stockGateway;
    private final EmailGateway emailGateway;
    private final ContabilidadGateway contabilidadGateway;
    private final EmpresaConsultaGateway empresaConsultaGateway;

    // Reserva el numeroVenta por empresa — ver comentario en registrarVenta.
    // ponytail: lock en memoria, no sirve si venta-service se escala a varias
    // instancias — en ese caso hace falta lock a nivel de BD o distribuido
    // (mismo trade-off que en FacturaUseCase de facturacion-service).
    private final ConcurrentHashMap<String, Object> locksPorEmpresa = new ConcurrentHashMap<>();

    public List<Venta> listarVentas(String empresaId) {
        return ventaGateway.listarVentas(empresaId);
    }

    public Venta buscarPorId(Long ventaId, String empresaId) {
        Venta venta = ventaGateway.buscarVentaPorId(ventaId, empresaId);
        if (venta == null)
            throw new NoSuchElementException("Venta no encontrada");
        return venta;
    }

    public List<Venta> buscarConFiltros(String empresaId, Long clienteId, LocalDate fechaInicio, LocalDate fechaFin) {
        return ventaGateway.buscarConFiltros(empresaId, clienteId, fechaInicio, fechaFin);
    }

    public Venta registrarVenta(Venta venta, String empresaId, String creadoPor) {
        validar(venta);
        calcularTotales(venta);

        // Descuenta stock ítem por ítem. Si alguno falla (sin stock o SKU
        // inexistente), repone lo ya descontado antes de abortar la venta.
        List<VentaItem> descontados = new ArrayList<>();
        try {
            for (VentaItem item : venta.getItems()) {
                stockGateway.descontarStock(item.getSku(), empresaId, item.getCantidad());
                descontados.add(item);
            }
        } catch (RuntimeException e) {
            compensar(descontados, empresaId);
            throw new RuntimeException("No se pudo confirmar la venta: " + e.getMessage());
        }

        venta.setEmpresaId(empresaId);
        venta.setCreadoPor(creadoPor);
        venta.setFecha(LocalDateTime.now());
        venta.setEstado("REGISTRADA");
        venta.setClienteNombre(
                venta.getClienteId() == null
                        ? (venta.getClienteNombre() != null ? venta.getClienteNombre() : "Consumidor final")
                        : venta.getClienteNombre());
        // Venta a crédito: nace debiendo lo que no se pagó de contado.
        // El saldo baja cuando el cliente abona con un recibo de caja.
        if (Boolean.TRUE.equals(venta.getTieneCreditoCliente())) {
            double pagado = venta.getFormasPago() == null ? 0
                    : venta.getFormasPago().stream().mapToDouble(fp -> fp.getValor() != null ? fp.getValor() : 0).sum();
            venta.setSaldoPendiente(Math.max(0, venta.getTotal() - pagado));
        } else {
            venta.setSaldoPendiente(0.0);
        }

        // Generar el número y guardar tienen que ser atómicos por empresa: generarSiguienteNumero
        // cuenta filas, así que dos ventas concurrentes sin este lock podrían leer el mismo
        // conteo y calcular el mismo numeroVenta. La restricción única (empresa_id, numero_venta)
        // en BD es el cerrojo final, pero acá se evita que el cajero vea siquiera ese error.
        Venta guardada;
        Object lock = locksPorEmpresa.computeIfAbsent(empresaId, k -> new Object());
        synchronized (lock) {
            venta.setNumeroVenta(ventaGateway.generarSiguienteNumero(empresaId));
            try {
                guardada = ventaGateway.guardarVenta(venta);
            } catch (RuntimeException e) {
                // La venta no se pudo persistir: repone el stock ya descontado.
                compensar(descontados, empresaId);
                throw new RuntimeException("No se pudo registrar la venta, el stock fue restituido: " + e.getMessage());
            }
        }

        // Contabilización obligatoria: si falla, la venta no queda "a medias" —
        // se revierte el stock y se marca con error para que quede claro que
        // necesita atención (no se borra, para no perder el rastro).
        try {
            contabilidadGateway.generarAsientoVenta(guardada, empresaId);
        } catch (RuntimeException e) {
            compensar(descontados, empresaId);
            guardada.setEstado("ERROR_CONTABILIZACION");
            ventaGateway.guardarVenta(guardada);
            throw new RuntimeException("La venta no se pudo contabilizar y fue revertida: " + e.getMessage());
        }

        return guardada;
    }

    public void anularVenta(Long ventaId, String empresaId) {
        Venta existente = ventaGateway.buscarVentaPorId(ventaId, empresaId);
        if (existente == null)
            throw new NoSuchElementException("Venta no encontrada");
        if ("ANULADA".equals(existente.getEstado()))
            throw new RuntimeException("La venta ya se encuentra anulada");

        // Repone el stock de cada ítem antes de marcar la venta como anulada.
        for (VentaItem item : existente.getItems()) {
            stockGateway.incrementarStock(item.getSku(), empresaId, item.getCantidad());
        }
        ventaGateway.anularVenta(ventaId, empresaId);
    }

    public void enviarComprobantePorCorreo(Long ventaId, String empresaId, String correoDestino) {
        if (correoDestino == null || correoDestino.isBlank())
            throw new RuntimeException("El cliente no tiene un correo registrado");

        Venta venta = buscarPorId(ventaId, empresaId);
        if (!"REGISTRADA".equals(venta.getEstado()))
            throw new RuntimeException("Solo se puede enviar el comprobante de una venta registrada");

        EmpresaRemota empresa = empresaConsultaGateway.buscarEmpresa(empresaId);
        String asunto = "Tu comprobante de compra " + venta.getNumeroVenta();
        emailGateway.enviarCorreo(correoDestino, asunto, construirCuerpoCorreo(venta, empresa));
    }

    // Para la ventana de "vista previa" en Configuración: arma el mismo HTML que se
    // manda de verdad, pero con una venta de ejemplo — nunca se desincroniza del
    // correo real porque es el mismo método el que arma ambos.
    public String generarVistaPreviaCorreo(String empresaId) {
        EmpresaRemota empresa = empresaConsultaGateway.buscarEmpresa(empresaId);
        if (empresa == null)
            throw new RuntimeException("Configura primero los datos de tu empresa para poder ver la vista previa");

        VentaItem item1 = new VentaItem();
        item1.setNombreProducto("Producto de ejemplo");
        item1.setCantidad(2);
        item1.setPrecioUnitario(19900.0);
        item1.setValorTotal(39800.0);
        VentaItem item2 = new VentaItem();
        item2.setNombreProducto("Otro producto de ejemplo");
        item2.setCantidad(1);
        item2.setPrecioUnitario(20000.0);
        item2.setValorTotal(20000.0);

        Venta ventaEjemplo = new Venta();
        ventaEjemplo.setNumeroVenta("VT-00001");
        ventaEjemplo.setClienteNombre("Cliente de ejemplo");
        ventaEjemplo.setItems(java.util.List.of(item1, item2));
        ventaEjemplo.setTotal(59800.0);

        return construirCuerpoCorreo(ventaEjemplo, empresa);
    }

    private String construirCuerpoCorreo(Venta venta, EmpresaRemota empresa) {
        StringBuilder filas = new StringBuilder();
        for (VentaItem item : venta.getItems()) {
            filas.append("<tr>")
                    .append("<td style='padding:6px 4px'>").append(item.getNombreProducto()).append("</td>")
                    .append("<td style='padding:6px 4px;text-align:center'>").append(item.getCantidad()).append("</td>")
                    .append("<td style='padding:6px 4px;text-align:right'>").append(formatoMoneda(item.getPrecioUnitario())).append("</td>")
                    .append("<td style='padding:6px 4px;text-align:right'>").append(formatoMoneda(item.getValorTotal())).append("</td>")
                    .append("</tr>");
        }

        String cliente = venta.getClienteNombre() != null ? venta.getClienteNombre() : "Consumidor final";
        String color = empresa != null && empresa.getColorPrincipal() != null ? empresa.getColorPrincipal() : "#4E6F3A";
        String nombreEmpresa = nombreEmpresa(empresa);
        String logoHtml = empresa != null && empresa.getLogoUrl() != null && !empresa.getLogoUrl().isBlank()
                ? "<img src=\"" + empresa.getLogoUrl() + "\" alt=\"\" style=\"max-height:48px;max-width:220px;margin-bottom:8px\"><br>"
                : "";

        return "<div style=\"font-family:Arial,sans-serif;max-width:480px;margin:auto;color:#333\">"
                + logoHtml
                + (nombreEmpresa.isBlank() ? "" : "<div style=\"font-size:13px;font-weight:600;color:" + color + "\">" + nombreEmpresa + "</div>")
                + "<h2 style=\"color:" + color + ";margin-bottom:4px\">Comprobante de compra " + venta.getNumeroVenta() + "</h2>"
                + "<p style=\"color:#888;font-size:12px;line-height:1.4\">"
                + "Este comprobante es un resumen de tu compra y no constituye una factura electrónica "
                + "de venta ante la DIAN.</p>"
                + "<p style=\"margin:12px 0 4px\"><b>Cliente:</b> " + cliente + "</p>"
                + "<table style=\"width:100%;border-collapse:collapse;font-size:13px\">"
                + "<thead><tr style=\"background:#f5f5f5;text-align:left\">"
                + "<th style=\"padding:6px 4px\">Producto</th><th style=\"padding:6px 4px\">Cant.</th>"
                + "<th style=\"padding:6px 4px\">Precio</th><th style=\"padding:6px 4px\">Subtotal</th>"
                + "</tr></thead><tbody>" + filas + "</tbody></table>"
                + "<h3 style=\"text-align:right;color:" + color + ";margin-top:14px\">Total: " + formatoMoneda(venta.getTotal()) + "</h3>"
                + "<p style=\"color:#aaa;font-size:11px;margin-top:24px\">Gracias por tu compra.</p>"
                + "</div>";
    }

    private String nombreEmpresa(EmpresaRemota empresa) {
        if (empresa == null) return "";
        if (empresa.getNombreComercial() != null && !empresa.getNombreComercial().isBlank()) return empresa.getNombreComercial();
        if ("JURIDICA".equals(empresa.getTipoPersona()) && empresa.getRazonSocial() != null) return empresa.getRazonSocial();
        return ((empresa.getNombres() != null ? empresa.getNombres() : "") + " " + (empresa.getApellidos() != null ? empresa.getApellidos() : "")).trim();
    }

    private String formatoMoneda(Double valor) {
        if (valor == null) valor = 0.0;
        return String.format("$%,.0f", valor);
    }

    private void compensar(List<VentaItem> descontados, String empresaId) {
        for (VentaItem item : descontados) {
            try {
                stockGateway.incrementarStock(item.getSku(), empresaId, item.getCantidad());
            } catch (RuntimeException ignored) {
                // Si la reposición falla, queda para conciliación manual;
                // no debe ocultar el error original de la venta.
            }
        }
    }

    // Las 4 tarifas de IVA vigentes en Colombia (19% general, 5% reducida, exento y
    // excluido a tarifa 0 — se diferencian solo para el derecho a devolución del
    // responsable, no para el cálculo). Espejo del mismo mapa en inventario-service
    // (Producto.tipoIva) y facturacion-service (UblXmlBuilder) — no hay librería
    // compartida entre microservicios en este proyecto, ver [[importar-inventario-excel]].
    private static final java.util.Map<String, Double> TASA_IVA = java.util.Map.of(
            "GENERAL_19", 0.19,
            "REDUCIDO_5", 0.05,
            "EXENTO", 0.0,
            "EXCLUIDO", 0.0
    );

    private void calcularTotales(Venta venta) {
        double subtotal = 0;
        double totalIva = 0;
        for (VentaItem item : venta.getItems()) {
            double cantidad = item.getCantidad() != null ? item.getCantidad() : 0;
            double precioUnitario = item.getPrecioUnitario() != null ? item.getPrecioUnitario() : 0;
            double descuento = item.getDescuento() != null ? item.getDescuento() : 0;
            // precioUnitario es el precio de venta al público, IVA INCLUIDO (así se
            // captura en Producto.precioVenta) — se desglosa hacia adentro en vez de
            // sumar el IVA encima, para que lo que se cobra sea el precio que el
            // negocio puso en la vitrina.
            double valorConIva = (cantidad * precioUnitario) - descuento;

            // El IVA se deriva SIEMPRE del tipoIva del ítem — nunca se confía en un
            // valorIva que venga ya calculado del frontend, para no arrastrar un
            // número desactualizado o manipulado.
            String tipoIva = item.getTipoIva() != null ? item.getTipoIva().toUpperCase() : "GENERAL_19";
            double tasa = TASA_IVA.getOrDefault(tipoIva, 0.19);
            double valorTotal = Math.round(valorConIva / (1 + tasa) * 100.0) / 100.0;
            double valorIva = Math.round((valorConIva - valorTotal) * 100.0) / 100.0;
            item.setValorTotal(valorTotal);
            item.setTipoIva(tipoIva);
            item.setValorIva(valorIva);
            subtotal += valorTotal;
            totalIva += valorIva;
        }

        double descuentoTotal = venta.getDescuentoTotal() != null ? venta.getDescuentoTotal() : 0;

        venta.setSubtotal(subtotal);
        venta.setTotalIva(totalIva);
        venta.setTotal(subtotal - descuentoTotal + totalIva);
    }

    private void validar(Venta venta) {
        if (venta.getItems() == null || venta.getItems().isEmpty())
            throw new RuntimeException("La venta debe tener al menos un ítem");

        for (VentaItem item : venta.getItems()) {
            if (item.getSku() == null || item.getSku().isBlank())
                throw new RuntimeException("Cada ítem debe tener un SKU");
            if (item.getCantidad() == null || item.getCantidad() <= 0)
                throw new RuntimeException("La cantidad del ítem " + item.getSku() + " debe ser mayor a 0");
            if (item.getPrecioUnitario() == null || item.getPrecioUnitario() < 0)
                throw new RuntimeException("El precio unitario del ítem " + item.getSku() + " es inválido");
        }

        if (venta.getFormasPago() == null || venta.getFormasPago().isEmpty())
            throw new RuntimeException("La venta debe tener al menos una forma de pago");

        double totalFormasPago = venta.getFormasPago().stream()
                .mapToDouble(FormaPago::getValor)
                .sum();
        if (totalFormasPago <= 0)
            throw new RuntimeException("El valor de las formas de pago debe ser mayor a 0");
    }
}
