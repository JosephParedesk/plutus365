package com.pos_backend.facturacion.domain.model.gateway;

import com.pos_backend.facturacion.domain.model.*;

/**
 * Emisión de documentos electrónicos a través de Factus (proveedor tecnológico
 * habilitado ante la DIAN). Reemplaza a lo que antes hacían por separado
 * XmlFacturaGateway (armar+firmar el UBL) y DianGateway (transmitir por SOAP):
 * Factus arma, firma y transmite en una sola llamada — no necesitamos certificado
 * digital propio ni construir el XML.
 */
public interface FacturaElectronicaGateway {

    /** Prueba las credenciales de Factus pidiendo un token — lanza si son inválidas. */
    void verificarCredenciales(ConfiguracionDian config);

    /**
     * Lista los rangos de numeración activos de un tipo de documento (código de la
     * tabla de Factus: 21 factura, 22 nota crédito, 23 nota débito, 24 documento
     * soporte, 26 nómina) — hace falta para poder llenar los *NumberingRangeId de
     * ConfiguracionDian cuando la cuenta tiene más de un rango activo de ese tipo.
     */
    java.util.List<RangoNumeracion> listarRangosNumeracion(ConfiguracionDian config, String codigoDocumento);

    /**
     * Cupo real de documentos electrónicos contratado con Factus (bolsa de
     * documentos) — reemplaza al campo foliosAsignados cargado a mano.
     * Verificado contra GET /v2/subscriptions en producción 2026-09-14: la
     * respuesta trae un arreglo (normalmente una sola entrada, "Facturación",
     * que agrupa factura/notas/documento soporte bajo un mismo cupo).
     */
    java.util.List<InfoSuscripcion> listarSuscripciones(ConfiguracionDian config);

    record InfoSuscripcion(
            String nombre,
            Integer documentosAsignados,
            Integer documentosConsumidos,
            Integer documentosDisponibles,
            boolean cupoIlimitado,
            boolean activa,
            boolean expirada,
            Integer diasHastaVencer
    ) {}

    record RangoNumeracion(
            Long id,
            String document,
            String prefix,
            String from,
            String to,
            String current,
            boolean isActive
    ) {}

    /**
     * Elimina un documento NO validado (is_validated:false) de Factus por su
     * reference_code — necesario para poder reintentar cuando quedó "pendiente por
     * enviar a la DIAN". tipoDocumento: FACTURA, NOTA_CREDITO, NOTA_DEBITO,
     * DOCUMENTO_SOPORTE o NOMINA (cada uno usa un path distinto en Factus).
     */
    void eliminarNoValidada(ConfiguracionDian config, String tipoDocumento, String referenceCode);

    ResultadoEmision emitirFactura(
            ConfiguracionDian config,
            ClienteRemoto cliente,
            EmpresaRemota empresa,
            VentaRemota venta,
            String referenceCode
    );

    ResultadoEmision emitirNotaCredito(
            ConfiguracionDian config,
            ClienteRemoto cliente,
            EmpresaRemota empresa,
            NotaCredito nota,
            String referenceCode
    );

    // La doc de Factus dice que "customer" es opcional (lo toma de la factura
    // referenciada por bill_number si no se manda) — verificado 2026-09-05, pero en
    // el sandbox compartido esa resolución automática no funcionó (posible cruce por
    // la cola compartida entre integradores, ver CLAUDE.md). Se manda igual, siempre,
    // para no depender de ese lookup: es opcional según la doc, así que no debería
    // romper nada donde sí funcione.
    ResultadoEmision emitirNotaDebito(
            ConfiguracionDian config,
            ClienteRemoto cliente,
            EmpresaRemota empresa,
            NotaDebito nota,
            String referenceCode
    );

    // Documento soporte: acá SÍ hace falta mandar el proveedor explícito (no hay
    // ningún documento previo del que Factus pueda sacarlo, es distinto de un cliente).
    ResultadoEmision emitirDocumentoSoporte(
            ConfiguracionDian config,
            ProveedorRemota proveedor,
            EmpresaRemota empresa,
            DocumentoSoporte documento,
            String referenceCode
    );

    // Un trabajador por solicitud — Factus no acepta un array de empleados.
    ResultadoEmision emitirNomina(
            ConfiguracionDian config,
            EmpleadoRemota empleado,
            EmpresaRemota empresa,
            NominaRemota nomina,
            NominaRemota.DetalleRemoto detalle,
            String referenceCode
    );

    // Corrige o anula un documento soporte ya ACEPTADO — mismo proveedor del
    // documento original, Factus no lo vuelve a resolver por su cuenta.
    ResultadoEmision emitirNotaAjusteDocumentoSoporte(
            ConfiguracionDian config,
            ProveedorRemota proveedor,
            EmpresaRemota empresa,
            NotaAjusteDocumentoSoporte nota,
            String referenceCode
    );

    // Elimina ante la DIAN una nómina electrónica ya ACEPTADA — sin ítems ni
    // concepto, solo referencia el número de la nómina original.
    ResultadoEmision emitirNotaAjusteNomina(
            ConfiguracionDian config,
            NotaAjusteNomina nota,
            String referenceCode
    );

    /**
     * Carga en Factus una factura electrónica RECIBIDA de un proveedor (por su
     * CUFE) para poder emitirle eventos RADIAN después — devuelve el `billId`
     * interno de Factus para ese documento (no es el CUFE).
     */
    String cargarDocumentoRecibido(ConfiguracionDian config, String cufe);

    /**
     * Emite un evento RADIAN (acuse de recibo, reclamo, recibo del bien/servicio
     * o aceptación expresa) sobre una factura recibida ya cargada con
     * {@link #cargarDocumentoRecibido}. `conceptoReclamo` solo aplica (y es
     * obligatorio) cuando `evento` es RECLAMO.
     */
    ResultadoEventoRadian emitirEventoRadian(
            ConfiguracionDian config,
            String billId,
            EventoRadian evento,
            RecepcionDocumento.Persona persona,
            ConceptoReclamoRadian conceptoReclamo
    );

    record ResultadoEventoRadian(boolean exitoso, String mensaje) {}

    record ResultadoEmision(
            boolean aceptada,
            String numeroDocumento,   // asignado por Factus (factura) o número de nota
            String cufeOCude,
            String qrUrl,
            String urlDocumento,      // vista pública del documento en Factus
            String ambiente,          // PRUEBAS | PRODUCCION, según el endpoint de Factus usado
            String mensaje
    ) {}
}
