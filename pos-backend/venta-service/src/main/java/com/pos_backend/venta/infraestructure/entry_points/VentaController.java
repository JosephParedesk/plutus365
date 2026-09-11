package com.pos_backend.venta.infraestructure.entry_points;

import com.pos_backend.venta.domain.model.Venta;
import com.pos_backend.venta.domain.usecase.VentaUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/pos/ventas")
@RequiredArgsConstructor
public class VentaController {

    private final VentaUseCase ventaUseCase;

    @GetMapping("/listar")
    public ResponseEntity<List<Venta>> listar(
            @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(ventaUseCase.listarVentas(empresaId));
    }

    @GetMapping("/buscar/{ventaId}")
    public ResponseEntity<Venta> buscar(
            @PathVariable Long ventaId,
            @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(ventaUseCase.buscarPorId(ventaId, empresaId));
    }

    @GetMapping("/filtrar")
    public ResponseEntity<List<Venta>> filtrar(
            @RequestHeader("X-Empresa-Id") String empresaId,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return ResponseEntity.ok(ventaUseCase.buscarConFiltros(empresaId, clienteId, fechaInicio, fechaFin));
    }

    @PostMapping("/registrar")
    public ResponseEntity<Venta> registrar(
            @RequestBody Venta venta,
            @RequestHeader("X-Empresa-Id") String empresaId,
            @RequestHeader(value = "X-Usuario-Nombre", required = false, defaultValue = "Sistema") String creadoPor) {
        return ResponseEntity.ok(ventaUseCase.registrarVenta(venta, empresaId, creadoPor));
    }

    @PutMapping("/anular/{ventaId}")
    public ResponseEntity<Void> anular(
            @PathVariable Long ventaId,
            @RequestHeader("X-Empresa-Id") String empresaId) {
        ventaUseCase.anularVenta(ventaId, empresaId);
        return ResponseEntity.noContent().build();
    }

    // ── Envía el comprobante de la venta al correo del cliente ──────
    // (Comprobante interno, no es factura electrónica DIAN)
    @PostMapping("/{ventaId}/enviar-comprobante")
    public ResponseEntity<Void> enviarComprobante(
            @PathVariable Long ventaId,
            @RequestBody java.util.Map<String, String> body,
            @RequestHeader("X-Empresa-Id") String empresaId) {
        ventaUseCase.enviarComprobantePorCorreo(ventaId, empresaId, body.get("correo"));
        return ResponseEntity.noContent().build();
    }

    // Vista previa del correo de comprobante, con el logo/color reales de la
    // empresa y datos de ejemplo — para la ventana de Configuración.
    @GetMapping(value = "/vista-previa-correo", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> vistaPreviaCorreo(@RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(ventaUseCase.generarVistaPreviaCorreo(empresaId));
    }
}
