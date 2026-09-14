package com.pos_backend.facturacion.infraestructure.driver_adapters.factus;

import com.fasterxml.jackson.databind.JsonNode;
import com.pos_backend.facturacion.domain.model.*;
import com.pos_backend.facturacion.domain.model.gateway.FacturaElectronicaGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Mapea nuestro dominio al formato que espera la API de Factus V2 y viceversa.
 * Referencia usada para los códigos DIAN (tabla de tipos de documento, tipos de
 * organización, impuestos, etc): developers.factus.com.co/tablas-de-referencia/tablas
 * — verificado con llamadas reales al sandbox antes de fijar este mapeo.
 */
@Component
@RequiredArgsConstructor
public class FactusFacturaElectronicaGatewayImpl implements FacturaElectronicaGateway {

    private final FactusHttpClient http;
    private final DivipolaMunicipioResolver municipioResolver;

    // package-private (no static/private) para poder probarla directo desde el test,
    // sin tener que instanciar la clase completa con sus dependencias de Spring.
    static final Map<String, String> CODIGO_TIPO_DOCUMENTO = Map.of(
            "CC", "13", "NIT", "31", "CE", "22", "TI", "12", "PASAPORTE", "41", "RC", "11"
    );

    @Override
    public void verificarCredenciales(ConfiguracionDian config) {
        http.verificarCredenciales(config);
    }

    @Override
    public java.util.List<RangoNumeracion> listarRangosNumeracion(ConfiguracionDian config, String codigoDocumento) {
        // La respuesta viene paginada (Laravel-style): {"data":{"data":[...],"pagination":{...}}}.
        JsonNode data = http.getDocumento(config, "/v2/numbering-ranges?filter[document]=" + codigoDocumento + "&filter[is_active]=1")
                .path("data").path("data");
        java.util.List<RangoNumeracion> rangos = new ArrayList<>();
        for (JsonNode r : data) {
            rangos.add(new RangoNumeracion(
                    r.path("id").asLong(),
                    r.path("document").asText(null),
                    r.path("prefix").asText(null),
                    r.path("from").asText(null),
                    r.path("to").asText(null),
                    r.path("current").asText(null),
                    r.path("is_active").asInt(0) == 1
            ));
        }
        return rangos;
    }

    @Override
    public java.util.List<InfoSuscripcion> listarSuscripciones(ConfiguracionDian config) {
        JsonNode data = http.getDocumento(config, "/v2/subscriptions").path("data");
        java.util.List<InfoSuscripcion> suscripciones = new ArrayList<>();
        for (JsonNode s : data) {
            suscripciones.add(new InfoSuscripcion(
                    s.path("name").asText(null),
                    s.path("has_unlimited_quota").asBoolean(false) ? null : s.path("documents_quota").asInt(),
                    s.path("documents_consumed").asInt(0),
                    s.path("has_unlimited_quota").asBoolean(false) ? null : s.path("documents_available").asInt(),
                    s.path("has_unlimited_quota").asBoolean(false),
                    s.path("is_active").asBoolean(false),
                    s.path("has_expired").asBoolean(false),
                    s.path("days_until_expiration").asInt(0)
            ));
        }
        return suscripciones;
    }

    @Override
    public void eliminarNoValidada(ConfiguracionDian config, String tipoDocumento, String referenceCode) {
        // Facturas usa "/destroy/reference/", los demás solo "/reference/" — verificado
        // uno por uno contra la colección oficial de Postman de Factus, no es simétrico.
        String path = switch (tipoDocumento) {
            case "FACTURA" -> "/v2/bills/destroy/reference/" + referenceCode;
            case "NOTA_CREDITO" -> "/v2/credit-notes/reference/" + referenceCode;
            case "NOTA_DEBITO" -> "/v2/debit-notes/reference/" + referenceCode;
            case "DOCUMENTO_SOPORTE" -> "/v2/support-documents/reference/" + referenceCode;
            case "NOMINA" -> "/v2/payrolls/reference/" + referenceCode;
            // Ojo: esta sí es /v1/ en la doc de Factus (developers.factus.com.co/notas-ajuste-documentos-soporte/eliminar),
            // no /v2/ como todo lo demás — verificado 2026-09-05, no es un error de tipeo nuestro.
            case "NOTA_AJUSTE_DOCUMENTO_SOPORTE" -> "/v1/adjustment-notes/reference/" + referenceCode;
            case "NOTA_AJUSTE_NOMINA" -> "/v2/adjustment-payrolls/reference/" + referenceCode;
            default -> throw new IllegalArgumentException("Tipo de documento desconocido: " + tipoDocumento);
        };
        http.eliminarDocumento(config, path);
    }

