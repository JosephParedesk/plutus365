package com.pos_backend.facturacion.domain.usecase;

import com.pos_backend.facturacion.domain.model.*;
import com.pos_backend.facturacion.domain.model.gateway.*;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Emisión de documentos soporte electrónicos (DIAN), transmitidos a través de Factus.
 * Obligatorio desde la Resolución 000488/2022 cuando la empresa compra a un proveedor
 * que no está obligado a facturar electrónicamente.
 */
@RequiredArgsConstructor
public class DocumentoSoporteUseCase {

    // Mismo patrón que extraerPorcentaje() en NuevaCompraPage.tsx — los impuestos de
    // compra son texto libre de un dropdown fijo ("IVA 19%", "Retefuente 3,5% Compras").
    private static final Pattern PORCENTAJE = Pattern.compile("([\\d,.]+)\\s*%");

    private final DocumentoSoporteGateway documentoSoporteGateway;
    private final CompraConsultaGateway compraConsultaGateway;
    private final ProveedorConsultaGateway proveedorConsultaGateway;
    private final EmpresaConsultaGateway empresaConsultaGateway;
    private final ConfiguracionDianGateway configuracionDianGateway;
    private final FacturaElectronicaGateway facturaElectronicaGateway;

    public List<DocumentoSoporte> listar(String empresaId) {
        return documentoSoporteGateway.listar(empresaId);
    }

    public DocumentoSoporte buscarPorId(Long id, String empresaId) {
        DocumentoSoporte d = documentoSoporteGateway.buscarPorId(id, empresaId);
        if (d == null) throw new NoSuchElementException("Documento soporte no encontrado");
        return d;
    }

    /** Borra de Factus un documento soporte que quedó pendiente/rechazado, para poder reintentar. */
    public void eliminarNoValidada(Long documentoSoporteId, String empresaId) {
        DocumentoSoporte documento = documentoSoporteGateway.buscarPorId(documentoSoporteId, empresaId);
        if (documento == null)
            throw new NoSuchElementException("Documento soporte no encontrado");
        if ("ACEPTADA".equals(documento.getEstado()))
            throw new RuntimeException("No se puede eliminar un documento soporte ya aceptado por la DIAN");
        if (documento.getReferenceCode() == null || documento.getReferenceCode().isBlank())
            throw new RuntimeException("Este documento no tiene código de referencia guardado — es de antes de este cambio, elimínalo manualmente si hace falta");

        ConfiguracionDian config = configuracionDianGateway.buscarPorEmpresaId(empresaId);
        if (config == null)
            throw new RuntimeException("Configura primero tus credenciales de Factus en Configuración");

        facturaElectronicaGateway.eliminarNoValidada(config, "DOCUMENTO_SOPORTE", documento.getReferenceCode());
        documentoSoporteGateway.eliminar(documentoSoporteId, empresaId);
    }

