package com.pos_backend.facturacion.infraestructure.entry_points;

import com.pos_backend.facturacion.domain.model.ConceptoNotaAjuste;
import com.pos_backend.facturacion.domain.model.NotaAjusteDocumentoSoporte;
import com.pos_backend.facturacion.domain.usecase.NotaAjusteDocumentoSoporteUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pos/facturacion/notas-ajuste-documento-soporte")
@RequiredArgsConstructor
public class NotaAjusteDocumentoSoporteController {

    private final NotaAjusteDocumentoSoporteUseCase notaAjusteUseCase;

    /** Catálogo de motivos válidos según la tabla de referencia de Factus. */
    @GetMapping("/conceptos")
    public ResponseEntity<List<Map<String, String>>> conceptos() {
        return ResponseEntity.ok(Arrays.stream(ConceptoNotaAjuste.values())
                .map(c -> Map.of("codigo", c.getCodigo(), "descripcion", c.getDescripcion()))
                .toList());
    }

    @GetMapping
    public ResponseEntity<List<NotaAjusteDocumentoSoporte>> listar(@RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(notaAjusteUseCase.listar(empresaId));
    }

    @GetMapping("/por-documento-soporte/{documentoSoporteId}")
    public ResponseEntity<List<NotaAjusteDocumentoSoporte>> porDocumentoSoporte(
            @PathVariable Long documentoSoporteId, @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(notaAjusteUseCase.listarPorDocumentoSoporte(documentoSoporteId, empresaId));
    }

    @DeleteMapping("/{id}/eliminar-no-validada")
    public ResponseEntity<Void> eliminarNoValidada(@PathVariable Long id, @RequestHeader("X-Empresa-Id") String empresaId) {
        notaAjusteUseCase.eliminarNoValidada(id, empresaId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{documentoSoporteId}")
    public ResponseEntity<NotaAjusteDocumentoSoporte> emitir(
            @PathVariable Long documentoSoporteId,
            @RequestBody SolicitudNotaAjuste solicitud,
            @RequestHeader("X-Empresa-Id") String empresaId,
            @RequestHeader(value = "X-Usuario-Nombre", required = false, defaultValue = "Sistema") String creadoPor) {
        return ResponseEntity.ok(notaAjusteUseCase.emitir(
                documentoSoporteId, solicitud.conceptoCodigo(), solicitud.observacion(), empresaId, creadoPor));
    }

    public record SolicitudNotaAjuste(String conceptoCodigo, String observacion) {}
}
