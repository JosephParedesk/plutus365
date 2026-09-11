package com.pos_backend.venta.infraestructure.entry_points;

import com.pos_backend.venta.domain.model.FormaPago;
import com.pos_backend.venta.domain.model.Remision;
import com.pos_backend.venta.domain.model.Venta;
import com.pos_backend.venta.domain.usecase.RemisionUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pos/ventas/remisiones")
@RequiredArgsConstructor
public class RemisionController {

    private final RemisionUseCase remisionUseCase;

    @GetMapping
    public ResponseEntity<List<Remision>> listar(@RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(remisionUseCase.listar(empresaId));
    }

    @GetMapping("/filtrar")
    public ResponseEntity<List<Remision>> filtrar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(remisionUseCase.buscarPorRango(empresaId, fechaInicio, fechaFin));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Remision> buscar(@PathVariable Long id,
                                            @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(remisionUseCase.buscarPorId(id, empresaId));
    }

    @PostMapping
    public ResponseEntity<Remision> crear(
            @RequestBody Remision remision,
            @RequestHeader("X-Empresa-Id") String empresaId,
            @RequestHeader(value = "X-Usuario-Nombre", required = false, defaultValue = "Sistema") String usuario) {
        return ResponseEntity.ok(remisionUseCase.crear(remision, empresaId, usuario));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Remision> actualizar(@PathVariable Long id, @RequestBody Remision cambios,
                                                @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(remisionUseCase.actualizar(id, cambios, empresaId));
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<Remision> cambiarEstado(@PathVariable Long id,
                                                   @RequestBody Map<String, String> body,
                                                   @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(remisionUseCase.cambiarEstado(id, body.get("estado"), empresaId));
    }

    /** Convierte la remisión en venta real (descuenta stock y contabiliza). */
    @PostMapping("/{id}/convertir")
    public ResponseEntity<Venta> convertir(
            @PathVariable Long id,
            @RequestBody List<FormaPago> formasPago,
            @RequestHeader("X-Empresa-Id") String empresaId,
            @RequestHeader(value = "X-Usuario-Nombre", required = false, defaultValue = "Sistema") String usuario) {
        return ResponseEntity.ok(remisionUseCase.convertirEnVenta(id, formasPago, empresaId, usuario));
    }
}
