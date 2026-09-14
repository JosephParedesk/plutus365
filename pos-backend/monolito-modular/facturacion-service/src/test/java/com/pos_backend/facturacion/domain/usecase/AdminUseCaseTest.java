package com.pos_backend.facturacion.domain.usecase;

import com.pos_backend.facturacion.domain.model.*;
import com.pos_backend.facturacion.domain.model.gateway.ConfiguracionDianGateway;
import com.pos_backend.facturacion.domain.model.gateway.EmpresaConsultaGateway;
import com.pos_backend.facturacion.domain.model.gateway.FacturaGateway;
import com.pos_backend.facturacion.domain.model.gateway.UsuarioConsultaGateway;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Cubre lo que se agregó hoy al panel de super admin: desglose de facturas por
// estado y folios restantes (foliosAsignados - facturas emitidas). Sin
// Mockito (no está en este módulo, ver NitDvTest) — fakes a mano en su lugar.
class AdminUseCaseTest {

    private static final String EMPRESA = "EMP-71452262";

    private EmpresaRemota empresaDePrueba() {
        EmpresaRemota e = new EmpresaRemota();
        e.setEmpresaId(EMPRESA);
        e.setNombres("WILMAR FERNEY");
        e.setApellidos("MUNERA ALZATE");
        return e;
    }

    private Factura factura(String estado) {
        Factura f = new Factura();
        f.setEmpresaId(EMPRESA);
        f.setEstado(estado);
        return f;
    }

    // FacturaGateway solo usa listar() acá — el resto de métodos no se llaman
    // en listarEmpresas(), quedan sin implementar a propósito.
    private FacturaGateway facturaGatewayCon(List<Factura> facturas) {
        return new FacturaGateway() {
            public Factura guardar(Factura f) { throw new UnsupportedOperationException(); }
            public Factura buscarPorId(Long id, String empresaId) { throw new UnsupportedOperationException(); }
            public Factura buscarPorVentaId(Long ventaId, String empresaId) { throw new UnsupportedOperationException(); }
            public List<Factura> listar(String empresaId) { return facturas; }
            public void eliminar(Long id, String empresaId) { throw new UnsupportedOperationException(); }
        };
    }

    private ConfiguracionDianGateway configuracionGatewayCon(ConfiguracionDian config) {
        return new ConfiguracionDianGateway() {
            public ConfiguracionDian guardar(ConfiguracionDian c) { throw new UnsupportedOperationException(); }
            public ConfiguracionDian buscarPorEmpresaId(String empresaId) { return config; }
        };
    }

    private AdminUseCase useCase(List<Factura> facturas, ConfiguracionDian config, Long planId) {
        EmpresaConsultaGateway empresaGateway = new EmpresaConsultaGateway() {
            public EmpresaRemota buscarEmpresa(String empresaId) { throw new UnsupportedOperationException(); }
            public List<EmpresaRemota> listarTodas() { return List.of(empresaDePrueba()); }
        };
        UsuarioConsultaGateway usuarioGateway = () -> planId == null
                ? List.of()
                : List.of(new UsuarioAdminResumen("71452262", "WILMAR", "x@x.com", "ADMIN", EMPRESA, planId));

        return new AdminUseCase(empresaGateway, configuracionGatewayCon(config), facturaGatewayCon(facturas), usuarioGateway);
    }

    @Test
    void listarEmpresas_sinConfiguracionDian_foliosNulos() {
        AdminUseCase useCase = useCase(List.of(), null, 1L);

        EmpresaAdminResumen r = useCase.listarEmpresas().get(0);

        assertFalse(r.isFactusConfigurado());
        assertNull(r.getFoliosAsignados());
        assertNull(r.getFoliosRestantes());
        assertEquals(0, r.getFacturasEmitidas());
        assertEquals(Long.valueOf(1L), r.getPlanId());
    }

    @Test
    void listarEmpresas_calculaDesgloseDeFacturasYFoliosRestantes() {
        List<Factura> facturas = List.of(factura("ACEPTADA"), factura("ACEPTADA"), factura("ERROR"));
        ConfiguracionDian config = new ConfiguracionDian();
        config.setFoliosAsignados(500);

        EmpresaAdminResumen r = useCase(facturas, config, 2L).listarEmpresas().get(0);

        assertTrue(r.isFactusConfigurado());
        assertEquals(3, r.getFacturasEmitidas());
        assertEquals(2, r.getFacturasAceptadas());
        assertEquals(0, r.getFacturasRechazadas());
        assertEquals(1, r.getFacturasError());
        assertEquals(500, r.getFoliosAsignados());
        assertEquals(497, r.getFoliosRestantes()); // 500 - 3
    }

    @Test
    void listarEmpresas_foliosRestantes_puedeQuedarNegativoSiSePaso() {
        List<Factura> facturas = List.of(factura("ACEPTADA"), factura("ACEPTADA"));
        ConfiguracionDian config = new ConfiguracionDian();
        config.setFoliosAsignados(1);

        EmpresaAdminResumen r = useCase(facturas, config, 1L).listarEmpresas().get(0);

        assertEquals(-1, r.getFoliosRestantes());
    }
}
