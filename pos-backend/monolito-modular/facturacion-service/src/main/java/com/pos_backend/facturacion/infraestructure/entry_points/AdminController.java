package com.pos_backend.facturacion.infraestructure.entry_points;

import com.pos_backend.facturacion.domain.model.ConfiguracionDian;
import com.pos_backend.facturacion.domain.model.EmpresaAdminResumen;
import com.pos_backend.facturacion.domain.model.UsuarioAdminResumen;
import com.pos_backend.facturacion.domain.model.gateway.FacturaElectronicaGateway.RangoNumeracion;
import com.pos_backend.facturacion.domain.usecase.AdminUseCase;
import com.pos_backend.facturacion.domain.usecase.ConfiguracionDianUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Solo rol SUPERADMIN puede llegar acá — lo filtra PermisosInterceptor del
// gateway (módulo PLATAFORMA), ni siquiera un ADMIN normal de una empresa
// pasa. Ver AdminUseCase para el porqué.
@RestController
@RequestMapping("/api/pos/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminUseCase adminUseCase;
    private final ConfiguracionDianUseCase configuracionDianUseCase;

    @GetMapping("/empresas")
    public ResponseEntity<List<EmpresaAdminResumen>> listarEmpresas() {
        return ResponseEntity.ok(adminUseCase.listarEmpresas());
    }

    @GetMapping("/usuarios")
    public ResponseEntity<List<UsuarioAdminResumen>> listarUsuarios() {
        return ResponseEntity.ok(adminUseCase.listarUsuarios());
    }

    // codigoDocumento: 21 factura, 22 nota crédito, 23 nota débito, 24 documento
    // soporte, 26 nómina — los rangos que Factus le asignó a esta empresa.
    @GetMapping("/empresas/{empresaId}/factus/rangos-numeracion")
    public ResponseEntity<List<RangoNumeracion>> rangosNumeracion(
            @PathVariable String empresaId,
            @RequestParam String codigoDocumento) {
        return ResponseEntity.ok(configuracionDianUseCase.listarRangosNumeracion(empresaId, codigoDocumento));
    }

    @GetMapping("/empresas/{empresaId}/factus")
    public ResponseEntity<ConfiguracionDian> obtenerFactus(@PathVariable String empresaId) {
        return ResponseEntity.ok(configuracionDianUseCase.obtener(empresaId));
    }

    @PutMapping("/empresas/{empresaId}/factus")
    public ResponseEntity<ConfiguracionDian> guardarFactus(
            @PathVariable String empresaId,
            @RequestBody ConfiguracionDian configuracion) {
        return ResponseEntity.ok(configuracionDianUseCase.guardar(configuracion, empresaId));
    }
}
