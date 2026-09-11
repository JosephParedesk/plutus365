package com.pos_backend.facturacion.domain.usecase;

import com.pos_backend.facturacion.domain.model.*;
import com.pos_backend.facturacion.domain.model.gateway.*;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Emisión de notas crédito electrónicas.
 *
 * IMPORTANTE — estado actual de la integración DIAN:
 * igual que las facturas, la nota se genera, se firma y se guarda localmente,
 * pero la transmisión real a la DIAN depende de tener certificado digital,
 * habilitación y set de pruebas superado. Mientras DIAN_SIMULACION=true la
 * nota queda marcada como ACEPTADA sin haber salido del servidor: sirve para
 * probar el flujo completo, NO tiene validez fiscal.
 */
@RequiredArgsConstructor
public class NotaCreditoUseCase {

    private final NotaCreditoGateway notaCreditoGateway;
    private final FacturaGateway facturaGateway;
    private final ConfiguracionDianGateway configuracionDianGateway;
    private final StockNotaGateway stockNotaGateway;
    private final ContabilidadNotaGateway contabilidadNotaGateway;

    public List<NotaCredito> listar(String empresaId) {
        return notaCreditoGateway.listar(empresaId);
    }

    public NotaCredito buscarPorId(Long id, String empresaId) {
        NotaCredito n = notaCreditoGateway.buscarPorId(id, empresaId);
        if (n == null) throw new NoSuchElementException("Nota crédito no encontrada");
        return n;
    }

    public List<NotaCredito> listarPorFactura(Long facturaId, String empresaId) {
        return notaCreditoGateway.listarPorFactura(facturaId, empresaId);
    }

