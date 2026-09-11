package com.pos_backend.compra.infraestructure.driver_adapters.importador;

import com.pos_backend.compra.domain.model.FacturaImportada;
import com.pos_backend.compra.domain.model.gateway.ImportadorFacturaGateway;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * IMPORTANTE: la DIAN no ofrece una API pública para consultar/traer una factura
 * a partir del CUFE (el portal catalogo-vpfe.dian.gov.co es para humanos, con
 * captcha, y desde jul/2026 bloquea explícitamente automatizaciones y agentes de
 * IA). Por eso esta clase NUNCA llama a la DIAN: solo lee el archivo que ya tiene
 * el usuario. El XML es un formato estructurado (UBL 2.1) y se parsea con
 * confianza alta. El PDF es solo la representación visual — cada proveedor
 * tecnológico diseña su plantilla distinto — así que la extracción es best-effort
 * y SIEMPRE debe revisarla un humano antes de guardar la compra.
 */
@Component
public class ImportadorFacturaGatewayImpl implements ImportadorFacturaGateway {

    private static final String NS_CBC = "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2";
    private static final String NS_CAC = "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2";

    @Override
    public FacturaImportada extraerDesdeXml(byte[] contenido) {
        FacturaImportada resultado = new FacturaImportada();
        resultado.setFuente("XML");
        resultado.setItems(new ArrayList<>());
        List<String> faltantes = new ArrayList<>();
        resultado.setCamposNoExtraidos(faltantes);

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true); // evita XXE
            Document doc = factory.newDocumentBuilder().parse(new ByteArrayInputStream(contenido));

            XPath xpath = XPathFactory.newInstance().newXPath();
            xpath.setNamespaceContext(namespaceContext());

            resultado.setCufe(texto(xpath, doc, "//*[local-name()='UUID']", faltantes, "CUFE"));
            resultado.setNumeroFacturaProveedor(texto(xpath, doc, "/*/*[local-name()='ID']", faltantes, "número de factura"));
            resultado.setNombreProveedor(primerNoVacio(faltantes, "nombre del proveedor",
                    () -> texto(xpath, doc, "//*[local-name()='AccountingSupplierParty']//*[local-name()='RegistrationName']", null, null),
                    () -> texto(xpath, doc, "//*[local-name()='AccountingSupplierParty']//*[local-name()='Name']", null, null)
            ));
            resultado.setNitProveedor(texto(xpath, doc, "//*[local-name()='AccountingSupplierParty']//*[local-name()='CompanyID']", faltantes, "NIT del proveedor"));

            String fechaTexto = texto(xpath, doc, "/*/*[local-name()='IssueDate']", faltantes, "fecha");
            if (fechaTexto != null) {
                try { resultado.setFecha(LocalDate.parse(fechaTexto.substring(0, 10))); }
                catch (Exception e) { faltantes.add("fecha (formato no reconocido)"); }
            }

            resultado.setSubtotal(numero(xpath, doc, "//*[local-name()='LegalMonetaryTotal']/*[local-name()='LineExtensionAmount']", faltantes, "subtotal"));
            resultado.setTotalIva(numero(xpath, doc, "//*[local-name()='TaxTotal']/*[local-name()='TaxAmount']", faltantes, "IVA"));
            resultado.setTotal(numero(xpath, doc, "//*[local-name()='LegalMonetaryTotal']/*[local-name()='PayableAmount']", faltantes, "total a pagar"));

