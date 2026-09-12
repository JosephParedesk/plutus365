package com.pos_backend.facturacion.domain.usecase;

import com.pos_backend.facturacion.domain.model.EmpresaAdminResumen;
import com.pos_backend.facturacion.domain.model.EmpresaRemota;
import com.pos_backend.facturacion.domain.model.UsuarioAdminResumen;
import com.pos_backend.facturacion.domain.model.gateway.ConfiguracionDianGateway;
import com.pos_backend.facturacion.domain.model.gateway.EmpresaConsultaGateway;
import com.pos_backend.facturacion.domain.model.gateway.FacturaGateway;
import com.pos_backend.facturacion.domain.model.gateway.UsuarioConsultaGateway;
import lombok.RequiredArgsConstructor;

import java.util.List;

// Panel de super administrador (rol SUPERADMIN, ver PermisosInterceptor del
// gateway): Plutus365 es quien tramita las credenciales de Factus con cada
// empresa (manda los documentos, Factus responde con usuario/clave), así que
// es Plutus365 quien las carga acá — no cada empresa por su cuenta. También
// da visibilidad de todos los usuarios registrados y cuántas facturas emite
// cada empresa, para poder hacer seguimiento del negocio.
@RequiredArgsConstructor
public class AdminUseCase {

    private final EmpresaConsultaGateway empresaConsultaGateway;
    private final ConfiguracionDianGateway configuracionDianGateway;
    private final FacturaGateway facturaGateway;
    private final UsuarioConsultaGateway usuarioConsultaGateway;

    public List<EmpresaAdminResumen> listarEmpresas() {
        List<EmpresaRemota> empresas = empresaConsultaGateway.listarTodas();
        return empresas.stream().map(e -> new EmpresaAdminResumen(
                e.getEmpresaId(),
                nombre(e),
                e.getTipoDocumento(),
                e.getNumeroDocumento(),
                e.getCorreo(),
                configuracionDianGateway.buscarPorEmpresaId(e.getEmpresaId()) != null,
                facturaGateway.listar(e.getEmpresaId()).size()
        )).toList();
    }

    public List<UsuarioAdminResumen> listarUsuarios() {
        return usuarioConsultaGateway.listarTodos();
    }

    private String nombre(EmpresaRemota e) {
        if (e.getRazonSocial() != null && !e.getRazonSocial().isBlank())
            return e.getRazonSocial();
        return (e.getNombres() == null ? "" : e.getNombres()) + " " + (e.getApellidos() == null ? "" : e.getApellidos());
    }
}