    public DocumentoSoporte generar(Long compraId, String empresaId) {
        // 1. Ya existe uno aceptado para esta compra?
        DocumentoSoporte existente = documentoSoporteGateway.buscarPorCompraId(compraId, empresaId);
        if (existente != null && "ACEPTADA".equals(existente.getEstado()))
            throw new RuntimeException("Esta compra ya tiene un documento soporte aceptado: " + existente.getNumeroDocumento());

        // 2. Configuración de Factus completa?
        ConfiguracionDian config = configuracionDianGateway.buscarPorEmpresaId(empresaId);
        if (config == null || !Boolean.TRUE.equals(config.getActivo()))
            throw new RuntimeException("Configura primero tus credenciales de Factus en Configuración");

        // 3. La compra debe existir, ser del tipo correcto y estar registrada.
        CompraRemota compra = compraConsultaGateway.buscarCompra(compraId, empresaId);
        if (compra == null)
            throw new NoSuchElementException("Compra no encontrada");
        if (!"DOCUMENTO_SOPORTE".equals(compra.getTipoTransaccion()))
            throw new RuntimeException("Solo se puede generar documento soporte sobre una compra de tipo Documento Soporte");
        if (!"REGISTRADA".equals(compra.getEstado()))
            throw new RuntimeException("Solo se puede generar el documento sobre una compra REGISTRADA (estado actual: " + compra.getEstado() + ")");
        if (compra.getItems() == null || compra.getItems().isEmpty())
            throw new RuntimeException("La compra no tiene ítems para el documento soporte");

        // 4. El proveedor debe existir y tener NIT — Factus exige que el proveedor de
        // un documento soporte se identifique con NIT (ver tabla de tipos de documento
        // de identidad para documentos soporte, no incluye cédula de ciudadanía).
        ProveedorRemota proveedor = proveedorConsultaGateway.buscarProveedor(compra.getProveedorId(), empresaId);
        if (proveedor == null)
            throw new NoSuchElementException("Proveedor de la compra no encontrado");
        if (proveedor.getNit() == null || proveedor.getNit().isBlank())
            throw new RuntimeException("El proveedor debe tener NIT para generar el documento soporte — Factus no acepta cédula de ciudadanía para este documento");

        EmpresaRemota empresa = empresaConsultaGateway.buscarEmpresa(empresaId);
        if (empresa == null)
            throw new RuntimeException("Configura primero los datos generales de tu empresa antes de generar el documento");

        DocumentoSoporte documento = new DocumentoSoporte();
        documento.setEmpresaId(empresaId);
        documento.setCompraId(compraId);
        documento.setNumeroComprobante(compra.getNumeroComprobante());
        documento.setProveedorId(proveedor.getProveedorId());
        documento.setProveedorNombre(proveedor.getNombre());
        documento.setFechaEmision(LocalDateTime.now());
        documento.setEstado("ERROR");

        double subtotal = 0, iva = 0, retencion = 0;
        var items = compra.getItems().stream().map(this::mapearItem).toList();
        for (DocumentoSoporte.ItemDocumentoSoporte item : items) {
            subtotal += item.getPrecioUnitario() * item.getCantidad();
            iva += item.getValorIva();
            retencion += item.getValorRetencion();
        }
        documento.setItems(items);
        documento.setSubtotal(redondear(subtotal));
        documento.setTotalIva(redondear(iva));
        documento.setTotalRetencion(redondear(retencion));
        documento.setTotal(redondear(subtotal + iva));

        String referenceCode = "DS-" + compra.getNumeroComprobante();
        documento.setReferenceCode(referenceCode);

        try {
            documento.setFechaEnvio(LocalDateTime.now());
            FacturaElectronicaGateway.ResultadoEmision resultado = facturaElectronicaGateway.emitirDocumentoSoporte(
                    config, proveedor, empresa, documento, referenceCode);

            documento.setNumeroDocumento(resultado.numeroDocumento());
            documento.setCude(resultado.cufeOCude());
            documento.setQrUrl(resultado.qrUrl());
            documento.setUrlDocumento(resultado.urlDocumento());
            documento.setAmbiente(resultado.ambiente());
            documento.setRespuestaDian(resultado.mensaje());
            documento.setEstado(resultado.aceptada() ? "ACEPTADA" : "RECHAZADA");
        } catch (RuntimeException e) {
            documento.setRespuestaDian(e.getMessage());
            documentoSoporteGateway.guardar(documento);
            throw new RuntimeException("Factus rechazó el documento soporte: " + e.getMessage());
        }

        if (!"ACEPTADA".equals(documento.getEstado())) {
            documentoSoporteGateway.guardar(documento);
            throw new RuntimeException("Factus no validó el documento soporte: " + documento.getRespuestaDian());
        }

        return documentoSoporteGateway.guardar(documento);
    }

    // valorUnitario de compra ya viene SIN IVA (así lo captura NuevaCompraPage — el IVA
    // se calcula aparte con extraerPorcentaje sobre la base), coincide con lo que Factus
    // espera en items.price. El descuento se aplica sobre la base antes de IVA/retención.
    private DocumentoSoporte.ItemDocumentoSoporte mapearItem(CompraRemota.ItemRemoto item) {
        double cantidad = safe(item.getCantidad());
        double baseSinDescuento = cantidad * safe(item.getValorUnitario());
        double descuento = safe(item.getDescuento());
        double base = baseSinDescuento - descuento;
        double tasaIva = porcentaje(item.getImpuestoCargo());
        double tasaRetencion = porcentaje(item.getImpuestoRetencion());

        DocumentoSoporte.ItemDocumentoSoporte i = new DocumentoSoporte.ItemDocumentoSoporte();
        i.setSku(item.getProductoSku());
        i.setDescripcion(item.getDescripcion());
        i.setCantidad(cantidad);
        i.setPrecioUnitario(redondear(cantidad > 0 ? base / cantidad : base));
        i.setDescuento(descuento);
        i.setPorcentajeIva(redondear(tasaIva * 100));
        i.setValorIva(redondear(base * tasaIva));
        i.setPorcentajeRetencion(redondear(tasaRetencion * 100));
        i.setValorRetencion(redondear(base * tasaRetencion));
        i.setValorTotal(redondear(base + (base * tasaIva)));
        return i;
    }

    // package-private y static para poder probarla directo desde el test.
    static double porcentaje(String texto) {
        if (texto == null) return 0;
        Matcher m = PORCENTAJE.matcher(texto);
        return m.find() ? Double.parseDouble(m.group(1).replace(',', '.')) / 100.0 : 0;
    }

    private double safe(Double v) { return v != null ? v : 0.0; }
    private double redondear(double v) { return Math.round(v * 100.0) / 100.0; }
}