    @Override
    public ResultadoEmision emitirFactura(ConfiguracionDian config, ClienteRemoto cliente, EmpresaRemota empresa, VentaRemota venta, String referenceCode) {
        Map<String, Object> body = new HashMap<>();
        body.put("reference_code", referenceCode);
        body.put("document", "01");
        if (config.getFacturaNumberingRangeId() != null)
            body.put("numbering_range_id", config.getFacturaNumberingRangeId());
        body.put("customer", customer(cliente, resolverMunicipio(cliente, empresa)));
        body.put("items", items(venta.getItems()));
        body.put("payment_details", List.of(pagoContado(venta.getTotal())));

        JsonNode data = http.postDocumento(config, "/v2/bills/validate", body).path("data");
        return resultado(config, data, data.path("number").asText(null));
    }

    @Override
    public ResultadoEmision emitirNotaCredito(ConfiguracionDian config, ClienteRemoto cliente, EmpresaRemota empresa, NotaCredito nota, String referenceCode) {
        Map<String, Object> body = new HashMap<>();
        body.put("reference_code", referenceCode);
        body.put("correction_concept_code", nota.getConceptoCodigo());
        body.put("bill_number", nota.getNumeroFactura());
        if (config.getNotaCreditoNumberingRangeId() != null)
            body.put("numbering_range_id", config.getNotaCreditoNumberingRangeId());
        body.put("customer", customer(cliente, resolverMunicipio(cliente, empresa)));
        body.put("items", itemsNota(nota.getItems()));
        body.put("payment_details", List.of(pagoContado(nota.getTotal())));

        JsonNode data = http.postDocumento(config, "/v2/credit-notes/validate", body).path("data");
        return resultado(config, data, data.path("number").asText(null));
    }

    @Override
    public ResultadoEmision emitirNotaDebito(ConfiguracionDian config, ClienteRemoto cliente, EmpresaRemota empresa,
                                              NotaDebito nota, String referenceCode) {
        Map<String, Object> body = new HashMap<>();
        body.put("reference_code", referenceCode);
        body.put("correction_concept_code", nota.getConceptoCodigo());
        body.put("bill_number", nota.getNumeroFactura());
        if (config.getNotaDebitoNumberingRangeId() != null)
            body.put("numbering_range_id", config.getNotaDebitoNumberingRangeId());
        body.put("customer", customer(cliente, resolverMunicipio(cliente, empresa)));
        body.put("items", itemsNotaDebito(nota.getItems()));
        body.put("payment_details", List.of(pagoContado(nota.getTotal())));

        JsonNode data = http.postDocumento(config, "/v2/debit-notes/validate", body).path("data");
        return resultado(config, data, data.path("number").asText(null));
    }

    @Override
    public ResultadoEmision emitirDocumentoSoporte(ConfiguracionDian config, ProveedorRemota proveedor,
                                                     EmpresaRemota empresa, DocumentoSoporte documento, String referenceCode) {
        Map<String, Object> body = new HashMap<>();
        body.put("reference_code", referenceCode);
        if (config.getDocumentoSoporteNumberingRangeId() != null)
            body.put("numbering_range_id", config.getDocumentoSoporteNumberingRangeId());
        body.put("provider", proveedor(proveedor, empresa));
        body.put("items", itemsDocumentoSoporte(documento.getItems()));
        body.put("payment_details", List.of(pagoContado(documento.getTotal())));

        JsonNode data = http.postDocumento(config, "/v2/support-documents/validate", body).path("data");
        return resultado(config, data, data.path("number").asText(null));
    }

    @Override
    public ResultadoEmision emitirNotaAjusteDocumentoSoporte(ConfiguracionDian config, ProveedorRemota proveedor,
                                                              EmpresaRemota empresa, NotaAjusteDocumentoSoporte nota,
                                                              String referenceCode) {
        Map<String, Object> body = new HashMap<>();
        body.put("reference_code", referenceCode);
        body.put("support_document_number", nota.getNumeroDocumentoSoporte());
        body.put("correction_concept_code", nota.getConceptoCodigo());
        if (nota.getObservacion() != null && !nota.getObservacion().isBlank())
            body.put("observation", nota.getObservacion());
        if (config.getNotaAjusteDocumentoSoporteNumberingRangeId() != null)
            body.put("numbering_range_id", config.getNotaAjusteDocumentoSoporteNumberingRangeId());
        body.put("provider", proveedor(proveedor, empresa));
        body.put("items", itemsDocumentoSoporte(nota.getItems()));
        body.put("payment_details", List.of(pagoContado(nota.getTotal())));

        JsonNode data = http.postDocumento(config, "/v2/adjustment-notes/validate", body).path("data");
        // Esta nota usa "cuds" en vez de "cude"/"cufe" como todo lo demás (verificado
        // 2026-09-05 contra el ejemplo real de respuesta) — no se puede reusar resultado().
        boolean aceptada = data.path("is_validated").asBoolean(false);
        String mensaje = data.hasNonNull("errors") ? data.get("errors").toString() : null;
        return new ResultadoEmision(
                aceptada,
                data.path("number").asText(null),
                data.path("cuds").asText(null),
                data.path("links").path("qr").asText(null),
                null,
                FactusHttpClient.ambiente(config),
                mensaje
        );
    }

