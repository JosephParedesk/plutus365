package com.pos_backend.venta.infraestructure.entry_points;

import com.pos_backend.venta.domain.model.Cotizacion;
import com.pos_backend.venta.domain.model.FormaPago;
import com.pos_backend.venta.domain.model.Venta;
import com.pos_backend.venta.domain.usecase.CotizacionUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pos/ventas/cotizaciones")
@RequiredArgsConstructor
public class CotizacionController {

    private final CotizacionUseCase cotizacionUseCase;

    @GetMapping
    public ResponseEntity<List<Cotizacion>> listar(@RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(cotizacionUseCase.listar(empresaId));
    }

    @GetMapping("/filtrar")
    public ResponseEntity<List<Cotizacion>> filtrar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(cotizacionUseCase.buscarPorRango(empresaId, fechaInicio, fechaFin));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cotizacion> buscar(@PathVariable Long id,
                                             @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(cotizacionUseCase.buscarPorId(id, empresaId));
    }

    @PostMapping
    public ResponseEntity<Cotizacion> crear(
            @RequestBody Cotizacion cotizacion,
            @RequestHeader("X-Empresa-Id") String empresaId,
            @RequestHeader(value = "X-Usuario-Nombre", required = false, defaultValue = "Sistema") String usuario) {
        return ResponseEntity.ok(cotizacionUseCase.crear(cotizacion, empresaId, usuario));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cotizacion> actualizar(@PathVariable Long id, @RequestBody Cotizacion cambios,
                                                 @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(cotizacionUseCase.actualizar(id, cambios, empresaId));
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<Cotizacion> cambiarEstado(@PathVariable Long id,
                                                    @RequestBody Map<String, String> body,
                                                    @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(cotizacionUseCase.cambiarEstado(id, body.get("estado"), empresaId));
    }

    /** Convierte la cotización en venta real (descuenta stock y contabiliza). */
    @PostMapping("/{id}/convertir")
    public ResponseEntity<Venta> convertir(
            @PathVariable Long id,
            @RequestBody List<FormaPago> formasPago,
            @RequestHeader("X-Empresa-Id") String empresaId,
            @RequestHeader(value = "X-Usuario-Nombre", required = false, defaultValue = "Sistema") String usuario) {
        return ResponseEntity.ok(cotizacionUseCase.convertirEnVenta(id, formasPago, empresaId, usuario));
    }
}
