package com.pos_backend.facturacion.infraestructure.entry_points;

import com.pos_backend.facturacion.domain.model.ConceptoReclamoRadian;
import com.pos_backend.facturacion.domain.model.EventoRadian;
import com.pos_backend.facturacion.domain.model.RecepcionDocumento;
import com.pos_backend.facturacion.domain.usecase.RecepcionDocumentoUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pos/facturacion/recepcion")
@RequiredArgsConstructor
public class RecepcionDocumentoController {

    private final RecepcionDocumentoUseCase recepcionDocumentoUseCase;

    /** Eventos que Plutus365 puede emitir manualmente (034 "tácita" queda fuera a propósito, la genera la DIAN sola). */
    @GetMapping("/eventos")
    public ResponseEntity<List<Map<String, String>>> eventos() {
        return ResponseEntity.ok(Arrays.stream(EventoRadian.values())
                .map(e -> Map.of("codigo", e.getCodigo(), "nombre", e.getNombre()))
                .toList());
    }

    @GetMapping("/conceptos-reclamo")
    public ResponseEntity<List<Map<String, String>>> conceptosReclamo() {
        return ResponseEntity.ok(Arrays.stream(ConceptoReclamoRadian.values())
                .map(c -> Map.of("codigo", c.getCodigo(), "descripcion", c.getDescripcion()))
                .toList());
    }

    @GetMapping
    public ResponseEntity<List<RecepcionDocumento>> listar(@RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(recepcionDocumentoUseCase.listar(empresaId));
    }

    @GetMapping("/por-compra/{compraId}")
    public ResponseEntity<RecepcionDocumento> porCompra(@PathVariable Long compraId,
                                                         @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(recepcionDocumentoUseCase.buscarPorCompraId(compraId, empresaId));
    }

    // Sube a Factus el CUFE de la factura del proveedor (compra importada por XML).
    @PostMapping("/cargar/{compraId}")
    public ResponseEntity<RecepcionDocumento> cargar(
            @PathVariable Long compraId,
            @RequestHeader("X-Empresa-Id") String empresaId,
            @RequestHeader(value = "X-Usuario-Nombre", required = false, defaultValue = "Sistema") String creadoPor) {
        return ResponseEntity.ok(recepcionDocumentoUseCase.cargar(compraId, empresaId, creadoPor));
    }

    @PostMapping("/{compraId}/evento")
    public ResponseEntity<RecepcionDocumento> emitirEvento(
            @PathVariable Long compraId,
            @RequestBody SolicitudEvento solicitud,
            @RequestHeader("X-Empresa-Id") String empresaId) {
        EventoRadian evento = EventoRadian.porCodigo(solicitud.codigoEvento());
        ConceptoReclamoRadian concepto = solicitud.conceptoReclamoCodigo() != null
                ? ConceptoReclamoRadian.porCodigo(solicitud.conceptoReclamoCodigo()) : null;
        return ResponseEntity.ok(recepcionDocumentoUseCase.emitirEvento(compraId, evento, concepto, solicitud.persona(), empresaId));
    }

    public record SolicitudEvento(String codigoEvento, String conceptoReclamoCodigo, RecepcionDocumento.Persona persona) {}
}
