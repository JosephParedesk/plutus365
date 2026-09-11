package com.pos_backend.facturacion.domain.model;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ClienteRemoto {
    private Long clienteId;
    private String tipoPersona;
    private String tipoDocumento;
    private String numeroDocumento;
    private String dv;
    private String regimenFiscal;
    private String razonSocial;
    private String nombres;
    private String apellidos;
    private String correo;
    private String telefono;
    private String direccion;
    private String ciudad;
    private String departamento;
    private String pais;

    // Usado cuando la venta no tiene cliente identificado.
    // NOTA: revisar con tu contador el código DIAN vigente para consumidor final;
    // esto es una aproximación razonable, no una certeza normativa.
    public static ClienteRemoto consumidorFinalGenerico() {
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
}
