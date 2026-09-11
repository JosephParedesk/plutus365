package com.pos_backend.facturacion.infraestructure.driver_adapters.factus;

import com.pos_backend.facturacion.domain.model.NominaRemota;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Check mínimo del mapeo de devengados/deducciones de nómina a la forma que exige
 * Factus — son los que más fácil se rompen con un typo en la clave del mapa
 * ("suel", "tra", "salu"...) o con el porcentaje mal recalculado desde el IBC.
 */
class FactusNominaMappingTest {

    private final FactusFacturaElectronicaGatewayImpl gateway =
            new FactusFacturaElectronicaGatewayImpl(
                    new FactusHttpClient("https://api-sandbox.factus.com.co"),
                    new DivipolaMunicipioResolver());

    private NominaRemota.DetalleRemoto detalleBasico() {
        NominaRemota.DetalleRemoto d = new NominaRemota.DetalleRemoto();
        d.setSueldo(2_800_000.0);
        d.setIbc(2_800_000.0);
        d.setSaludEmpleado(112_000.0);   // 4% del IBC
        d.setPensionEmpleado(112_000.0); // 4% del IBC
        return d;
    }

    @Test
    @SuppressWarnings("unchecked")
    void siempreMandaElSueldoBasico() {
        Map<String, Object> a = gateway.devengados(detalleBasico());
        Map<String, Object> suel = (Map<String, Object>) a.get("suel");
        assertEquals("2800000.00", suel.get("amount"));
    }

    @Test
    void noMandaConceptosOpcionalesEnCero() {
        Map<String, Object> a = gateway.devengados(detalleBasico());
        assertFalse(a.containsKey("tra"));
        assertFalse(a.containsKey("comi"));
        assertFalse(a.containsKey("boni"));
        assertFalse(a.containsKey("hora"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void mapeaCadaTipoDeHoraExtraConSuCodigo() {
        NominaRemota.DetalleRemoto d = detalleBasico();
        NominaRemota.HoraExtraRemota he = new NominaRemota.HoraExtraRemota();
        he.setTipoCode(1); he.setCantidadHoras(5.0); he.setPorcentaje(25.0); he.setValor(79_545.45);
        d.setHorasExtra(List.of(he));

        Map<String, Object> a = gateway.devengados(d);
        List<Map<String, Object>> horas = (List<Map<String, Object>>) a.get("hora");
        assertEquals(1, horas.size());
        assertEquals("1", horas.get(0).get("accrual_type_code"));
        assertEquals(5, horas.get(0).get("quantity"));
        assertEquals("25.00", horas.get(0).get("percentage"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void recalculaElPorcentajeDeSaludYPensionDesdeElIbc() {
        Map<String, Object> ded = gateway.deducciones(detalleBasico());
        Map<String, Object> salud = (Map<String, Object>) ded.get("salu");
        assertEquals("4.00", salud.get("percentage"));
        Map<String, Object> pension = (Map<String, Object>) ded.get("pens");
        assertEquals("4.00", pension.get("percentage"));
    }

    @Test
    void noMandaFondoSolidaridadNiRetencionSiSonCero() {
        Map<String, Object> ded = gateway.deducciones(detalleBasico());
        assertFalse(ded.containsKey("dedu"));
        assertFalse(ded.containsKey("rete"));
        assertFalse(ded.containsKey("deud"));
    }
}
