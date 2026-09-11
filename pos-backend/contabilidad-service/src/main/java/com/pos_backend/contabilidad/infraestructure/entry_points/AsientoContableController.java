package com.pos_backend.contabilidad.infraestructure.entry_points;

import com.pos_backend.contabilidad.domain.model.AsientoContable;
import com.pos_backend.contabilidad.domain.model.CompraParaAsiento;
import com.pos_backend.contabilidad.domain.model.NotaCreditoParaAsiento;
import com.pos_backend.contabilidad.domain.model.ReciboCajaParaAsiento;
import com.pos_backend.contabilidad.domain.model.VentaParaAsiento;
import com.pos_backend.contabilidad.domain.usecase.AsientoContableUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pos/contabilidad/asientos")
@RequiredArgsConstructor
public class AsientoContableController {

    private final AsientoContableUseCase asientoContableUseCase;

    @GetMapping
    public ResponseEntity<List<AsientoContable>> listar(@RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(asientoContableUseCase.listar(empresaId));
    }

    @GetMapping("/{asientoId}")
    public ResponseEntity<AsientoContable> buscar(
            @PathVariable Long asientoId,
            @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(asientoContableUseCase.buscarPorId(asientoId, empresaId));
    }

    // Creación manual (partida doble validada por el backend)
    @PostMapping
    public ResponseEntity<AsientoContable> crear(
            @RequestBody AsientoContable asiento,
            @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(asientoContableUseCase.crear(asiento, empresaId));
    }

    // Llamado automáticamente por venta-service al registrar una venta
    @PostMapping("/desde-venta")
    public ResponseEntity<AsientoContable> desdeVenta(
            @RequestBody VentaParaAsiento venta,
            @RequestHeader("X-Empresa-Id") String empresaId,
            @RequestHeader(value = "X-Usuario-Nombre", required = false, defaultValue = "Sistema") String creadoPor) {
        return ResponseEntity.ok(asientoContableUseCase.generarDesdeVenta(venta, empresaId, creadoPor));
    }

    // Llamado automáticamente por compra-service al registrar una compra/recibo/nota
    @PostMapping("/desde-compra")
    public ResponseEntity<AsientoContable> desdeCompra(
            @RequestBody CompraParaAsiento compra,
            @RequestHeader("X-Empresa-Id") String empresaId,
            @RequestHeader(value = "X-Usuario-Nombre", required = false, defaultValue = "Sistema") String creadoPor) {
        return ResponseEntity.ok(asientoContableUseCase.generarDesdeCompra(compra, empresaId, creadoPor));
    }

    // Llamado por facturacion-service al emitir una nota crédito
    @PostMapping("/desde-nota-credito")
    public ResponseEntity<AsientoContable> desdeNotaCredito(
            @RequestBody NotaCreditoParaAsiento nota,
            @RequestHeader("X-Empresa-Id") String empresaId,
            @RequestHeader(value = "X-Usuario-Nombre", required = false, defaultValue = "Sistema") String creadoPor) {
        return ResponseEntity.ok(asientoContableUseCase.generarDesdeNotaCredito(nota, empresaId, creadoPor));
    }

    // Llamado por venta-service al registrar un recibo de caja
    @PostMapping("/desde-recibo-caja")
    public ResponseEntity<AsientoContable> desdeReciboCaja(
            @RequestBody ReciboCajaParaAsiento recibo,
            @RequestHeader("X-Empresa-Id") String empresaId,
            @RequestHeader(value = "X-Usuario-Nombre", required = false, defaultValue = "Sistema") String creadoPor) {
        return ResponseEntity.ok(asientoContableUseCase.generarDesdeReciboCaja(recibo, empresaId, creadoPor));
    }

    // Llamado por inventario-service al importar el saldo inicial de inventario (una sola vez por empresa)
    @PostMapping("/desde-saldo-inicial-inventario")
    public ResponseEntity<AsientoContable> desdeSaldoInicialInventario(
            @RequestBody com.pos_backend.contabilidad.domain.model.SaldoInicialInventarioParaAsiento datos,
            @RequestHeader("X-Empresa-Id") String empresaId,
            @RequestHeader(value = "X-Usuario-Nombre", required = false, defaultValue = "Sistema") String creadoPor) {
        return ResponseEntity.ok(asientoContableUseCase.generarDesdeSaldoInicialInventario(datos, empresaId, creadoPor));
    }
}