    // Sin ítems ni concepto: es una eliminación pura de una nómina ya ACEPTADA. La
    // doc de Factus no publica un ejemplo de respuesta para este endpoint (verificado
    // 2026-09-05) y no se pudo probar en vivo — la cuenta demo no tiene módulo de
    // nómina habilitado (mismo bloqueo que emitirNomina). Se asume el mismo shape que
    // el resto ({"data":{...,"errors":[...]}); si Factus responde distinto, ajustar
    // cuando haya cuenta real para probarlo.
    @Override
    public ResultadoEmision emitirNotaAjusteNomina(ConfiguracionDian config, NotaAjusteNomina nota, String referenceCode) {
        Map<String, Object> body = new HashMap<>();
        body.put("payroll_number", nota.getNumeroNominaElectronica());
        body.put("reference_code", referenceCode);
        if (config.getNotaAjusteNominaNumberingRangeId() != null && !config.getNotaAjusteNominaNumberingRangeId().isBlank())
            body.put("numbering_range_id", config.getNotaAjusteNominaNumberingRangeId());

        JsonNode data = http.postDocumento(config, "/v2/adjustment-payrolls", body).path("data");
        boolean tieneErrores = data.hasNonNull("errors") && data.get("errors").size() > 0;
        boolean aceptada = data.has("is_validated") ? data.path("is_validated").asBoolean(false) : !tieneErrores;
        String mensaje = tieneErrores ? data.get("errors").toString() : null;
        return new ResultadoEmision(
                aceptada,
                data.path("number").asText(data.path("payroll_number").asText(null)),
                null, null, null,
                FactusHttpClient.ambiente(config),
                mensaje
        );
    }

    @Override
    public String cargarDocumentoRecibido(ConfiguracionDian config, String cufe) {
        Map<String, Object> body = new HashMap<>();
        body.put("track_id", cufe);
        http.postDocumento(config, "/v2/receptions/upload", body);

        // Factus no devuelve el billId en la carga — hay que buscarlo por CUFE
        // (misma paginación estilo Laravel que /v2/numbering-ranges: data.data[]).
        JsonNode lista = http.getDocumento(config, "/v2/receptions/bills?filter[cufe]=" + cufe).path("data").path("data");
        if (!lista.isArray() || lista.isEmpty())
            throw new RuntimeException("Factus no encontró la factura recibida por su CUFE después de cargarla");
        String billId = lista.get(0).path("id").asText(null);
        if (billId == null)
            throw new RuntimeException("Factus cargó la factura recibida pero no devolvió su id");
        return billId;
    }

    @Override
    public ResultadoEventoRadian emitirEventoRadian(ConfiguracionDian config, String billId, EventoRadian evento,
                                                      RecepcionDocumento.Persona persona, ConceptoReclamoRadian conceptoReclamo) {
        if (evento == EventoRadian.RECLAMO && conceptoReclamo == null)
            throw new RuntimeException("El evento de reclamo exige indicar un concepto (ConceptoReclamoRadian)");

        Map<String, Object> body = new HashMap<>();
        // La doc de Factus da un ejemplo con identification_document_code=6 para NIT
        // en este endpoint específico, distinto de la tabla estándar (31) que usa el
        // resto de la API — no se pudo confirmar cuál rige realmente (sin ejemplo de
        // respuesta ni cuenta con facturas recibidas para probarlo en vivo). Se usa
        // la tabla estándar por consistencia con el resto del código; revisar si
        // Factus la rechaza en la práctica.
        body.put("identification_document_code", CODIGO_TIPO_DOCUMENTO.getOrDefault(persona.getTipoDocumento(), "13"));
        body.put("identification", persona.getNumeroDocumento());
        if (persona.getDv() != null && !persona.getDv().isBlank()) body.put("dv", persona.getDv());
        body.put("first_name", persona.getNombres());
        body.put("last_name", persona.getApellidos());
        if (persona.getCargo() != null && !persona.getCargo().isBlank()) body.put("job_title", persona.getCargo());
        if (persona.getArea() != null && !persona.getArea().isBlank()) body.put("organization_department", persona.getArea());
        if (evento == EventoRadian.RECLAMO) body.put("claim_concept_code", conceptoReclamo.getCodigo());

        try {
            JsonNode data = http.patchDocumento(config,
                    "/v2/receptions/bills/" + billId + "/radian/events/" + evento.getCodigo(), body);
            String mensaje = data != null && data.hasNonNull("message") ? data.get("message").asText() : "Evento emitido a la DIAN";
            return new ResultadoEventoRadian(true, mensaje);
        } catch (RuntimeException e) {
            return new ResultadoEventoRadian(false, e.getMessage());
        }
    }

