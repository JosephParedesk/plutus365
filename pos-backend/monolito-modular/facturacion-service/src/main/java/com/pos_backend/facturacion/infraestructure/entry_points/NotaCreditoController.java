package com.pos_backend.facturacion.infraestructure.entry_points;

import com.pos_backend.facturacion.domain.model.ConceptoNotaCredito;
import com.pos_backend.facturacion.domain.model.NotaCredito;
import com.pos_backend.facturacion.domain.usecase.NotaCreditoUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pos/facturacion/notas-credito")
@RequiredArgsConstructor
public class NotaCreditoController {

    private final NotaCreditoUseCase notaCreditoUseCase;

    /** Catálogo de conceptos válidos según el anexo técnico de la DIAN. */
    @GetMapping("/conceptos")
    public ResponseEntity<List<Map<String, String>>> conceptos() {
        return ResponseEntity.ok(Arrays.stream(ConceptoNotaCredito.values())
                .map(c -> Map.of("codigo", c.getCodigo(), "descripcion", c.getDescripcion()))
                .toList());
    }

    @GetMapping
    public ResponseEntity<List<NotaCredito>> listar(@RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(notaCreditoUseCase.listar(empresaId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotaCredito> buscar(@PathVariable Long id,
                                              @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(notaCreditoUseCase.buscarPorId(id, empresaId));
    }

    @GetMapping("/por-factura/{facturaId}")
    public ResponseEntity<List<NotaCredito>> porFactura(@PathVariable Long facturaId,
                                                       @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(notaCreditoUseCase.listarPorFactura(facturaId, empresaId));
    }

    // Borra de Factus una nota crédito pendiente/rechazada, para poder reintentar.
    @DeleteMapping("/{id}/eliminar-no-validada")
    public ResponseEntity<Void> eliminarNoValidada(@PathVariable Long id,
                                                    @RequestHeader("X-Empresa-Id") String empresaId) {
        notaCreditoUseCase.eliminarNoValidada(id, empresaId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<NotaCredito> emitir(
            @RequestBody NotaCredito nota,
            @RequestHeader("X-Empresa-Id") String empresaId,
            @RequestHeader(value = "X-Usuario-Nombre", required = false, defaultValue = "Sistema") String creadoPor) {
        return ResponseEntity.ok(notaCreditoUseCase.emitir(nota, empresaId, creadoPor));
    }
}
