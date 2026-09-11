package com.pos_backend.venta.infraestructure.entry_points;

import com.pos_backend.venta.domain.model.FacturaRecurrente;
import com.pos_backend.venta.domain.model.Venta;
import com.pos_backend.venta.domain.usecase.FacturaRecurrenteUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pos/ventas/recurrentes")
@RequiredArgsConstructor
public class FacturaRecurrenteController {

    private final FacturaRecurrenteUseCase recurrenteUseCase;

    @GetMapping
    public ResponseEntity<List<FacturaRecurrente>> listar(@RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(recurrenteUseCase.listar(empresaId));
    }

    /** Las que ya cumplieron su fecha y esperan confirmación del usuario. */
    @GetMapping("/pendientes")
    public ResponseEntity<List<FacturaRecurrente>> pendientes(@RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(recurrenteUseCase.pendientes(empresaId));
    }

    @PostMapping
    public ResponseEntity<FacturaRecurrente> crear(
            @RequestBody FacturaRecurrente r,
            @RequestHeader("X-Empresa-Id") String empresaId,
            @RequestHeader(value = "X-Usuario-Nombre", required = false, defaultValue = "Sistema") String usuario) {
        return ResponseEntity.ok(recurrenteUseCase.crear(r, empresaId, usuario));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FacturaRecurrente> actualizar(@PathVariable Long id, @RequestBody FacturaRecurrente cambios,
                                                        @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(recurrenteUseCase.actualizar(id, cambios, empresaId));
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<FacturaRecurrente> cambiarEstado(@PathVariable Long id,
                                                           @RequestBody Map<String, Boolean> body,
                                                           @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(recurrenteUseCase.cambiarEstado(id, Boolean.TRUE.equals(body.get("activa")), empresaId));
    }

    /** Genera la venta del período (descuenta inventario y contabiliza). */
    @PostMapping("/{id}/generar")
    public ResponseEntity<Venta> generar(
            @PathVariable Long id,
            @RequestHeader("X-Empresa-Id") String empresaId,
            @RequestHeader(value = "X-Usuario-Nombre", required = false, defaultValue = "Sistema") String usuario) {
        return ResponseEntity.ok(recurrenteUseCase.generarAhora(id, empresaId, usuario));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id,
                                         @RequestHeader("X-Empresa-Id") String empresaId) {
        recurrenteUseCase.eliminar(id, empresaId);
        return ResponseEntity.noContent().build();
    }
}