    // Proveedor SIEMPRE con NIT — la tabla de identificación de Factus para documentos
    // soporte no incluye cédula de ciudadanía (verificado en su tabla de referencia), y
    // ya se valida en el use case que el proveedor tenga NIT antes de llegar acá.
    private Map<String, Object> proveedor(ProveedorRemota proveedor, EmpresaRemota empresa) {
        Map<String, Object> p = new HashMap<>();
        p.put("identification_document_code", "31");
        // Proveedor guarda el NIT como texto libre ("900111220-0" o "900123456").
        // El sandbox compartido aceptaba el string tal cual; el privado de cada
        // cliente valida contra la DIAN (reglas DSAJ21/DSAJ24b) y exige número y DV
        // por separado.
        String[] nitDv = nitYDv(proveedor.getNit(), proveedor.getNombre());
        p.put("identification", nitDv[0]);
        p.put("dv", nitDv[1]);
        p.put("names", proveedor.getNombre());
        p.put("address", proveedor.getDireccion() != null && !proveedor.getDireccion().isBlank()
                ? proveedor.getDireccion() : "No especificada");
        p.put("country_code", "CO");
        p.put("legal_organization_code", "1");
        // Proveedor no guarda departamento, solo ciudad — si no se puede resolver con
        // eso solo, se usa el municipio de la propia empresa (misma decisión que ya se
        // tomó para clientes sin dirección al facturar).
        String municipio = municipioResolver.resolver(null, proveedor.getCiudad());
        if (municipio == null && empresa != null)
            municipio = municipioResolver.resolver(empresa.getDepartamento(), empresa.getCiudad());
        if (municipio == null)
            throw new RuntimeException("No se pudo determinar el municipio del proveedor ni de la empresa — " +
                    "completa la ciudad del proveedor o de tu empresa en Configuración.");
        p.put("municipality_code", municipio);
        if (proveedor.getCorreo() != null && !proveedor.getCorreo().isBlank()) p.put("email", proveedor.getCorreo());
        if (proveedor.getTelefono() != null && !proveedor.getTelefono().isBlank()) p.put("phone", proveedor.getTelefono());
        return p;
    }

    // Devuelve {número, dv}. Si el NIT trae DV tras el guion y no cuadra, se avisa
    // acá con un mensaje claro en vez de esperar el rechazo DSAJ24b de la DIAN.
    static String[] nitYDv(String nit, String nombreProveedor) {
        String[] partes = (nit == null ? "" : nit).split("-", 2);
        String numero = partes[0].replaceAll("\\D", "");
        if (numero.isEmpty())
            throw new RuntimeException("El proveedor " + nombreProveedor + " no tiene NIT — complétalo antes de emitir el documento soporte");
        String dv = String.valueOf(calcularDv(numero));
        String dvGuardado = partes.length > 1 ? partes[1].replaceAll("\\D", "") : "";
        if (!dvGuardado.isEmpty() && !dvGuardado.equals(dv))
            throw new RuntimeException("El NIT del proveedor " + nombreProveedor + " (" + nit + ") tiene el dígito de verificación mal: para "
                    + numero + " debería ser " + dv + ". Corrígelo en Proveedores.");
        return new String[]{numero, dv};
    }

    // Algoritmo módulo 11 de la DIAN. Verificado con NITs reales: DIAN 800197268-4.
    static int calcularDv(String numero) {
        int[] pesos = {3, 7, 13, 17, 19, 23, 29, 37, 41, 43, 47, 53, 59, 67, 71};
        int suma = 0;
        for (int i = 0; i < numero.length(); i++)
            suma += Character.getNumericValue(numero.charAt(numero.length() - 1 - i)) * pesos[i];
        int r = suma % 11;
        return r < 2 ? r : 11 - r;
    }

    private List<Map<String, Object>> itemsDocumentoSoporte(List<DocumentoSoporte.ItemDocumentoSoporte> items) {
        return items.stream().map(this::itemDocumentoSoporte).toList();
    }

    private Map<String, Object> itemDocumentoSoporte(DocumentoSoporte.ItemDocumentoSoporte item) {
        Map<String, Object> m = new HashMap<>();
        m.put("code_reference", item.getSku() != null && !item.getSku().isBlank() ? item.getSku() : "SIN-SKU");
        m.put("name", item.getDescripcion());
        m.put("quantity", formatoMoneda(item.getCantidad()));
        m.put("discount_rate", "0.00");
        m.put("price", formatoMoneda(item.getPrecioUnitario()));
        m.put("unit_measure_code", "94");
        m.put("standard_code", "999");
        m.put("taxes", List.of(taxPorTasa(item.getPorcentajeIva())));
        if (item.getPorcentajeRetencion() != null && item.getPorcentajeRetencion() > 0) {
            Map<String, Object> retencion = new HashMap<>();
            retencion.put("code", "06"); // retención sobre renta — no manejamos retención de IVA (05) por ahora
            retencion.put("rate", formatoMoneda(item.getPorcentajeRetencion()));
            m.put("withholding_taxes", List.of(retencion));
        }
        return m;
    }

