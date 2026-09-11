package com.pos_backend.facturacion.domain.usecase;

import com.pos_backend.facturacion.domain.model.*;
import com.pos_backend.facturacion.domain.model.gateway.*;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
public class FacturaUseCase {

    private final FacturaGateway facturaGateway;
    private final ConfiguracionDianGateway configuracionDianGateway;
    private final CertificadoGateway certificadoGateway;
    private final VentaConsultaGateway ventaConsultaGateway;
    private final ClienteConsultaGateway clienteConsultaGateway;
    private final EmpresaConsultaGateway empresaConsultaGateway;
    private final XmlFacturaGateway xmlFacturaGateway;
    private final DianGateway dianGateway;
    private final QrCodeGateway qrCodeGateway;
    private final EmailGateway emailGateway;

    public List<Factura> listar(String empresaId) {
        return facturaGateway.listar(empresaId);
    }

    public Factura buscarPorId(Long facturaId, String empresaId) {
        Factura factura = facturaGateway.buscarPorId(facturaId, empresaId);
        if (factura == null) throw new NoSuchElementException("Factura no encontrada");
        return factura;
    }

    public FacturaDetalle obtenerDetalle(Long facturaId, String empresaId) {
        Factura factura = buscarPorId(facturaId, empresaId);

        VentaRemota venta = ventaConsultaGateway.buscarVenta(factura.getVentaId(), empresaId);
        ClienteRemoto cliente = venta != null && venta.getClienteId() != null
                ? clienteConsultaGateway.buscarCliente(venta.getClienteId(), empresaId)
                : consumidorFinalGenerico();
        EmpresaRemota empresa = empresaConsultaGateway.buscarEmpresa(empresaId);

        String qrBase64 = null;
        try {
            qrBase64 = qrCodeGateway.generarBase64(factura.getQrUrl());
        } catch (RuntimeException ignored) {
            // Si falla el QR no debe tumbar toda la vista del documento.
        }

        return new FacturaDetalle(factura, venta, cliente, empresa, qrBase64);
    }

    public void enviarPorCorreo(Long facturaId, String empresaId, String correoDestino) {
        if (correoDestino == null || correoDestino.isBlank())
            throw new RuntimeException("El cliente no tiene un correo registrado");

        FacturaDetalle detalle = obtenerDetalle(facturaId, empresaId);
        if (!"ACEPTADA".equals(detalle.getFactura().getEstado()))
            throw new RuntimeException("Solo se puede enviar por correo una factura ACEPTADA por la DIAN");

        String asunto = "Factura electrónica " + detalle.getFactura().getNumeroFactura();
        emailGateway.enviarCorreo(correoDestino, asunto, construirCorreoFactura(detalle));
    }

    // Para la ventana de "vista previa" en Configuración: arma el mismo HTML que se
    // manda de verdad, pero con una factura de ejemplo — así la vista previa nunca
    // se desincroniza del correo real (es el mismo método el que arma ambos).
    public String generarVistaPreviaCorreo(String empresaId) {
        EmpresaRemota empresa = empresaConsultaGateway.buscarEmpresa(empresaId);
        if (empresa == null)
            throw new RuntimeException("Configura primero los datos de tu empresa para poder ver la vista previa");

        Factura facturaEjemplo = new Factura();
        facturaEjemplo.setNumeroFactura("SETP000001");
        facturaEjemplo.setCufe("cufe-de-ejemplo-1234567890abcdef1234567890abcdef");
        facturaEjemplo.setQrUrl("https://catalogo-vpfe-hab.dian.gov.co/document/searchqr?documentkey=ejemplo");

        VentaRemota.ItemRemoto item1 = new VentaRemota.ItemRemoto();
        item1.setNombreProducto("Producto de ejemplo");
        item1.setCantidad(2);
        item1.setValorTotal(39800.0);
        VentaRemota.ItemRemoto item2 = new VentaRemota.ItemRemoto();
        item2.setNombreProducto("Otro producto de ejemplo");
        item2.setCantidad(1);
        item2.setValorTotal(20000.0);
        VentaRemota ventaEjemplo = new VentaRemota();
        ventaEjemplo.setTotal(59800.0);
        ventaEjemplo.setItems(java.util.List.of(item1, item2));

        ClienteRemoto clienteEjemplo = new ClienteRemoto();
        clienteEjemplo.setTipoPersona("NATURAL");
        clienteEjemplo.setNombres("Cliente");
        clienteEjemplo.setApellidos("de ejemplo");

        return construirCorreoFactura(new FacturaDetalle(facturaEjemplo, ventaEjemplo, clienteEjemplo, empresa, null));
    }