    public NotaCredito emitir(NotaCredito nota, String empresaId, String creadoPor) {
        // 1. La factura debe existir y estar aceptada por la DIAN.
        Factura factura = facturaGateway.buscarPorId(nota.getFacturaId(), empresaId);
        if (factura == null)
            throw new NoSuchElementException("La factura que intentas corregir no existe");
        if (!"ACEPTADA".equals(factura.getEstado()))
            throw new RuntimeException("Solo se puede emitir una nota crédito sobre una factura ACEPTADA por la DIAN. " +
                    "Esta factura está en estado " + factura.getEstado() + ".");
        if (factura.getCufe() == null || factura.getCufe().isBlank())
            throw new RuntimeException("La factura no tiene CUFE. La nota crédito debe referenciarlo obligatoriamente.");

        // 2. El concepto de corrección debe ser uno de los válidos del anexo técnico.
        ConceptoNotaCredito concepto = ConceptoNotaCredito.porCodigo(nota.getConceptoCodigo());

        // 3. Debe tener ítems y valores coherentes.
        if (nota.getItems() == null || nota.getItems().isEmpty())
            throw new RuntimeException("La nota crédito debe tener al menos un ítem");

        double subtotal = 0, iva = 0;
        for (NotaCredito.ItemNotaCredito item : nota.getItems()) {
            if (item.getCantidad() == null || item.getCantidad() <= 0)
                throw new RuntimeException("La cantidad de cada ítem debe ser mayor a 0");
            double base = item.getCantidad() * safe(item.getPrecioUnitario());
            double ivaItem = base * (safe(item.getPorcentajeIva()) / 100.0);
            item.setValorIva(redondear(ivaItem));
            item.setValorTotal(redondear(base + ivaItem));
            subtotal += base;
            iva += ivaItem;
        }
        nota.setSubtotal(redondear(subtotal));
        nota.setTotalIva(redondear(iva));
        nota.setTotal(redondear(subtotal + iva));

        // 4. No se puede acreditar más de lo facturado (sumando notas anteriores).
        double yaAcreditado = listarPorFactura(nota.getFacturaId(), empresaId).stream()
                .filter(n -> !"RECHAZADA".equals(n.getEstado()) && !"ERROR".equals(n.getEstado()))
                .mapToDouble(n -> safe(n.getTotal()))
                .sum();
        // El total de la factura no está en el modelo Factura, así que la validación
        // fuerte contra el valor facturado la hace el controlador con el dato de la venta.
        if (yaAcreditado > 0 && concepto.esAnulacion())
            throw new RuntimeException("Esta factura ya tiene notas crédito por " + yaAcreditado +
                    ". No puedes anularla completa; emite una nota por el saldo restante o revisa las notas anteriores.");

        nota.setEmpresaId(empresaId);
        nota.setNumeroFactura(factura.getNumeroFactura());
        nota.setCufeFactura(factura.getCufe());
        nota.setConceptoDescripcion(concepto.getDescripcion());
        nota.setAnulaTotal(concepto.esAnulacion());
        nota.setFechaEmision(LocalDateTime.now());
        nota.setCreadoPor(creadoPor);

        ConfiguracionDian config = configuracionDianGateway.buscarPorEmpresaId(empresaId);
        nota.setAmbiente(config != null && config.getAmbiente() != null ? config.getAmbiente() : "HABILITACION");
        nota.setNumeroNota(generarNumero(empresaId));
        nota.setCude(generarCude(nota));
        nota.setEstado("GENERADA");

        NotaCredito guardada = notaCreditoGateway.guardar(nota);

        // ── Reintegro de inventario ──
        // Solo aplica cuando realmente vuelve mercancía: devolución (1) o anulación (2).
        // Un descuento o ajuste de precio NO devuelve producto, así que no toca el stock.
        List<String> skusReintegrados = new ArrayList<>();
        boolean devuelveMercancia = concepto == ConceptoNotaCredito.DEVOLUCION_PARCIAL || concepto.esAnulacion();
        if (devuelveMercancia) {
            try {
                for (NotaCredito.ItemNotaCredito item : guardada.getItems()) {
                    if (item.getSku() == null || item.getSku().isBlank()) continue;
                    int cantidad = (int) Math.round(safe(item.getCantidad()));
                    if (cantidad <= 0) continue;
                    stockNotaGateway.reintegrar(item.getSku(), cantidad, empresaId);
                    skusReintegrados.add(item.getSku() + ":" + cantidad);
                }
            } catch (RuntimeException e) {
                guardada.setEstado("ERROR");
                guardada.setRespuestaDian("No se pudo reintegrar el inventario: " + e.getMessage());
                notaCreditoGateway.guardar(guardada);
                throw new RuntimeException("La nota crédito no se pudo aplicar al inventario: " + e.getMessage());
            }
        }

        // ── Asiento contable de reversión ──
        try {
            // VentaRemota no expone las formas de pago, así que no se puede saber
            // con certeza si el dinero salió de caja o de bancos. Se asume EFECTIVO
            // (el caso más común en un POS); si el reintegro real fue por transferencia,
            // el contador debe reclasificar el asiento a Bancos.
            contabilidadNotaGateway.generarAsiento(guardada, "EFECTIVO", empresaId);
        } catch (RuntimeException e) {
            // Compensación: se devuelve el stock que ya se había reintegrado.
            for (String s : skusReintegrados) {
                String[] partes = s.split(":");
                stockNotaGateway.revertirReintegro(partes[0], Integer.parseInt(partes[1]), empresaId);
            }
            guardada.setEstado("ERROR");
            guardada.setRespuestaDian("No se pudo contabilizar: " + e.getMessage());
            notaCreditoGateway.guardar(guardada);
            throw new RuntimeException("La nota crédito no se pudo contabilizar y fue revertida: " + e.getMessage());
        }

        return guardada;
    }


    private String generarNumero(String empresaId) {
        long n = notaCreditoGateway.contarPorEmpresa(empresaId) + 1;
        return "NC-" + String.format("%06d", n);
    }

    /**
     * CUDE provisional. El CUDE real se calcula con SHA-384 sobre la cadena
     * exacta que define el anexo técnico de la DIAN (incluye NIT, valores,
     * fecha, clave técnica, etc.) — se implementa junto con la transmisión real,
     * igual que se hizo con el CUFE de las facturas.
     */
    private String generarCude(NotaCredito nota) {
        try {
            String cadena = nota.getNumeroNota() + nota.getCufeFactura() + nota.getTotal()
                    + nota.getFechaEmision() + nota.getEmpresaId();
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-384");
            byte[] hash = md.digest(cadena.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("No se pudo generar el CUDE: " + e.getMessage());
        }
    }

    private double safe(Double v) { return v != null ? v : 0.0; }
    private double redondear(double v) { return Math.round(v * 100.0) / 100.0; }
}
