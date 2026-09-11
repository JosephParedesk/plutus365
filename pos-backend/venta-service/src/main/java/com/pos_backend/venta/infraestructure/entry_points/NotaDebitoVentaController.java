package com.pos_backend.venta.infraestructure.entry_points;

import com.pos_backend.venta.domain.model.NotaDebitoVenta;
import com.pos_backend.venta.domain.usecase.NotaDebitoVentaUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/pos/ventas/notas-debito")
@RequiredArgsConstructor
public class NotaDebitoVentaController {

    private final NotaDebitoVentaUseCase notaDebitoVentaUseCase;

    @GetMapping
    public ResponseEntity<List<NotaDebitoVenta>> listar(@RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(notaDebitoVentaUseCase.listar(empresaId));
    }

    @GetMapping("/filtrar")
    public ResponseEntity<List<NotaDebitoVenta>> filtrar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(notaDebitoVentaUseCase.buscarPorRango(empresaId, fechaInicio, fechaFin));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotaDebitoVenta> buscar(@PathVariable Long id,
                                                   @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(notaDebitoVentaUseCase.buscarPorId(id, empresaId));
    }

    @PostMapping
    public ResponseEntity<NotaDebitoVenta> registrar(
            @RequestBody NotaDebitoVenta nota,
            @RequestHeader("X-Empresa-Id") String empresaId,
            @RequestHeader(value = "X-Usuario-Nombre", required = false, defaultValue = "Sistema") String usuario) {
        return ResponseEntity.ok(notaDebitoVentaUseCase.registrar(nota, empresaId, usuario));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> anular(@PathVariable Long id, @RequestHeader("X-Empresa-Id") String empresaId) {
        notaDebitoVentaUseCase.anular(id, empresaId);
        return ResponseEntity.noContent().build();
    }
}
