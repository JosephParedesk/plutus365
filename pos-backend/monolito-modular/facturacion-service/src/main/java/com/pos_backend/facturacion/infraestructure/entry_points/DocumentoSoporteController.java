package com.pos_backend.facturacion.infraestructure.entry_points;

import com.pos_backend.facturacion.domain.model.DocumentoSoporte;
import com.pos_backend.facturacion.domain.usecase.DocumentoSoporteUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pos/facturacion/documentos-soporte")
@RequiredArgsConstructor
public class DocumentoSoporteController {

    private final DocumentoSoporteUseCase documentoSoporteUseCase;

    @GetMapping
    public ResponseEntity<List<DocumentoSoporte>> listar(@RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(documentoSoporteUseCase.listar(empresaId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentoSoporte> buscar(@PathVariable Long id,
                                                    @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(documentoSoporteUseCase.buscarPorId(id, empresaId));
    }

    // Borra de Factus un documento soporte pendiente/rechazado, para poder reintentar.
    @DeleteMapping("/{id}/eliminar-no-validada")
    public ResponseEntity<Void> eliminarNoValidada(@PathVariable Long id,
                                                    @RequestHeader("X-Empresa-Id") String empresaId) {
        documentoSoporteUseCase.eliminarNoValidada(id, empresaId);
        return ResponseEntity.noContent().build();
    }

    // Genera, firma y envía a la DIAN el documento soporte de una compra ya registrada.
    @PostMapping("/generar/{compraId}")
    public ResponseEntity<DocumentoSoporte> generar(@PathVariable Long compraId,
                                                     @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(documentoSoporteUseCase.generar(compraId, empresaId));
    }
}
