package com.pos_backend.facturacion.domain.usecase;

import com.pos_backend.facturacion.domain.model.EmpresaAdminResumen;
import com.pos_backend.facturacion.domain.model.EmpresaRemota;
import com.pos_backend.facturacion.domain.model.Factura;
import com.pos_backend.facturacion.domain.model.UsuarioAdminResumen;
import com.pos_backend.facturacion.domain.model.gateway.ConfiguracionDianGateway;
import com.pos_backend.facturacion.domain.model.gateway.EmpresaConsultaGateway;
import com.pos_backend.facturacion.domain.model.gateway.FacturaGateway;
import com.pos_backend.facturacion.domain.model.gateway.UsuarioConsultaGateway;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

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

        // planId vive por usuario, no por empresa (ver [[usuarios]]) — toma el de
        // cualquier usuario de esa empresa, todos comparten el mismo tras un cambio
        // de plan (ver UsuarioUseCase.cambiarPlanEmpresa).
        Map<String, Long> planPorEmpresa = usuarioConsultaGateway.listarTodos().stream()
                .collect(java.util.stream.Collectors.toMap(
                        UsuarioAdminResumen::getEmpresaId, UsuarioAdminResumen::getPlanId, (a, b) -> a));

        return empresas.stream().map(e -> {
            var config = configuracionDianGateway.buscarPorEmpresaId(e.getEmpresaId());
            List<Factura> facturas = facturaGateway.listar(e.getEmpresaId());
            int aceptadas = (int) facturas.stream().filter(f -> "ACEPTADA".equals(f.getEstado())).count();
            int rechazadas = (int) facturas.stream().filter(f -> "RECHAZADA".equals(f.getEstado())).count();
            int error = (int) facturas.stream().filter(f -> "ERROR".equals(f.getEstado())).count();
            Integer foliosAsignados = config != null ? config.getFoliosAsignados() : null;

            return new EmpresaAdminResumen(
                    e.getEmpresaId(),
                    nombre(e),
                    e.getTipoPersona(),
                    e.getTipoDocumento(),
                    e.getNumeroDocumento(),
                    e.getDv(),
                    e.getRegimenFiscal(),
                    e.getCorreo(),
                    e.getTelefono(),
                    e.getDireccion(),
                    e.getCiudad(),
                    e.getDepartamento(),
                    e.getPais(),
                    config != null,
                    planPorEmpresa.get(e.getEmpresaId()),
                    facturas.size(),
                    aceptadas,
                    rechazadas,
                    error,
                    foliosAsignados,
                    foliosAsignados != null ? foliosAsignados - facturas.size() : null
            );
        }).toList();
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