    private String construirCorreoFactura(FacturaDetalle detalle) {
        VentaRemota venta = detalle.getVenta();
        StringBuilder filas = new StringBuilder();
        if (venta != null && venta.getItems() != null) {
            for (VentaRemota.ItemRemoto item : venta.getItems()) {
                filas.append("<tr>")
                        .append("<td style='padding:6px 4px'>").append(item.getNombreProducto()).append("</td>")
                        .append("<td style='padding:6px 4px;text-align:center'>").append(item.getCantidad()).append("</td>")
                        .append("<td style='padding:6px 4px;text-align:right'>").append(moneda(item.getValorTotal())).append("</td>")
                        .append("</tr>");
            }
        }

        String nombreCliente = detalle.getCliente() != null
                ? nombreCompleto(detalle.getCliente().getTipoPersona(), detalle.getCliente().getRazonSocial(),
                    detalle.getCliente().getNombres(), detalle.getCliente().getApellidos())
                : "Cliente";
        String nombreEmpresa = detalle.getEmpresa() != null
                ? nombreCompleto(detalle.getEmpresa().getTipoPersona(), detalle.getEmpresa().getRazonSocial(),
                    detalle.getEmpresa().getNombres(), detalle.getEmpresa().getApellidos())
                : "";
        String color = detalle.getEmpresa() != null && detalle.getEmpresa().getColorPrincipal() != null
                ? detalle.getEmpresa().getColorPrincipal() : "#4E6F3A";
        String logoUrl = detalle.getEmpresa() != null ? detalle.getEmpresa().getLogoUrl() : null;
        String logoHtml = logoUrl != null && !logoUrl.isBlank()
                ? "<img src=\"" + logoUrl + "\" alt=\"\" style=\"max-height:48px;max-width:220px;margin-bottom:8px\"><br>"
                : "";

        return "<div style=\"font-family:Arial,sans-serif;max-width:500px;margin:auto;color:#333\">"
                + logoHtml
                + "<h2 style=\"color:" + color + ";margin-bottom:2px\">" + nombreEmpresa + "</h2>"
                + "<p style=\"margin:0 0 16px;font-size:13px;color:#888\">Factura electrónica de venta</p>"
                + "<p><b>Factura:</b> " + detalle.getFactura().getNumeroFactura() + "</p>"
                + "<p><b>CUFE:</b> <span style='font-size:11px;word-break:break-all'>" + detalle.getFactura().getCufe() + "</span></p>"
                + "<p><b>Cliente:</b> " + nombreCliente + "</p>"
                + "<table style=\"width:100%;border-collapse:collapse;font-size:13px;margin-top:12px\">"
                + "<thead><tr style=\"background:#EAFBF1;text-align:left\">"
                + "<th style=\"padding:6px 4px\">Producto</th><th style=\"padding:6px 4px\">Cant.</th><th style=\"padding:6px 4px\">Total</th>"
                + "</tr></thead><tbody>" + filas + "</tbody></table>"
                + "<h3 style=\"text-align:right;color:" + color + ";margin-top:14px\">Total: " + moneda(venta != null ? venta.getTotal() : 0.0) + "</h3>"
                + "<p style=\"color:#aaa;font-size:11px;margin-top:24px\">"
                + "Esta es tu factura electrónica de venta, validada por la DIAN. Puedes verificarla con el CUFE en "
                + "<a href=\"" + detalle.getFactura().getQrUrl() + "\">catalogo-vpfe.dian.gov.co</a>.</p>"
                + "</div>";
    }

    private String moneda(Double valor) {
        return String.format("$%,.0f", valor != null ? valor : 0.0);
    }

    private String nombreCompleto(String tipoPersona, String razonSocial, String nombres, String apellidos) {
        if ("JURIDICA".equals(tipoPersona)) return razonSocial != null ? razonSocial : "";
        return ((nombres != null ? nombres : "") + " " + (apellidos != null ? apellidos : "")).trim();
    }