    // Un trabajador por solicitud — Factus no acepta un array de empleados en
    // /v2/payrolls (a pesar de que su propia página "Campos de la nómina" liste el
    // endpoint como /v2/payroll/validate, verificado contra el sandbox real que esa
    // ruta no existe — es un typo de su doc, la real es esta).
    @Override
    public ResultadoEmision emitirNomina(ConfiguracionDian config, EmpleadoRemota empleado, EmpresaRemota empresa,
                                          NominaRemota nomina, NominaRemota.DetalleRemoto detalle, String referenceCode) {
        Map<String, Object> body = new HashMap<>();
        body.put("reference_code", referenceCode);
        if (config.getNominaNumberingRangeId() != null && !config.getNominaNumberingRangeId().isBlank())
            body.put("numbering_range_id", config.getNominaNumberingRangeId());
        body.put("settlement_period", periodoLiquidacion(nomina));
        body.put("payment", pagoNomina(empleado, nomina));
        body.put("worker", trabajador(empleado, detalle, empresa));
        body.put("accruals", devengados(detalle));
        body.put("deductions", deducciones(detalle));

        JsonNode data = http.postDocumento(config, "/v2/payrolls", body).path("data");
        return resultado(config, data, data.path("number").asText(null));
    }

    private Map<String, Object> periodoLiquidacion(NominaRemota nomina) {
        Map<String, Object> p = new HashMap<>();
        p.put("month", nomina.getMes());
        p.put("year", nomina.getAnio());
        p.put("payroll_period_code", "QUINCENAL".equals(nomina.getPeriodicidad()) ? "4" : "5");
        return p;
    }

    private Map<String, Object> pagoNomina(EmpleadoRemota empleado, NominaRemota nomina) {
        Map<String, Object> p = new HashMap<>();
        boolean tieneBanco = empleado.getBancoPago() != null && !empleado.getBancoPago().isBlank()
                && empleado.getNumeroCuenta() != null && !empleado.getNumeroCuenta().isBlank();
        p.put("payment_method_code", tieneBanco ? "42" : "10"); // 42=consignación, 10=efectivo
        if (tieneBanco) {
            p.put("bank_name", empleado.getBancoPago());
            p.put("account_type", "CORRIENTE".equals(empleado.getTipoCuenta()) ? "3" : "2"); // 2=ahorros, 3=corriente
            p.put("account_number", empleado.getNumeroCuenta());
        }
        p.put("payment_date", (nomina.getFechaPago() != null ? nomina.getFechaPago() : java.time.LocalDate.now()).toString());
        return p;
    }

    private static final java.util.Map<String, String> CODIGO_TIPO_DOC_EMPLEADO =
            java.util.Map.of("CC", "13", "CE", "22", "PA", "41", "PEP", "47");

    private Map<String, Object> trabajador(EmpleadoRemota empleado, NominaRemota.DetalleRemoto detalle, EmpresaRemota empresa) {
        Map<String, Object> w = new HashMap<>();
        w.put("identification_document_code", CODIGO_TIPO_DOC_EMPLEADO.getOrDefault(empleado.getTipoDocumento(), "13"));
        w.put("identification_number", empleado.getNumeroDocumento());

        // El nombre se captura como un solo texto en Empleado — se parte por espacios
        // porque Factus pide nombre(s)/apellidos por separado. ponytail: heurística
        // simple, un apellido compuesto con espacio ("DE LA CRUZ") queda mal partido;
        // arreglar cuando Empleado capture nombres/apellidos en campos separados.
        String[] nombres = (empleado.getNombres() == null ? "" : empleado.getNombres().trim()).split("\\s+", 2);
        w.put("first_name", nombres[0].isEmpty() ? "Sin nombre" : nombres[0]);
        if (nombres.length > 1 && !nombres[1].isBlank()) w.put("other_names", nombres[1]);
        String[] apellidos = (empleado.getApellidos() == null ? "" : empleado.getApellidos().trim()).split("\\s+", 2);
        w.put("first_surname", apellidos[0].isEmpty() ? "Sin apellido" : apellidos[0]);
        w.put("second_surname", apellidos.length > 1 ? apellidos[1] : apellidos[0].isEmpty() ? "Sin apellido" : apellidos[0]);

        w.put("address", empleado.getDireccion() != null && !empleado.getDireccion().isBlank()
                ? empleado.getDireccion() : "No especificada");
        w.put("country_code", "CO");
        String municipio = municipioResolver.resolver(null, empleado.getCiudad());
        if (municipio == null && empresa != null)
            municipio = municipioResolver.resolver(empresa.getDepartamento(), empresa.getCiudad());
        if (municipio == null)
            throw new RuntimeException("No se pudo determinar el municipio del empleado ni de la empresa — " +
                    "completa la ciudad del empleado o de tu empresa en Configuración.");
        w.put("municipality_code", municipio);

        w.put("has_integral_salary", Boolean.TRUE.equals(empleado.getSalarioIntegral()));
        // "Alto riesgo" (Decreto 2090/2003) es un concepto legal distinto al nivel de
        // riesgo ARL — no lo inferimos de ahí para no adivinar mal, se manda false.
        w.put("has_high_risk", false);
        w.put("worker_type_code", "01");  // Dependiente — el único tipo que maneja hoy Plutus365
        w.put("worker_subtype", "00");    // No aplica
        w.put("contract_type", switch (empleado.getTipoContrato() != null ? empleado.getTipoContrato() : "") {
            case "FIJO" -> "1";
            case "INDEFINIDO" -> "2";
            case "OBRA_LABOR" -> "3";
            case "APRENDIZAJE" -> "4";
            default -> "2";
        });
        w.put("employee_code", String.valueOf(empleado.getEmpleadoId()));
        w.put("salary", formatoMoneda(empleado.getSalarioBase()));
        w.put("entry_date", empleado.getFechaIngreso() != null ? empleado.getFechaIngreso().toString()
                : java.time.LocalDate.now().toString());
        w.put("days_worked", String.valueOf(detalle.getDiasTrabajados() != null ? detalle.getDiasTrabajados() : 30));
        return w;
    }

