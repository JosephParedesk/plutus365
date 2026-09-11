package com.pos_backend.facturacion.infraestructure.entry_points;

import com.pos_backend.facturacion.domain.model.ConfiguracionDian;
import com.pos_backend.facturacion.domain.usecase.ConfiguracionDianUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pos/facturacion/configuracion")
@RequiredArgsConstructor
public class ConfiguracionDianController {

    private final ConfiguracionDianUseCase configuracionDianUseCase;

    @GetMapping
    public ResponseEntity<ConfiguracionDian> obtener(@RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(configuracionDianUseCase.obtener(empresaId));
    }

    @PutMapping
    public ResponseEntity<ConfiguracionDian> guardar(
            @RequestBody ConfiguracionDian configuracion,
            @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(configuracionDianUseCase.guardar(configuracion, empresaId));
    }

    // codigoDocumento: 21 factura, 22 nota crédito, 23 nota débito, 24 documento soporte, 26 nómina.
    @GetMapping("/rangos-numeracion")
    public ResponseEntity<java.util.List<com.pos_backend.facturacion.domain.model.gateway.FacturaElectronicaGateway.RangoNumeracion>> rangosNumeracion(
            @RequestParam String codigoDocumento,
            @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(configuracionDianUseCase.listarRangosNumeracion(empresaId, codigoDocumento));
    }
}
