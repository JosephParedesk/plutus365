package com.pos_backend.nomina.infraestructure.entry_points;

import com.pos_backend.nomina.domain.model.Nomina;
import com.pos_backend.nomina.domain.model.NominaDetalle;
import com.pos_backend.nomina.domain.model.ParametrosNomina;
import com.pos_backend.nomina.domain.usecase.NominaUseCase;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pos/nomina")
@RequiredArgsConstructor
public class NominaController {

    private final NominaUseCase nominaUseCase;

    /** Parámetros legales vigentes, para que el frontend los muestre sin duplicarlos. */
    @GetMapping("/parametros")
    public ResponseEntity<Map<String, Object>> parametros() {
        return ResponseEntity.ok(Map.of(
                "anioVigencia", ParametrosNomina.ANIO_VIGENCIA,
                "smmlv", ParametrosNomina.SMMLV,
                "auxilioTransporte", ParametrosNomina.AUXILIO_TRANSPORTE,
                "topeAuxilioTransporte", ParametrosNomina.TOPE_AUXILIO_TRANSPORTE,
                "minimoSalarioIntegral", ParametrosNomina.MINIMO_SALARIO_INTEGRAL,
                "topeExoneracionAportes", ParametrosNomina.TOPE_EXONERACION_APORTES
        ));
    }

    @GetMapping("/periodos")
    public ResponseEntity<List<Nomina>> listar(@RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(nominaUseCase.listar(empresaId));
    }

    @GetMapping("/periodos/{nominaId}")
    public ResponseEntity<Nomina> buscar(@PathVariable Long nominaId,
                                         @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(nominaUseCase.buscarPorId(nominaId, empresaId));
    }

    @PostMapping("/liquidar")
    public ResponseEntity<Nomina> liquidar(@RequestBody LiquidarRequest req,
                                           @RequestHeader("X-Empresa-Id") String empresaId,
                                           @RequestHeader(value = "X-Usuario-Nombre", required = false,
                                                   defaultValue = "Sistema") String usuario) {
        return ResponseEntity.ok(nominaUseCase.liquidar(
                req.getAnio(), req.getMes(), req.getPeriodicidad(), req.getNovedades(), empresaId, usuario));
    }

    @PutMapping("/periodos/{nominaId}/pagar")
    public ResponseEntity<Nomina> pagar(@PathVariable Long nominaId,
                                        @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(nominaUseCase.marcarPagada(nominaId, empresaId));
    }

    @DeleteMapping("/periodos/{nominaId}")
    public ResponseEntity<Void> anular(@PathVariable Long nominaId,
                                       @RequestHeader("X-Empresa-Id") String empresaId) {
        nominaUseCase.anular(nominaId, empresaId);
        return ResponseEntity.noContent().build();
    }

    @Getter @Setter @NoArgsConstructor
    public static class LiquidarRequest {
        private Integer anio;
        private Integer mes;
        private String periodicidad;
        private Map<Long, NominaDetalle> novedades;
    }
}