    // Solo se arman los conceptos que venta365/nomina-service realmente calcula hoy —
    // Factus soporta ~23 tipos de devengado (cesantías, primas, vacaciones,
    // incapacidades, licencias...) que nomina-service no liquida todavía; no se
    // inventan, quedan sin usar hasta que se calculen de verdad.
    Map<String, Object> devengados(NominaRemota.DetalleRemoto d) {
        Map<String, Object> a = new HashMap<>();
        Map<String, Object> suel = new HashMap<>();
        suel.put("amount", formatoMoneda(d.getSueldo()));
        a.put("suel", suel);

        if (d.getAuxilioTransporte() != null && d.getAuxilioTransporte() > 0)
            a.put("tra", List.of(Map.of("amount", formatoMoneda(d.getAuxilioTransporte()), "accrual_type_code", "1")));
        if (d.getComisiones() != null && d.getComisiones() > 0)
            a.put("comi", List.of(Map.of("amount", formatoMoneda(d.getComisiones()))));
        if (d.getBonificaciones() != null && d.getBonificaciones() > 0)
            a.put("boni", List.of(Map.of("amount", formatoMoneda(d.getBonificaciones()), "accrual_type_code", "1")));
        if (d.getOtrosDevengados() != null && d.getOtrosDevengados() > 0)
            a.put("otro", List.of(new HashMap<>(Map.of(
                    "amount", formatoMoneda(d.getOtrosDevengados()),
                    "description", "Otros devengados",
                    "accrual_type_code", "1"))));

        if (d.getHorasExtra() != null && !d.getHorasExtra().isEmpty()) {
            List<Map<String, Object>> horas = d.getHorasExtra().stream().map(h -> {
                Map<String, Object> hm = new HashMap<>();
                hm.put("quantity", (int) Math.round(h.getCantidadHoras() != null ? h.getCantidadHoras() : 0));
                hm.put("percentage", formatoMoneda(h.getPorcentaje()));
                hm.put("amount", formatoMoneda(h.getValor()));
                hm.put("accrual_type_code", String.valueOf(h.getTipoCode()));
                return hm;
            }).toList();
            a.put("hora", horas);
        }
        return a;
    }