    public Factura generarFactura(Long ventaId, String empresaId) {
        // 1. Ya existe una factura para esta venta?
        Factura existente = facturaGateway.buscarPorVentaId(ventaId, empresaId);
        if (existente != null && "ACEPTADA".equals(existente.getEstado()))
            throw new RuntimeException("Esta venta ya tiene una factura electrónica aceptada: " + existente.getNumeroFactura());

        // 2. Configuración DIAN completa?
        ConfiguracionDian config = configuracionDianGateway.buscarPorEmpresaId(empresaId);
        if (config == null)
            throw new RuntimeException("Configura primero la facturación electrónica de tu empresa (resolución DIAN)");
        if (!certificadoGateway.existeCertificado(empresaId))
            throw new RuntimeException("Sube primero el certificado digital de tu empresa en Configuración");
        validarResolucionVigente(config);

        // 3. Datos de la venta
        VentaRemota venta = ventaConsultaGateway.buscarVenta(ventaId, empresaId);
        if (venta == null)
            throw new NoSuchElementException("Venta no encontrada");
        if (!"REGISTRADA".equals(venta.getEstado()))
            throw new RuntimeException("Solo se puede facturar una venta REGISTRADA (estado actual: " + venta.getEstado() + ")");
        if (venta.getItems() == null || venta.getItems().isEmpty())
            throw new RuntimeException("La venta no tiene ítems para facturar");

        // 4. Emisor
        EmpresaRemota empresa = empresaConsultaGateway.buscarEmpresa(empresaId);
        if (empresa == null)
            throw new RuntimeException("Configura primero los datos generales de tu empresa antes de facturar");

        // 5. Receptor — si no hay cliente identificado, se usa un consumidor final genérico.
        //    NOTA: revisar con tu contador el código DIAN vigente para consumidor final;
        //    esto es una aproximación razonable, no una certeza normativa.
        ClienteRemoto cliente = venta.getClienteId() != null
                ? clienteConsultaGateway.buscarCliente(venta.getClienteId(), empresaId)
                : consumidorFinalGenerico();
        if (cliente == null)
            throw new NoSuchElementException("Cliente de la venta no encontrado");

        // 6. Certificado
        CertificadoGateway.MaterialFirma material = certificadoGateway.cargarMaterialFirma(empresaId);

        // 7. Numeración — se reserva ANTES de tocar la DIAN, no después. Dos facturas
        // concurrentes de la misma empresa podrían leer el mismo consecutivoActual y
        // terminar enviando dos documentos distintos a la DIAN con el mismo número; eso
        // es peor que dejar un hueco en la numeración, que sí es legal. La reserva es un
        // UPDATE atómico en BD (compare-and-swap), no un lock en memoria: funciona aunque
        // el servicio corra en varias instancias. Ver ConfiguracionDianDataGatewayImpl.
        long consecutivo = configuracionDianGateway.reservarSiguienteConsecutivo(empresaId);
        String numeroFactura = config.getPrefijo() + consecutivo;

        Factura factura = new Factura();
        factura.setEmpresaId(empresaId);
        factura.setVentaId(ventaId);
        factura.setNumeroFactura(numeroFactura);
        factura.setAmbiente(config.getAmbiente());
        factura.setFechaEmision(LocalDateTime.now());
        factura.setEstado("ERROR");

        try {
            // 8. Construir XML UBL + CUFE + firma XAdES
            XmlFacturaGateway.ResultadoXml resultado = xmlFacturaGateway.construirYFirmar(
                    empresa, cliente, venta, config, numeroFactura, material);

            factura.setCufe(resultado.cufe());
            factura.setQrUrl(resultado.qrUrl());
            factura.setXmlFirmado(resultado.xmlFirmado());
            factura.setEstado("GENERADA");

            // 9. Enviar a la DIAN
            String zipBase64 = comprimirBase64(resultado.xmlFirmado(), "FV" + numeroFactura + ".xml");
            factura.setFechaEnvio(LocalDateTime.now());

            DianGateway.RespuestaDian respuesta = dianGateway.enviarFactura(
                    zipBase64, "FV" + numeroFactura + ".zip", material,
                    config.getAmbiente(), config.getTestSetId());

            factura.setRespuestaDian(respuesta.mensaje());
            factura.setEstado(respuesta.exitosa() ? "ACEPTADA" : "RECHAZADA");

        } catch (RuntimeException e) {
            factura.setRespuestaDian(e.getMessage());
            factura.setEstado("ERROR");
            facturaGateway.guardar(factura);
            throw e;
        }

        return facturaGateway.guardar(factura);
    }

    private void validarResolucionVigente(ConfiguracionDian config) {
        try {
            LocalDate fin = LocalDate.parse(config.getResolucionFechaFin());
            if (LocalDate.now().isAfter(fin))
                throw new RuntimeException("La resolución de numeración de la DIAN venció el " + fin + ". Solicita una nueva.");
        } catch (java.time.format.DateTimeParseException e) {
            throw new RuntimeException("La fecha de vencimiento de la resolución no es válida");
        }
    }

    private ClienteRemoto consumidorFinalGenerico() {
        ClienteRemoto c = new ClienteRemoto();
        c.setTipoPersona("NATURAL");
        c.setTipoDocumento("CC");
        c.setNumeroDocumento("222222222222");
        c.setNombres("Consumidor");
        c.setApellidos("Final");
        c.setRegimenFiscal("NO_RESPONSABLE_IVA");
        c.setCorreo("");
        c.setTelefono("");
        c.setPais("Colombia");
        return c;
    }

    private String comprimirBase64(String xml, String nombreArchivo) {
        try {
            var baos = new java.io.ByteArrayOutputStream();
            try (var zos = new java.util.zip.ZipOutputStream(baos)) {
                zos.putNextEntry(new java.util.zip.ZipEntry(nombreArchivo));
                zos.write(xml.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                zos.closeEntry();
            }
            return java.util.Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Error al comprimir el XML de la factura: " + e.getMessage());
        }
    }
}
