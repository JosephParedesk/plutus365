package com.pos_backend.facturacion.domain.model;

import lombok.*;

/**
 * Credenciales de Factus (proveedor tecnológico habilitado ante la DIAN) para esta
 * empresa. Cada cuenta de Factus representa UNA sola empresa emisora (verificado
 * contra developers.factus.com.co: GET /v2/companies devuelve "la empresa del
 * usuario correspondiente", sin listar ni crear varias) — por eso estas credenciales
 * son por empresa, igual que antes lo era el certificado digital propio.
 */
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class ConfiguracionDian {

    private String empresaId;          // PK - un registro por empresa

    private String factusClientId;
    private String factusClientSecret;     // se guarda cifrado (ver CertificadoCrypto)
    private String factusUsername;
    private String factusPassword;         // se guarda cifrado

    // Solo hacen falta si la cuenta de Factus tiene más de un rango activo de ese
    // tipo de documento — si no se informan, Factus usa el único rango disponible.
    private Long facturaNumberingRangeId;
    private Long notaCreditoNumberingRangeId;
    private Long notaDebitoNumberingRangeId;
    private Long documentoSoporteNumberingRangeId;
    // A diferencia de los otros 3, el ID de rango de nómina en Factus es un
    // string (ej. "01kpdv25zepw0vzn39th90h1a7"), no un entero — verificado en su
    // ejemplo de /v2/payrolls.
    private String nominaNumberingRangeId;
    // Documento código 25 en /v2/numbering-ranges — rango propio, distinto al de
    // documento soporte (24).
    private Long notaAjusteDocumentoSoporteNumberingRangeId;
    // Igual que nominaNumberingRangeId: string, no entero, según el ejemplo de
    // /v2/adjustment-payrolls.
    private String notaAjusteNominaNumberingRangeId;

    private Boolean activo;
}
