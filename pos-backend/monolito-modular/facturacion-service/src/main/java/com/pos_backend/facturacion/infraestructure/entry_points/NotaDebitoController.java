package com.pos_backend.facturacion.infraestructure.entry_points;

import com.pos_backend.facturacion.domain.model.ConceptoNotaDebito;
import com.pos_backend.facturacion.domain.model.NotaDebito;
import com.pos_backend.facturacion.domain.usecase.NotaDebitoUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pos/facturacion/notas-debito")
@RequiredArgsConstructor
public class NotaDebitoController {

    private final NotaDebitoUseCase notaDebitoUseCase;

    /** Catálogo de conceptos válidos para nota débito según la tabla de Factus. */
    @GetMapping("/conceptos")
    public ResponseEntity<List<Map<String, String>>> conceptos() {
        return ResponseEntity.ok(Arrays.stream(ConceptoNotaDebito.values())
                .map(c -> Map.of("codigo", c.getCodigo(), "descripcion", c.getDescripcion()))
                .toList());
    }

    @GetMapping
    public ResponseEntity<List<NotaDebito>> listar(@RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(notaDebitoUseCase.listar(empresaId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotaDebito> buscar(@PathVariable Long id,
                                              @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(notaDebitoUseCase.buscarPorId(id, empresaId));
    }

    @GetMapping("/por-factura/{facturaId}")
    public ResponseEntity<List<NotaDebito>> porFactura(@PathVariable Long facturaId,
                                                         @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(notaDebitoUseCase.listarPorFactura(facturaId, empresaId));
    }

    // Borra de Factus una nota débito pendiente/rechazada, para poder reintentar.
    @DeleteMapping("/{id}/eliminar-no-validada")
    public ResponseEntity<Void> eliminarNoValidada(@PathVariable Long id,
                                                    @RequestHeader("X-Empresa-Id") String empresaId) {
        notaDebitoUseCase.eliminarNoValidada(id, empresaId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<NotaDebito> emitir(
            @RequestBody NotaDebito nota,
            @RequestHeader("X-Empresa-Id") String empresaId,
            @RequestHeader(value = "X-Usuario-Nombre", required = false, defaultValue = "Sistema") String creadoPor) {
        return ResponseEntity.ok(notaDebitoUseCase.emitir(nota, empresaId, creadoPor));
    }
}
