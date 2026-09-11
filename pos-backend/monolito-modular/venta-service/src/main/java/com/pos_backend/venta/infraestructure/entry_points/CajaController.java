package com.pos_backend.venta.infraestructure.entry_points;

import com.pos_backend.venta.domain.model.CajaSesion;
import com.pos_backend.venta.domain.usecase.CajaUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pos/ventas/caja")
@RequiredArgsConstructor
public class CajaController {

    private final CajaUseCase cajaUseCase;

    // Devuelve la caja abierta actual, o 204 si no hay ninguna
    @GetMapping("/actual")
    public ResponseEntity<CajaSesion> actual(@RequestHeader("X-Empresa-Id") String empresaId) {
        CajaSesion abierta = cajaUseCase.obtenerAbierta(empresaId);
        return abierta != null ? ResponseEntity.ok(abierta) : ResponseEntity.noContent().build();
    }

    // Cuánto debería haber en efectivo AHORA MISMO, sin cerrar la caja todavía
    @GetMapping("/esperado")
    public ResponseEntity<CajaUseCase.ResumenCaja> esperado(@RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(cajaUseCase.calcularEsperado(empresaId));
    }

    @GetMapping("/historial")
    public ResponseEntity<List<CajaSesion>> historial(@RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(cajaUseCase.listar(empresaId));
    }

    @PostMapping("/abrir")
    public ResponseEntity<CajaSesion> abrir(
            @RequestBody Map<String, Double> body,
            @RequestHeader("X-Empresa-Id") String empresaId,
            @RequestHeader(value = "X-Usuario-Nombre", required = false, defaultValue = "Sistema") String usuario) {
        return ResponseEntity.ok(cajaUseCase.abrir(body.get("montoApertura"), empresaId, usuario));
    }

    @PutMapping("/cerrar")
    public ResponseEntity<CajaSesion> cerrar(
            @RequestBody Map<String, Object> body,
            @RequestHeader("X-Empresa-Id") String empresaId,
            @RequestHeader(value = "X-Usuario-Nombre", required = false, defaultValue = "Sistema") String usuario) {
        Double montoDeclarado = body.get("montoDeclarado") != null ? Double.valueOf(body.get("montoDeclarado").toString()) : null;
        String observaciones = (String) body.get("observaciones");
        return ResponseEntity.ok(cajaUseCase.cerrar(montoDeclarado, observaciones, empresaId, usuario));
    }
}
