package com.pos_backend.venta.infraestructure.entry_points;

import com.pos_backend.venta.domain.model.ReciboCaja;
import com.pos_backend.venta.domain.model.Venta;
import com.pos_backend.venta.domain.usecase.ReciboCajaUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/pos/ventas/recibos-caja")
@RequiredArgsConstructor
public class ReciboCajaController {

    private final ReciboCajaUseCase reciboCajaUseCase;

    @GetMapping
    public ResponseEntity<List<ReciboCaja>> listar(@RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(reciboCajaUseCase.listar(empresaId));
    }

    @GetMapping("/filtrar")
    public ResponseEntity<List<ReciboCaja>> filtrar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(reciboCajaUseCase.buscarPorRango(empresaId, fechaInicio, fechaFin));
    }

    /** Facturas con saldo por cobrar, para elegir a cuáles aplicar el abono. */
    @GetMapping("/cartera")
    public ResponseEntity<List<Venta>> cartera(
            @RequestParam(required = false) Long clienteId,
            @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(reciboCajaUseCase.carteraPendiente(clienteId, empresaId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReciboCaja> buscar(@PathVariable Long id,
                                             @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(reciboCajaUseCase.buscarPorId(id, empresaId));
    }

    @PostMapping
    public ResponseEntity<ReciboCaja> registrar(
            @RequestBody ReciboCaja recibo,
            @RequestHeader("X-Empresa-Id") String empresaId,
            @RequestHeader(value = "X-Usuario-Nombre", required = false, defaultValue = "Sistema") String usuario) {
        return ResponseEntity.ok(reciboCajaUseCase.registrar(recibo, empresaId, usuario));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> anular(@PathVariable Long id,
                                       @RequestHeader("X-Empresa-Id") String empresaId) {
        reciboCajaUseCase.anular(id, empresaId);
        return ResponseEntity.noContent().build();
    }
}
