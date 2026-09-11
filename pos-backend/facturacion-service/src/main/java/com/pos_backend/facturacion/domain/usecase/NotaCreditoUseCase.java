package com.pos_backend.facturacion.domain.usecase;

import com.pos_backend.facturacion.domain.model.*;
import com.pos_backend.facturacion.domain.model.gateway.*;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Emisión de notas crédito electrónicas, transmitidas a través de Factus.
 */
@RequiredArgsConstructor
public class NotaCreditoUseCase {

    private final NotaCreditoGateway notaCreditoGateway;
    private final FacturaGateway facturaGateway;
    private final ConfiguracionDianGateway configuracionDianGateway;
    private final VentaConsultaGateway ventaConsultaGateway;
    private final ClienteConsultaGateway clienteConsultaGateway;
    private final EmpresaConsultaGateway empresaConsultaGateway;
    private final FacturaElectronicaGateway facturaElectronicaGateway;
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

    /** Borra de Factus una nota crédito que quedó pendiente/rechazada, para poder reintentar. */
    public void eliminarNoValidada(Long notaCreditoId, String empresaId) {
        NotaCredito nota = notaCreditoGateway.buscarPorId(notaCreditoId, empresaId);
        if (nota == null)
            throw new NoSuchElementException("Nota crédito no encontrada");
        if ("ACEPTADA".equals(nota.getEstado()))
            throw new RuntimeException("No se puede eliminar una nota crédito ya aceptada por la DIAN");
        if (nota.getReferenceCode() == null || nota.getReferenceCode().isBlank())
            throw new RuntimeException("Esta nota no tiene código de referencia guardado — es de antes de este cambio, elimínala manualmente si hace falta");

        ConfiguracionDian config = configuracionDianGateway.buscarPorEmpresaId(empresaId);
        if (config == null)
            throw new RuntimeException("Configura primero tus credenciales de Factus en Configuración");

        facturaElectronicaGateway.eliminarNoValidada(config, "NOTA_CREDITO", nota.getReferenceCode());
        notaCreditoGateway.eliminar(notaCreditoId, empresaId);
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
        nota.setEstado("ERROR");

        ConfiguracionDian config = configuracionDianGateway.buscarPorEmpresaId(empresaId);
        if (config == null || !Boolean.TRUE.equals(config.getActivo()))
            throw new RuntimeException("Configura primero tus credenciales de Factus en Configuración");

        VentaRemota venta = ventaConsultaGateway.buscarVenta(factura.getVentaId(), empresaId);
        ClienteRemoto cliente = venta != null && venta.getClienteId() != null
                ? clienteConsultaGateway.buscarCliente(venta.getClienteId(), empresaId)
                : ClienteRemoto.consumidorFinalGenerico();
        EmpresaRemota empresa = empresaConsultaGateway.buscarEmpresa(empresaId);

        String referenceCode = "NC-" + factura.getNumeroFactura() + "-" + System.currentTimeMillis();
        nota.setReferenceCode(referenceCode);

        try {
            FacturaElectronicaGateway.ResultadoEmision resultado = facturaElectronicaGateway.emitirNotaCredito(
                    config, cliente, empresa, nota, referenceCode);

            nota.setNumeroNota(resultado.numeroDocumento());
            nota.setCude(resultado.cufeOCude());
            nota.setQrUrl(resultado.qrUrl());
            nota.setUrlDocumento(resultado.urlDocumento());
            nota.setAmbiente(resultado.ambiente());
            nota.setRespuestaDian(resultado.mensaje());
            nota.setEstado(resultado.aceptada() ? "GENERADA" : "RECHAZADA");
        } catch (RuntimeException e) {
            nota.setRespuestaDian(e.getMessage());
            notaCreditoGateway.guardar(nota);
            throw new RuntimeException("Factus rechazó la nota crédito: " + e.getMessage());
        }

        if (!"GENERADA".equals(nota.getEstado())) {
            notaCreditoGateway.guardar(nota);
            throw new RuntimeException("Factus no validó la nota crédito: " + nota.getRespuestaDian());
        }

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

    private double safe(Double v) { return v != null ? v : 0.0; }
    private double redondear(double v) { return Math.round(v * 100.0) / 100.0; }
}