    // Los porcentajes de salud/pensión/FSP no se guardan como número aparte en
    // NominaDetalle, solo el valor ya calculado — se recalculan aquí a partir del
    // valor y el IBC (mismos números que nomina-service usó, sin duplicar sus
    // constantes legales entre dos microservicios).
    Map<String, Object> deducciones(NominaRemota.DetalleRemoto d) {
        Map<String, Object> ded = new HashMap<>();
        double ibc = d.getIbc() != null && d.getIbc() > 0 ? d.getIbc() : 1;

        Map<String, Object> salud = new HashMap<>();
        salud.put("percentage", formatoMoneda(porcentajeDe(d.getSaludEmpleado(), ibc)));
        salud.put("amount", formatoMoneda(d.getSaludEmpleado()));
        ded.put("salu", salud);

        Map<String, Object> pension = new HashMap<>();
        pension.put("percentage", formatoMoneda(porcentajeDe(d.getPensionEmpleado(), ibc)));
        pension.put("amount", formatoMoneda(d.getPensionEmpleado()));
        ded.put("pens", pension);

        if (d.getFondoSolidaridad() != null && d.getFondoSolidaridad() > 0) {
            Map<String, Object> fsp = new HashMap<>();
            fsp.put("percentage", formatoMoneda(porcentajeDe(d.getFondoSolidaridad(), ibc)));
            fsp.put("amount", formatoMoneda(d.getFondoSolidaridad()));
            fsp.put("deduction_type_code", "1"); // deducción de solidaridad pensional
            ded.put("dedu", fsp);
        }
        if (d.getRetencionFuente() != null && d.getRetencionFuente() > 0)
            ded.put("rete", Map.of("amount", formatoMoneda(d.getRetencionFuente())));
        // "préstamo" se mapea a deuda del trabajador con la empresa — el tipo que
        // menos campos extra exige (no pide description, a diferencia de libranza).
        if (d.getPrestamos() != null && d.getPrestamos() > 0)
            ded.put("deud", Map.of("amount", formatoMoneda(d.getPrestamos())));
        if (d.getOtrasDeducciones() != null && d.getOtrasDeducciones() > 0)
            ded.put("otra", List.of(Map.of("amount", formatoMoneda(d.getOtrasDeducciones()))));
        return ded;
    }

    private double porcentajeDe(Double valor, double ibc) {
        return valor == null || valor == 0 ? 0 : redondear2((valor / ibc) * 100);
    }

    private double redondear2(double v) { return Math.round(v * 100.0) / 100.0; }

    // Factus exige municipality_code siempre. Cuando el cliente no tiene ciudad/departamento
    // (consumidor final, o uno registrado solo con el documento) se usa el municipio de la
    // propia empresa — decisión del usuario, no una norma DIAN verificada puntualmente para
    // este caso; si el contador objeta, ajustar acá.
    private String resolverMunicipio(ClienteRemoto cliente, EmpresaRemota empresa) {
        String codigo = municipioResolver.resolver(cliente.getDepartamento(), cliente.getCiudad());
        if (codigo == null && empresa != null)
            codigo = municipioResolver.resolver(empresa.getDepartamento(), empresa.getCiudad());
        if (codigo == null)
            throw new RuntimeException("No se pudo determinar el municipio DIAN para facturar — " +
                    "completa ciudad y departamento en Configuración de la empresa (Factus lo exige siempre).");
        return codigo;
    }

    private ResultadoEmision resultado(ConfiguracionDian config, JsonNode data, String numeroDocumento) {
        boolean aceptada = data.path("is_validated").asBoolean(false);
        // Factura manda "cufe", notas "cude", documento soporte "cuds" (verificado
        // 2026-09-12 contra GET /v2/support-documents/{number} en sandbox privado).
        String cufeOCude = data.hasNonNull("cufe") ? data.get("cufe").asText()
                : data.hasNonNull("cude") ? data.get("cude").asText()
                : data.path("cuds").asText(null);
        String mensaje = data.hasNonNull("errors") ? data.get("errors").toString() : null;
        return new ResultadoEmision(
                aceptada,
                numeroDocumento,
                cufeOCude,
                data.path("links").path("qr").asText(null),
                data.path("links").path("public_url").asText(null),
                FactusHttpClient.ambiente(config),
                mensaje
        );
    }

    // ponytail: sin desglose real de forma de pago (venta-service no lo expone hoy) —
    // se manda todo como contado/efectivo. Ajustar cuando venta-service exponga el
    // medio de pago real; ver misma limitación documentada en NotaCreditoUseCase.
    private Map<String, Object> pagoContado(Double total) {
        Map<String, Object> pago = new HashMap<>();
        pago.put("payment_form", "1");
        pago.put("payment_method_code", "10");
        pago.put("amount", formatoMoneda(total));
        return pago;
    }

    static Map<String, Object> customer(ClienteRemoto cliente, String municipalityCode) {
        Map<String, Object> c = new HashMap<>();
        c.put("identification_document_code", CODIGO_TIPO_DOCUMENTO.getOrDefault(cliente.getTipoDocumento(), "13"));
        c.put("identification", cliente.getNumeroDocumento());
        if (cliente.getDv() != null && !cliente.getDv().isBlank()) c.put("dv", cliente.getDv());
        boolean juridica = "JURIDICA".equals(cliente.getTipoPersona());
        c.put("legal_organization_code", juridica ? "1" : "2");
        // Factus exige "names" siempre, sea persona natural o jurídica (igual que en
        // proveedor() más arriba) — para jurídica además se manda "company".
        if (juridica) {
            c.put("company", cliente.getRazonSocial());
            c.put("names", cliente.getRazonSocial());
        } else {
            c.put("names", ((cliente.getNombres() != null ? cliente.getNombres() : "") + " "
                    + (cliente.getApellidos() != null ? cliente.getApellidos() : "")).trim());
        }
        c.put("tribute_code", "ZZ");
        c.put("responsibilities", List.of("R-99-PN"));
        // "CO" por defecto: no tenemos catálogo de países para clientes extranjeros.
        c.put("country_code", "CO");
        c.put("municipality_code", municipalityCode);
        if (cliente.getCorreo() != null && !cliente.getCorreo().isBlank()) c.put("email", cliente.getCorreo());
        if (cliente.getTelefono() != null && !cliente.getTelefono().isBlank()) c.put("phone", cliente.getTelefono());
        if (cliente.getDireccion() != null && !cliente.getDireccion().isBlank()) c.put("address", cliente.getDireccion());
        return c;
    }

