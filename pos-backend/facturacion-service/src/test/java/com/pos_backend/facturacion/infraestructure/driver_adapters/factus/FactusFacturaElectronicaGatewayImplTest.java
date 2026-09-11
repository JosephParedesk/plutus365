package com.pos_backend.facturacion.infraestructure.driver_adapters.factus;

import com.pos_backend.facturacion.domain.model.ClienteRemoto;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Check mínimo de los mapeos a códigos DIAN (verificados contra el sandbox real de
 * Factus antes de fijarlos, ver plan de implementación) — son los que más fácil se
 * rompen con un typo y generan una factura mal formada.
 */
class FactusFacturaElectronicaGatewayImplTest {

    @Test
    void mapeaTiposDeDocumentoAlCodigoDian() {
        assertEquals("13", FactusFacturaElectronicaGatewayImpl.CODIGO_TIPO_DOCUMENTO.get("CC"));
        assertEquals("31", FactusFacturaElectronicaGatewayImpl.CODIGO_TIPO_DOCUMENTO.get("NIT"));
        assertEquals("22", FactusFacturaElectronicaGatewayImpl.CODIGO_TIPO_DOCUMENTO.get("CE"));
        assertEquals("12", FactusFacturaElectronicaGatewayImpl.CODIGO_TIPO_DOCUMENTO.get("TI"));
        assertEquals("41", FactusFacturaElectronicaGatewayImpl.CODIGO_TIPO_DOCUMENTO.get("PASAPORTE"));
        assertEquals("11", FactusFacturaElectronicaGatewayImpl.CODIGO_TIPO_DOCUMENTO.get("RC"));
    }

    @Test
    void mapeaTasasDeIvaCorrectamente() {
        assertEquals("19.00", FactusFacturaElectronicaGatewayImpl.tax("GENERAL_19").get("rate"));
        assertEquals("5.00", FactusFacturaElectronicaGatewayImpl.tax("REDUCIDO_5").get("rate"));
        assertEquals("0.00", FactusFacturaElectronicaGatewayImpl.tax("EXENTO").get("rate"));
        assertNull(FactusFacturaElectronicaGatewayImpl.tax("EXENTO").get("is_excluded"));

        Map<String, Object> excluido = FactusFacturaElectronicaGatewayImpl.tax("EXCLUIDO");
        assertEquals("0.00", excluido.get("rate"));
        assertEquals(true, excluido.get("is_excluded"));
    }

    @Test
    void rechazaUnTipoDeIvaDesconocidoEnVezDeAdivinar() {
        assertThrows(RuntimeException.class, () -> FactusFacturaElectronicaGatewayImpl.tax("NO_EXISTE"));
    }

    @Test
    void clientePersonaJuridicaMandaRazonSocialComoCompany() {
        ClienteRemoto cliente = new ClienteRemoto();
        cliente.setTipoPersona("JURIDICA");
        cliente.setTipoDocumento("NIT");
        cliente.setNumeroDocumento("900123456");
        cliente.setRazonSocial("Distribuidora ACME SAS");

        Map<String, Object> customer = FactusFacturaElectronicaGatewayImpl.customer(cliente, "11001");
        assertEquals("31", customer.get("identification_document_code"));
        assertEquals("1", customer.get("legal_organization_code"));
        assertEquals("Distribuidora ACME SAS", customer.get("company"));
        assertNull(customer.get("names"));
    }

    @Test
    void clientePersonaNaturalMandaNombreCompletoEnNames() {
        ClienteRemoto cliente = ClienteRemoto.consumidorFinalGenerico();
        Map<String, Object> customer = FactusFacturaElectronicaGatewayImpl.customer(cliente, "11001");
        assertEquals("13", customer.get("identification_document_code"));
        assertEquals("2", customer.get("legal_organization_code"));
        assertEquals("Consumidor Final", customer.get("names"));
        assertNull(customer.get("company"));
        assertEquals("11001", customer.get("municipality_code"));
    }
}
