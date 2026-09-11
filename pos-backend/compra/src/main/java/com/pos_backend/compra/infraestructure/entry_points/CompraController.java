package com.pos_backend.compra.infraestructure.entry_points;

import com.pos_backend.compra.domain.model.Compra;
import com.pos_backend.compra.domain.usecase.CompraUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/pos/compras")
@RequiredArgsConstructor
public class CompraController {

    private final CompraUseCase compraUseCase;

    @GetMapping("/listar")
    public ResponseEntity<List<Compra>> listar(
            @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(compraUseCase.listarCompras(empresaId));
    }

    @GetMapping("/buscar/{compraId}")
    public ResponseEntity<Compra> buscar(
            @PathVariable Long compraId,
            @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(compraUseCase.buscarPorId(compraId, empresaId));
    }

    @GetMapping("/filtrar")
    public ResponseEntity<List<Compra>> filtrar(
            @RequestHeader("X-Empresa-Id") String empresaId,
            @RequestParam(required = false) Long proveedorId,
            @RequestParam(required = false) String tipoTransaccion,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) String creadoPor) {
        return ResponseEntity.ok(compraUseCase.buscarConFiltros(
                empresaId, proveedorId, tipoTransaccion, fechaInicio, fechaFin, creadoPor));
    }

    @PostMapping("/registrar")
    public ResponseEntity<Compra> registrar(
            @RequestBody Compra compra,
            @RequestHeader("X-Empresa-Id") String empresaId,
            @RequestHeader(value = "X-Usuario-Nombre", required = false, defaultValue = "Sistema") String creadoPor) {
        return ResponseEntity.ok(compraUseCase.registrarCompra(compra, empresaId, creadoPor));
    }

    @PutMapping("/anular/{compraId}")
    public ResponseEntity<Void> anular(
            @PathVariable Long compraId,
            @RequestHeader("X-Empresa-Id") String empresaId) {
        compraUseCase.anularCompra(compraId, empresaId);
        return ResponseEntity.noContent().build();
    }

    // Facturas pendientes de un proveedor — para armar el recibo de pago
    @GetMapping("/pendientes/{proveedorId}")
    public ResponseEntity<List<Compra>> pendientesPorProveedor(
            @PathVariable Long proveedorId,
            @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(compraUseCase.pendientesPorProveedor(empresaId, proveedorId));
    }

    // Cuentas por pagar (crédito a proveedores) próximas a vencer — para la notificación
    @GetMapping("/proximas-vencer")
    public ResponseEntity<List<Compra>> proximasAVencer(
            @RequestHeader("X-Empresa-Id") String empresaId,
            @RequestParam(defaultValue = "7") int dias) {
        return ResponseEntity.ok(compraUseCase.proximasAVencer(empresaId, dias));
    }
}