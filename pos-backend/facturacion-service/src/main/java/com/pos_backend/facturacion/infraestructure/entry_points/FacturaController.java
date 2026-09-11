package com.pos_backend.facturacion.infraestructure.entry_points;

import com.pos_backend.facturacion.domain.model.Factura;
import com.pos_backend.facturacion.domain.model.FacturaDetalle;
import com.pos_backend.facturacion.domain.usecase.FacturaUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pos/facturacion")
@RequiredArgsConstructor
public class FacturaController {

    private final FacturaUseCase facturaUseCase;

    @GetMapping("/listar")
    public ResponseEntity<List<Factura>> listar(@RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(facturaUseCase.listar(empresaId));
    }

    @GetMapping("/buscar/{facturaId}")
    public ResponseEntity<Factura> buscar(
            @PathVariable Long facturaId,
            @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(facturaUseCase.buscarPorId(facturaId, empresaId));
    }

    // Todo lo necesario para renderizar/imprimir el documento visible de la factura
    @GetMapping("/buscar/{facturaId}/detalle")
    public ResponseEntity<FacturaDetalle> obtenerDetalle(
            @PathVariable Long facturaId,
            @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(facturaUseCase.obtenerDetalle(facturaId, empresaId));
    }

    // Envía la factura (documento + CUFE) al correo del cliente
    @PostMapping("/{facturaId}/enviar-correo")
    public ResponseEntity<Void> enviarCorreo(
            @PathVariable Long facturaId,
            @RequestBody java.util.Map<String, String> body,
            @RequestHeader("X-Empresa-Id") String empresaId) {
        facturaUseCase.enviarPorCorreo(facturaId, empresaId, body.get("correo"));
        return ResponseEntity.noContent().build();
    }

    // Genera, firma y envía a la DIAN la factura electrónica de una venta ya registrada.
    @PostMapping("/generar/{ventaId}")
    public ResponseEntity<Factura> generar(
            @PathVariable Long ventaId,
            @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(facturaUseCase.generarFactura(ventaId, empresaId));
    }

    // Borra de Factus una factura pendiente/rechazada, para poder reintentar.
    @DeleteMapping("/{facturaId}/eliminar-no-validada")
    public ResponseEntity<Void> eliminarNoValidada(
            @PathVariable Long facturaId,
            @RequestHeader("X-Empresa-Id") String empresaId) {
        facturaUseCase.eliminarNoValidada(facturaId, empresaId);
        return ResponseEntity.noContent().build();
    }

    // Vista previa del correo de factura electrónica, con el logo/color reales de
    // la empresa y datos de ejemplo — para la ventana de Configuración.
    @GetMapping(value = "/vista-previa-correo", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> vistaPreviaCorreo(@RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(facturaUseCase.generarVistaPreviaCorreo(empresaId));
    }
}