    private List<Map<String, Object>> items(List<VentaRemota.ItemRemoto> items) {
        return items.stream().map(this::item).toList();
    }

    private Map<String, Object> item(VentaRemota.ItemRemoto item) {
        Map<String, Object> m = new HashMap<>();
        m.put("code_reference", item.getSku() != null && !item.getSku().isBlank() ? item.getSku() : "SIN-SKU");
        m.put("name", item.getNombreProducto());
        m.put("quantity", String.valueOf(item.getCantidad()) + ".00");
        // Factus espera el precio unitario SIN IVA — su ejemplo documentado
        // (developers.factus.com.co/facturas/crear-y-validar/) calcula el total de la
        // línea como price*quantity y LE SUMA el impuesto encima. venta-service en
        // cambio guarda precioUnitario CON IVA incluido; valorTotal ya es la base
        // gravable de la línea (sin IVA, con el descuento ya neteado — ver
        // VentaItem.java), así que se manda esa dividida entre la cantidad, y no se
        // repite el descuento aparte para no aplicarlo dos veces.
        m.put("price", formatoMoneda(item.getValorTotal() / item.getCantidad()));
        m.put("discount_rate", "0.00");
        // Sin catálogo UNSPSC propio: "94" (unidad) y "999" (estándar de adopción del
        // contribuyente) son los defaults documentados por Factus para quien no lo tiene.
        m.put("unit_measure_code", "94");
        m.put("standard_code", "999");
        m.put("taxes", List.of(tax(item.getTipoIva())));
        return m;
    }

    private List<Map<String, Object>> itemsNota(List<NotaCredito.ItemNotaCredito> items) {
        return items.stream().map(this::itemNota).toList();
    }

    private Map<String, Object> itemNota(NotaCredito.ItemNotaCredito item) {
        Map<String, Object> m = new HashMap<>();
        m.put("code_reference", item.getSku() != null && !item.getSku().isBlank() ? item.getSku() : "SIN-SKU");
        m.put("name", item.getDescripcion());
        m.put("quantity", formatoMoneda(item.getCantidad()));
        m.put("price", formatoMoneda(item.getPrecioUnitario()));
        m.put("discount_rate", "0.00");
        m.put("unit_measure_code", "94");
        m.put("standard_code", "999");
        m.put("taxes", List.of(taxPorTasa(item.getPorcentajeIva())));
        return m;
    }

    private List<Map<String, Object>> itemsNotaDebito(List<NotaDebito.ItemNotaDebito> items) {
        return items.stream().map(this::itemNotaDebito).toList();
    }

    private Map<String, Object> itemNotaDebito(NotaDebito.ItemNotaDebito item) {
        Map<String, Object> m = new HashMap<>();
        m.put("code_reference", item.getSku() != null && !item.getSku().isBlank() ? item.getSku() : "SIN-SKU");
        m.put("name", item.getDescripcion());
        m.put("quantity", formatoMoneda(item.getCantidad()));
        m.put("price", formatoMoneda(item.getPrecioUnitario()));
        m.put("discount_rate", "0.00");
        m.put("unit_measure_code", "94");
        m.put("standard_code", "999");
        m.put("taxes", List.of(taxPorTasa(item.getPorcentajeIva())));
        return m;
    }

    static Map<String, Object> tax(String tipoIva) {
        double rate = switch (tipoIva) {
            case "GENERAL_19" -> 19.00;
            case "REDUCIDO_5" -> 5.00;
            case "EXENTO", "EXCLUIDO" -> 0.00;
            default -> throw new RuntimeException("Tipo de IVA no reconocido para facturar con Factus: " + tipoIva);
        };
        return taxPorTasa(rate, "EXCLUIDO".equals(tipoIva));
    }

    private static Map<String, Object> taxPorTasa(Double rate) {
        return taxPorTasa(rate != null ? rate : 0.0, false);
    }

    private static Map<String, Object> taxPorTasa(double rate, boolean excluido) {
        Map<String, Object> t = new HashMap<>();
        t.put("code", "01");
        t.put("rate", formatoMoneda(rate));
        if (excluido) t.put("is_excluded", true);
        return t;
    }

    private static String formatoMoneda(Double valor) {
        return String.format(Locale.US, "%.2f", valor != null ? valor : 0.0);
    }
}