            NodeList lineas = (NodeList) xpath.evaluate("//*[local-name()='InvoiceLine']", doc, XPathConstants.NODESET);
            for (int i = 0; i < lineas.getLength(); i++) {
                Node linea = lineas.item(i);
                try {
                    FacturaImportada.ItemImportado item = new FacturaImportada.ItemImportado();
                    item.setDescripcion(textoDeNodo(xpath, linea, ".//*[local-name()='Item']/*[local-name()='Description']"));
                    item.setCantidad(numeroDeNodo(xpath, linea, ".//*[local-name()='InvoicedQuantity']"));
                    item.setValorUnitario(numeroDeNodo(xpath, linea, ".//*[local-name()='Price']/*[local-name()='PriceAmount']"));
                    item.setValorTotal(numeroDeNodo(xpath, linea, "./*[local-name()='LineExtensionAmount']"));
                    resultado.getItems().add(item);
                } catch (Exception ignored) {
                    // una línea rara no debe tumbar la importación completa
                }
            }
            if (resultado.getItems().isEmpty()) faltantes.add("ítems (no se encontraron líneas de factura)");

        } catch (Exception e) {
            throw new RuntimeException("El archivo no parece ser un XML de factura electrónica UBL válido: " + e.getMessage());
        }

        return resultado;
    }

    @Override
    public FacturaImportada extraerDesdePdf(byte[] contenido) {
        FacturaImportada resultado = new FacturaImportada();
        resultado.setFuente("PDF");
        resultado.setItems(new ArrayList<>()); // desde PDF no se intentan extraer ítems línea por línea: muy poco confiable
        List<String> faltantes = new ArrayList<>();
        resultado.setCamposNoExtraidos(faltantes);
        faltantes.add("ítems (el PDF es solo la representación visual; agrégalos manualmente)");

        String texto;
        try (PDDocument pdf = Loader.loadPDF(contenido)) {
            texto = new PDFTextStripper().getText(pdf);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo leer el PDF: " + e.getMessage());
        }

        resultado.setCufe(buscarRegex(texto, "(?i)cufe[:\\s]*([a-f0-9]{90,96})", faltantes, "CUFE"));
        resultado.setNitProveedor(buscarRegex(texto, "(?i)nit[:\\s]*([\\d.]{6,15}-?\\d?)", faltantes, "NIT del proveedor"));
        resultado.setNumeroFacturaProveedor(buscarRegex(texto, "(?i)(?:factura|no\\.?)\\s*(?:de venta)?\\s*(?:electr[oó]nica)?\\s*:?\\s*([A-Z]{1,5}[\\s-]?\\d{1,10})", faltantes, "número de factura"));

        Double total = buscarNumeroRegex(texto, "(?i)total\\s*a\\s*pagar[:\\s$]*([\\d.,]+)");
        if (total == null) faltantes.add("total a pagar");
        resultado.setTotal(total);

        faltantes.add("nombre del proveedor (revísalo manualmente)");
        faltantes.add("fecha (revísala manualmente)");
        faltantes.add("subtotal e IVA por separado (solo se detecta el total)");

        return resultado;
    }

    // ─── Helpers XML ─────────────────────────────────────────────────────

    private NamespaceContext namespaceContext() {
        return new NamespaceContext() {
            public String getNamespaceURI(String prefix) {
                if ("cbc".equals(prefix)) return NS_CBC;
                if ("cac".equals(prefix)) return NS_CAC;
                return null;
            }
            public String getPrefix(String uri) { return null; }
            public Iterator<String> getPrefixes(String uri) { return null; }
        };
    }

    private String texto(XPath xpath, Document doc, String expresion, List<String> faltantes, String nombreCampo) {
        try {
            String valor = (String) xpath.evaluate(expresion, doc, XPathConstants.STRING);
            if (valor == null || valor.isBlank()) {
                if (faltantes != null) faltantes.add(nombreCampo);
                return null;
            }
            return valor.trim();
        } catch (Exception e) {
            if (faltantes != null) faltantes.add(nombreCampo);
            return null;
        }
    }

    private Double numero(XPath xpath, Document doc, String expresion, List<String> faltantes, String nombreCampo) {
        String valor = texto(xpath, doc, expresion, faltantes, nombreCampo);
        if (valor == null) return null;
        try { return Double.parseDouble(valor); } catch (Exception e) { return null; }
    }

    private String textoDeNodo(XPath xpath, Node nodo, String expresion) throws Exception {
        String valor = (String) xpath.evaluate(expresion, nodo, XPathConstants.STRING);
        return valor == null || valor.isBlank() ? "Ítem importado" : valor.trim();
    }

    private Double numeroDeNodo(XPath xpath, Node nodo, String expresion) throws Exception {
        String valor = (String) xpath.evaluate(expresion, nodo, XPathConstants.STRING);
        return valor == null || valor.isBlank() ? null : Double.parseDouble(valor.trim());
    }

    @FunctionalInterface
    private interface Extractor { String obtener(); }

    private String primerNoVacio(List<String> faltantes, String nombreCampo, Extractor... extractores) {
        for (Extractor e : extractores) {
            try {
                String valor = e.obtener();
                if (valor != null && !valor.isBlank()) return valor;
            } catch (Exception ignored) { }
        }
        faltantes.add(nombreCampo);
        return null;
    }

    // ─── Helpers PDF (texto plano, best-effort) ─────────────────────────

    private String buscarRegex(String texto, String patron, List<String> faltantes, String nombreCampo) {
        Matcher m = Pattern.compile(patron).matcher(texto);
        if (m.find()) return m.group(1).trim();
        faltantes.add(nombreCampo);
        return null;
    }

    private Double buscarNumeroRegex(String texto, String patron) {
        Matcher m = Pattern.compile(patron).matcher(texto);
        if (!m.find()) return null;
        try {
            String limpio = m.group(1).replace(".", "").replace(",", ".");
            return Double.parseDouble(limpio);
        } catch (Exception e) {
            return null;
        }
    }
}
