package com.pos_backend.facturacion.infraestructure.entry_points;

import com.pos_backend.facturacion.domain.model.NominaElectronica;
import com.pos_backend.facturacion.domain.usecase.NominaElectronicaUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pos/facturacion/nomina-electronica")
@RequiredArgsConstructor
public class NominaElectronicaController {

    private final NominaElectronicaUseCase nominaElectronicaUseCase;

    @GetMapping
    public ResponseEntity<List<NominaElectronica>> listar(@RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(nominaElectronicaUseCase.listar(empresaId));
    }

    @GetMapping("/por-periodo/{nominaId}")
    public ResponseEntity<List<NominaElectronica>> porPeriodo(@PathVariable Long nominaId,
                                                                @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(nominaElectronicaUseCase.listarPorNomina(nominaId, empresaId));
    }

    // Borra de Factus una nómina electrónica pendiente/rechazada, para poder reintentar.
    @DeleteMapping("/{id}/eliminar-no-validada")
    public ResponseEntity<Void> eliminarNoValidada(@PathVariable Long id,
                                                    @RequestHeader("X-Empresa-Id") String empresaId) {
        nominaElectronicaUseCase.eliminarNoValidada(id, empresaId);
        return ResponseEntity.noContent().build();
    }

    // Transmite a la DIAN la nómina de UN empleado dentro de un período ya liquidado.
    @PostMapping("/generar/{nominaId}/{empleadoId}")
    public ResponseEntity<NominaElectronica> generar(
            @PathVariable Long nominaId, @PathVariable Long empleadoId,
            @RequestHeader("X-Empresa-Id") String empresaId,
            @RequestHeader(value = "X-Usuario-Nombre", required = false, defaultValue = "Sistema") String creadoPor) {
        return ResponseEntity.ok(nominaElectronicaUseCase.generar(nominaId, empleadoId, empresaId, creadoPor));
    }
}
